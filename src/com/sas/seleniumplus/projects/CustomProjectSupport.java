package com.sas.seleniumplus.projects;

import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import com.sas.seleniumplus.builders.AppMapBuilder;
import com.sas.seleniumplus.natures.ProjectNature;
import com.sas.seleniumplus.popupmenu.FileTemplates;

public class CustomProjectSupport {

	public static String SELENIUM_PLUS;
	public static String STAFDIR;
	public static String packageDir;
	public static String mapPkg;

	/**
	 * For this marvelous project we need to: - create the default Eclipse
	 * project - add the custom project nature - create the folder structure
	 *
	 * @param projectName
	 * @param location
	 * @param natureId
	 * @return
	 */
	public static IProject createProject(String projectName, URI location, String companyName) {
		Assert.isNotNull(projectName);
		Assert.isNotNull(companyName);
		Assert.isTrue(projectName.trim().length() > 0);

		IProject project = createBaseProject(projectName, location);
		packageDir = BaseProject.SRC_SRC_DIR+ "/com/" + companyName.toLowerCase() +"/"+ projectName.toLowerCase() +"/"+ BaseProject.SRC_TEST_DIR+"/" ;
		mapPkg = "com." + companyName.toLowerCase() + "."+ projectName.toLowerCase() ;

		try {
			addNature(project);

			String[] paths = {
					packageDir,
					packageDir + "/map", // what is this?
					BaseProject.TEST_DIR,
					BaseProject.BENCH_DIR,
					BaseProject.DIF_DIR,
					BaseProject.LOGS_DIR,
					BaseProject.DATAPOOL_DIR
			};
			addToProjectStructure(project, paths);
		} catch (Exception e) {
			e.printStackTrace();
			project = null;
		}

		return project;
	}

	/**
	 * Just do the basics: create a basic project.
	 *
	 * @param location
	 * @param projectName
	 */
	private static IProject createBaseProject(String projectName, URI location) {

		IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);

		if (!newProject.exists()) {
			URI projectLocation = location;
			IProjectDescription desc = newProject.getWorkspace()
					.newProjectDescription(newProject.getName());
			desc.setNatureIds(new String[] { JavaCore.NATURE_ID,
					"org.eclipse.wst.common.project.facet.core.nature" });
			org.eclipse.core.resources.ICommand[] commands = new ICommand[] {
					desc.newCommand(), desc.newCommand() };
			commands[0].setBuilderName(AppMapBuilder.BUILDER_ID);
			commands[1].setBuilderName(JavaCore.BUILDER_ID);
			desc.setBuildSpec(commands);
			if (location != null
					&& ResourcesPlugin.getWorkspace().getRoot()
							.getLocationURI().equals(location)) {
				projectLocation = null;
			}
			desc.setLocationURI(projectLocation);

			try {

				newProject.create(desc, null);
				if (!newProject.isOpen()) {
					newProject.open(null);
				}
				IFolder srcFolder = newProject.getFolder("src");
				IJavaProject javaProject = JavaCore.create(newProject);
				org.eclipse.jdt.core.IClasspathEntry src = JavaCore
						.newSourceEntry(srcFolder.getFullPath());
				IClasspathEntry jre = JavaCore.newContainerEntry(new Path(
						JavaRuntime.JRE_CONTAINER), new IAccessRule[0],
						new IClasspathAttribute[] { JavaCore
								.newClasspathAttribute("owner.project.facets",
										"java") }, false);
				IClasspathEntry[] entries = new IClasspathEntry[] { src, jre };

				if (SELENIUM_PLUS != null) {
					IClasspathEntry seleniumjar = JavaCore.newLibraryEntry(
							new Path(SELENIUM_PLUS + BaseProject.SELENIUMPLUS_JAR_PATH), null,
							null);
					IClasspathEntry stafjar = JavaCore.newLibraryEntry(
							new Path(STAFDIR + BaseProject.STAF_JAR_PATH), null, null);
					entries = null;
					entries = new IClasspathEntry[] { src, jre, seleniumjar,
							stafjar };
				}

				javaProject.setRawClasspath(entries, newProject.getFullPath()
						.append("bin"), new NullProgressMonitor());

			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

		return newProject;
	}

	private static void createFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
	}

	/**
	 * Create a folder structure with a parent root, overlay, and a few child
	 * folders.
	 *
	 * @param newProject
	 * @param paths
	 * @throws CoreException
	 */
	private static void addToProjectStructure(IProject newProject,
			String[] paths) throws CoreException, Exception {

		for (String path : paths) {
			IFolder etcFolders = newProject.getFolder(path);
			createFolder(etcFolders);
		}


		/**
		 * Create sample class
		 */
		String testClass = BaseProject.TESTCASECLASS_FILE;
		IFolder testPkg = newProject.getFolder(paths[0]);
		String tmp_pkg = testPkg.toString();
		String[] packageName = tmp_pkg.split(BaseProject.SRC_SRC_DIR+"/");
		String newPackage = packageName[1].replaceAll("/", ".");

		if (testPkg.exists()){

			IFile testclass = testPkg.getFile(testClass + ".java");
			InputStream testclassstream = FileTemplates.testClass(newProject.getName(),newPackage,mapPkg, testClass);
			testclass.create(testclassstream, true, null);
			testclassstream.close();
		}

		/**
		 * Map and Map order files
		 */
		IFolder mapFolder = newProject.getFolder(BaseProject.DATAPOOL_DIR);

		if (mapFolder.exists()) {

			IFile appMap = mapFolder.getFile(newProject.getName()+BaseProject.APPMAP_FILE);
			InputStream mapstream = FileTemplates.appMap();
			appMap.create(mapstream, true, null);
			mapstream.close();

			appMap = mapFolder.getFile(newProject.getName()+BaseProject.APPMAP_EN_FILE);
			mapstream = FileTemplates.appMap();
			appMap.create(mapstream, true, null);
			mapstream.close();

			appMap = mapFolder.getFile(BaseProject.APPMAP_ORDER_FILE);
			mapstream = FileTemplates.appMapOrder(newProject.getName());
			appMap.create(mapstream, true, null);
			mapstream.close();


		}

		/**
		 * create test.ini file
		 */
		IContainer container = mapFolder.getParent();
		IFile iniFile = container.getFile(new Path(BaseProject.TESTINI_FILE));
		InputStream inistream = FileTemplates.testINI(SELENIUM_PLUS,newProject.getName());
		iniFile.create(inistream, true, null);
		inistream.close();
	}

	private static void addNature(IProject project) throws CoreException {
		if (!project.hasNature(ProjectNature.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = ProjectNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		}
	}

}
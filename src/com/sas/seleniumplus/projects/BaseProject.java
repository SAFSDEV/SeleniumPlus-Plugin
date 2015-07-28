package com.sas.seleniumplus.projects;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.builders.AppMapBuilder;
import com.sas.seleniumplus.natures.ProjectNature;
import com.sas.seleniumplus.popupmenu.FileTemplates;

public class BaseProject {

	/** holds path to SeleniumPlus install directory -- once validated. */
	public static String SELENIUM_PLUS;
	/** holds path to STAF install directory -- once validated. */
	public static String STAFDIR;
	
	/** "SELENIUM_PLUS" */
	public static String SELENIUM_PLUS_ENV = "SELENIUM_PLUS";
	/** "STAFDIR" */
	public static String STAFDIR_ENV = "STAFDIR";
	/** "/bin/STAFProc" */
	public static String STAFPROC_PATH = File.separatorChar+"bin"+ File.separatorChar +"STAFProc";
	/** "SAFSDIR" */
	public static String SAFSDIR_ENV = "SAFSDIR";
	
	/** "Tests" */
	public static String SRC_TEST_DIR = "Tests";
	/** "src" */
	public static String SRC_SRC_DIR = "src";
	
	/** "testcases" */
	public static String SRC_TESTCASES_SUBDIR = "testcases";
	/** "testruns" */
	public static String SRC_TESTRUNS_SUBDIR = "testruns";
	/** "tests" */
	public static String SRC_TESTS_SUBDIR = "tests";
	/** "suites" */
	public static String SRC_SUITES_SUBDIR = "suites";

	/** build path jars */
	public final static String SELENIUMPLUS_JAR = "seleniumplus.jar";
	public final static String JSTAF_EMBEDDDED_JAR = "JSTAFEmbedded.jar";
	public final static String SAFSSELENIUM_JAR = "safsselenium.jar"; 
	public final static String STAF_JAR = "JSTAF.jar";
	public final static String SELENIUM_SERVER_JAR_PART_NAME = "selenium-server-standalone";
	
	
	/** "TestCase1" */
	public static String TESTCASECLASS_FILE = "TestCase1";
	/** "/samples/TestCase1.java" */
	public static String TESTCASECLASS_RESOURCE = "/samples/TestCase1.java";
	/** "TestRun1" */
	public static String TESTRUNCLASS_FILE = "TestRun1";
	/** "/samples/TestRun1.java" */
	public static String TESTRUNCLASS_RESOURCE = "/samples/TestRun1.java";

	/** "/libs/seleniumplus.jar" */
	public static String SELENIUMPLUS_JAR_PATH = File.separator + "libs"+ File.separator + SELENIUMPLUS_JAR;
	/** "/lib/safsselenium.jar" */
	public static String SAFSSELENIUM_JAR_PATH = File.separator + "lib"+ File.separator + SAFSSELENIUM_JAR;
	
	/** "/bin/JSTAF.jar" */
	public static String STAF_JAR_PATH = File.separator + "bin"+ File.separator + STAF_JAR;
	
	/** "/libs/JSTAFEmbedded.jar" */
	public static String NOSTAF_JAR_PATH = File.separator + "libs"+ File.separator + JSTAF_EMBEDDDED_JAR;
	
	public static String srcDir;
	public static String testcaseDir;
	public static String testrunDir;

	/** "Maps" */
	public static String DATAPOOL_DIR = "Maps";
	/** "Benchmarks" */
	public static String BENCH_DIR = "Benchmarks";
	/** "Diffs" */
	public static String DIF_DIR = "Diffs";
	/** "Actuals" */
	public static String TEST_DIR = "Actuals";
	/** "Logs" */
	public static String LOGS_DIR = "Logs";
	
	/** "SeleniumProject" */
	public static String PROJECTTYPE_SELENIUM = "SeleniumProject";
	/** "AdvanceProject" */
	public static String PROJECTTYPE_ADVANCE  = "AdvanceProject";
	/** "SampleProject" */
	public static String PROJECTTYPE_SAMPLE   = "SampleProject";

	/** "SAMPLE" */
	public static String PROJECTNAME_SAMPLE   = "SAMPLE";
	
	/** Map */
	public static String MAPCLASS_FILE = "Map";
	/** test.ini */
	public static String TESTINI_FILE = "test.ini";
	/** runAutomation.bat */
	public static String RUNAUTOMATION_WIN_FILE = "runAutomation.bat";
	/** /samples/runautomation.bat */
	public static String RUNAUTOMATION_WIN_RESOURCE = "/samples/runautomation.bat";
	/** runAutomation.sh */
	public static String RUNAUTOMATION_UNX_FILE = "runAutomation.sh";
	/** /samples/runautomation.sh */
	public static String RUNAUTOMATION_UNX_RESOURCE = "/samples/runautomation.sh";
	/** App.map */
	public static String APPMAP_FILE = "App.map";
	/** /samples/App.map */
	public static String APPMAP_RESOURCE = "/samples/App.map";
	/** App_en.map */
	public static String APPMAP_EN_FILE = "App_en.map";
	/** /samples/App_en.map */
	public static String APPMAP_EN_RESOURCE = "/samples/App_en.map";
	/** AppMap.order */
	public static String APPMAP_ORDER_FILE = "AppMap.order";
	/** /samples/AppMap.order */
	public static String APPMAP_ORDER_RESOURCE = "/samples/AppMap.order";
	
	public static String MSG_INSTALL_NOT_FOUND = "SeleniumPlus installation not found";
	public static String MSG_INSTALL_AND_RESTART = " 1. Please install SeleniumPlus.\n" + 
                                                   " 2. Re-run Setup.bat from the SeleniumPlus install directory.\n"+
                                                   " 3. Restart SeleniumPlus.\n";		
	/**
	 * For this marvelous project we need to: - create the default Eclipse
	 * project - add the custom project nature - create the folder structure
	 * 
	 * @param projectName
	 * @param location
	 * @param companyName
	 * @param projectType {@value #PROJECTTYPE_SAMPLE}, {@value #PROJECTTYPE_SELENIUM}, {@value #PROJECTTYPE_ADVANCE}
	 * @return
	 */
	public static IProject createProject(String projectName, URI location, String companyName, String projectType) {
		Assert.isNotNull(projectName);
		Assert.isNotNull(companyName);
		Assert.isTrue(projectName.trim().length() > 0);

		if (projectType.equalsIgnoreCase(PROJECTTYPE_SELENIUM) || 
			projectType.equalsIgnoreCase(PROJECTTYPE_SAMPLE)){
			
			srcDir = SRC_TEST_DIR;
			testcaseDir = srcDir + "/"+ projectName.toLowerCase() +"/"+ SRC_TESTCASES_SUBDIR;
			testrunDir =  srcDir + "/"+ projectName.toLowerCase() +"/"+ SRC_TESTRUNS_SUBDIR;

		} else if (projectType.equalsIgnoreCase(PROJECTTYPE_ADVANCE)){
			
			srcDir = SRC_SRC_DIR;
			testcaseDir = srcDir + "/com/" + companyName.toLowerCase() + "/"+ projectName.toLowerCase()+ "/"+ SRC_TESTS_SUBDIR;
			testrunDir = srcDir + "/com/" + companyName.toLowerCase() + "/"+ projectName.toLowerCase()+ "/"+ SRC_SUITES_SUBDIR;
					
		} else {
			// internal error
		}
				
		IProject project = createBaseProject(projectName,srcDir,location);
				
		try {
			
			addNature(project);

			String[] paths = {
					srcDir,
					testcaseDir,
					testrunDir,					
					DATAPOOL_DIR,
					TEST_DIR, 
					BENCH_DIR, 
					DIF_DIR, 
					LOGS_DIR
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
	 * @param projectName
	 * @param sourceDirName
	 * @param location
	 */
	private static IProject createBaseProject(String projectName, String sourceDirName, URI location) {

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
				IFolder srcFolder = newProject.getFolder(sourceDirName);
			
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
					IClasspathEntry seleniumjar = JavaCore.newVariableEntry(new Path(Activator.SELENIUMPLUS_HOME + SELENIUMPLUS_JAR_PATH), null, null);
					IClasspathEntry seleniumServerjar = JavaCore.newVariableEntry(new Path(Activator.SELENIUMPLUS_HOME + "/libs/" + findLatestJar().getName()), null, null);
					IClasspathEntry stafjar = null;
					IClasspathEntry nostafjar = null;
					
					try{ stafjar = JavaCore.newLibraryEntry(
							new Path(STAFDIR + STAF_JAR_PATH), null, null);}
					catch(Exception x){}
					
					try{ nostafjar = JavaCore.newVariableEntry(
							new Path(Activator.SELENIUMPLUS_HOME + NOSTAF_JAR_PATH), null, null);}
					catch(Exception x){}
					entries = null;
					if(nostafjar == null){
						entries = new IClasspathEntry[] { src, jre, seleniumjar, seleniumServerjar, stafjar };
					}else{
						entries = new IClasspathEntry[] { src, jre, seleniumjar, seleniumServerjar, nostafjar };
					}
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
		 * Create sample test class
		*/
		String testClass = TESTCASECLASS_FILE;
		IFolder testPkg = newProject.getFolder(paths[1]);
		String tmp_pkg = testPkg.toString();
		String packagedir = tmp_pkg.substring(tmp_pkg.indexOf(paths[0]) + paths[0].length() + 1 , tmp_pkg.length());
		String prjPackage = packagedir;
		try{ prjPackage = packagedir.substring(0, packagedir.lastIndexOf("/"));}catch(Exception ignore){}
		String newPackage = packagedir.replaceAll("/", ".");
		String mapPackage = prjPackage.replaceAll("/",  ".");
		String mapPkg = mapPackage + "."+ MAPCLASS_FILE;
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
				
		if (testPkg.exists()){			
			IFile testclass = testPkg.getFile(testClass + ".java");
			InputStream testclassstream = null;
			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){				
				testclassstream = loader.getResourceAsStream(TESTCASECLASS_RESOURCE);
			} else {
				testclassstream = FileTemplates.testClass(newProject.getName(),newPackage,mapPkg, testClass);				
			}
			
			testclass.create(testclassstream, true, null);
			testclassstream.close();			
		}		
		
		
		/**
		 * Create run tests
		 */
		String testRunClass = TESTRUNCLASS_FILE;
		testPkg = newProject.getFolder(paths[2]);
		tmp_pkg = testPkg.toString();
		packagedir = tmp_pkg.substring(tmp_pkg.indexOf(paths[0]) + paths[0].length() + 1 , tmp_pkg.length());
		prjPackage = packagedir;
		try{ prjPackage = packagedir.substring(0, packagedir.lastIndexOf("/"));}catch(Exception ignore){}
		newPackage = packagedir.replaceAll("/", ".");
		mapPackage = prjPackage.replaceAll("/",  ".");
		mapPkg = mapPackage + "."+ MAPCLASS_FILE;
		
		
		if (testPkg.exists()){			
			IFile testruns = testPkg.getFile(testRunClass + ".java");
			InputStream testrunstream = null;
			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){				
				testrunstream =loader.getResourceAsStream(TESTRUNCLASS_RESOURCE);
			} else {
				testrunstream = FileTemplates.testRunClass(newProject.getName(),newPackage,mapPkg, testRunClass);
			}
						
			testruns.create(testrunstream, true, null);
			testrunstream.close();
		}		
		
		
		
		/**
		 * Map and Map order files
		 */
		IFolder mapFolder = newProject.getFolder(DATAPOOL_DIR);
		
		if (mapFolder.exists()) {
			
			if (newProject.getName().equalsIgnoreCase(PROJECTNAME_SAMPLE)){	
				
				IFile appMap = mapFolder.getFile(newProject.getName()+APPMAP_FILE);
				//InputStream mapstream = BaseProject.class.getResourceAsStream("../../../../samples/App.map");
				InputStream mapstream = loader.getResourceAsStream(APPMAP_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();
				
				appMap = mapFolder.getFile(newProject.getName()+APPMAP_EN_FILE);
				//mapstream = BaseProject.class.getResourceAsStream("../../../../samples/App_zh.map");
				mapstream = loader.getResourceAsStream(APPMAP_EN_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();
	
				appMap = mapFolder.getFile(APPMAP_ORDER_FILE);
				//mapstream = BaseProject.class.getResourceAsStream("../../../../samples/AppMap.order");
				mapstream = loader.getResourceAsStream(APPMAP_ORDER_RESOURCE);
				appMap.create(mapstream, true, null);
				if (mapstream != null) mapstream.close();			
				
			} else {
			
				IFile appMap = mapFolder.getFile(newProject.getName()+APPMAP_FILE);
				InputStream mapstream = FileTemplates.appMap();
				appMap.create(mapstream, true, null);
				mapstream.close();
	
				appMap = mapFolder.getFile(newProject.getName()+APPMAP_EN_FILE);
				mapstream = FileTemplates.appMap();
				appMap.create(mapstream, true, null);
				mapstream.close();
	
				appMap = mapFolder.getFile(APPMAP_ORDER_FILE);
				mapstream = FileTemplates.appMapOrder(newProject.getName());
				appMap.create(mapstream, true, null);
				mapstream.close();			
			}
		}
		
		/**
		 * create test.ini file
		 */
		IContainer container = mapFolder.getParent();
		IFile iniFile = container.getFile(new Path(TESTINI_FILE));		
		InputStream inistream = FileTemplates.testINI(SELENIUM_PLUS,newProject.getName());
		iniFile.create(inistream, true, null);
		inistream.close();
		
		/**
		 * create commandline bat file
		 */
		// TODO WIN and NIX versions of scripts
		boolean isWin = true; 
		IFile batfile =  null;
		InputStream batstream = null;
		
		if(isWin){
			batfile = container.getFile(new Path(RUNAUTOMATION_WIN_FILE));
			batstream = loader.getResourceAsStream(RUNAUTOMATION_WIN_RESOURCE);
		}
		if (batstream != null) {
			batfile.create(batstream, true, null);
			batstream.close();	
		}
		
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
	
	private static File findLatestJar(){
		
		File rootdir = new CaseInsensitiveFile(SELENIUM_PLUS).toFile();
		File libsdir = new CaseInsensitiveFile(rootdir, "libs").toFile();
		
		File[] files = libsdir.listFiles(new FilenameFilter(){ public boolean accept(File dir, String name){
			try{ return name.toLowerCase().startsWith(SELENIUM_SERVER_JAR_PART_NAME);}catch(Exception x){ return false;}
		}});
		
		File jarfile = null;
		
		// if more than one, find the latest
		if(files.length > 1){
			long diftime = 0;
			for(File afile: files){
				if(afile.lastModified() > diftime){
					diftime = afile.lastModified();
					jarfile = afile;
				}
			}
		}else{
			jarfile = files[0];			
		}
		
		return jarfile;
	}

}
/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package com.sas.seleniumplus.projects;
/**
 * APR 26, 2017	(LeiWang) Modified createBaseProject(): Use CommonLib.getLatestSeleniumPlusJARS().
 * MAY 10, 2018	(LeiWang) Refactored code to easily add file (test source, map, .ini etc.) to the project.
 * NOV 23, 2018	(LeiWang) Added method addLog4jConfigFile(): create log4j config file for creating project.
 *
 */
import java.io.File;
import java.io.IOException;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.safs.Constants;
import org.safs.android.auto.lib.Console;
import org.safs.projects.common.projects.callbacks.Callbacks;
import org.safs.projects.seleniumplus.popupmenu.FileTemplates.IniFileType;
import org.safs.projects.seleniumplus.popupmenu.FileTemplates.MapFileType;
import org.safs.projects.seleniumplus.popupmenu.FileTemplates.TestFileType;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;
import com.sas.seleniumplus.builders.AppMapBuilder;
import com.sas.seleniumplus.eclipse.EclipseCallbacks;
import com.sas.seleniumplus.eclipse.IContainerHolder;
import com.sas.seleniumplus.eclipse.IFolderHolder;
import com.sas.seleniumplus.eclipse.IProjectHolder;
import com.sas.seleniumplus.natures.ProjectNature;
import com.sas.seleniumplus.popupmenu.FileTemplates;

public class BaseProject extends org.safs.projects.seleniumplus.projects.BaseProject {

	/** holds path to STAF install directory -- once validated. */
	public static String STAFDIR;

	/** "STAFDIR" */
	public final static String STAFDIR_ENV = "STAFDIR";
	/** "/bin/STAFProc" */
	public final static String STAFPROC_PATH = File.separatorChar+"bin"+ File.separatorChar +"STAFProc";
	/** "SAFSDIR" */
	public final static String SAFSDIR_ENV = "SAFSDIR";

	/** "tests" */
	public final static String SRC_TESTS_SUBDIR = "tests";
	/** "suites" */
	public final static String SRC_SUITES_SUBDIR = "suites";

	/** build path jars */
	public final static String SELENIUMPLUS_JAR = "seleniumplus.jar";
	public final static String JSTAF_EMBEDDDED_JAR = "JSTAFEmbedded.jar";
	public final static String SAFSSELENIUM_JAR = "safsselenium.jar";
	public final static String STAF_JAR = "JSTAF.jar";
	public final static String SELENIUM_SERVER_JAR_PART_NAME = "selenium-server-standalone";
	/** build path attachment source zip*/
	public final static String SAFSSELENIUM_PLUS_SOURCE_CORE = "source_core.zip";

	/** "/libs/seleniumplus.jar" */
	public final static String SELENIUMPLUS_JAR_PATH = File.separator + "libs"+ File.separator + SELENIUMPLUS_JAR;
	/** "/lib/safsselenium.jar" */
	public final static String SAFSSELENIUM_JAR_PATH = File.separator + "lib"+ File.separator + SAFSSELENIUM_JAR;

	/** "/bin/JSTAF.jar" */
	public final static String STAF_JAR_PATH = File.separator + "bin"+ File.separator + STAF_JAR;

	/** "/libs/JSTAFEmbedded.jar" */
	public final static String NOSTAF_JAR_PATH = File.separator + "libs"+ File.separator + JSTAF_EMBEDDDED_JAR;

	/** "SeleniumProjectWithTestLevel" */
	public final static String PROJECTTYPE_TESTLEVEL = "SeleniumProjectWithTestLevel";
	/** "SeleniumProject" */
	public final static String PROJECTTYPE_SELENIUM = "SeleniumProject";
	/** "AdvanceProject" */
	public final static String PROJECTTYPE_ADVANCE  = "AdvanceProject";
	/** runAutomation.sh */
	public final static String RUNAUTOMATION_UNX_FILE = "runAutomation.sh";
	/** /samples/runautomation.sh */
	public final static String RUNAUTOMATION_UNX_RESOURCE = "/samples/runautomation.sh";

	public final static String MSG_INSTALL_NOT_FOUND = "SeleniumPlus installation not found";
	public final static String MSG_INSTALL_AND_RESTART = " 1. Please install SeleniumPlus.\n" +
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

		String testrunPackage = null;
		String testcyclePackage = null;
		String testsuitePackage = null;
		String testcasePackage = null;

		String testBasePackage = null;
		if (projectType.equalsIgnoreCase(PROJECTTYPE_SAMPLE)) {
			Callbacks callbacks = new EclipseCallbacks(projectName, location);
			IProjectHolder projectHolder = (IProjectHolder) org.safs.projects.seleniumplus.projects.BaseProject.createProject(projectName, location, companyName, projectType, callbacks);
			return projectHolder.getIProject();

		} else if (projectType.equalsIgnoreCase(PROJECTTYPE_SELENIUM)){

			srcDir = SRC_TEST_DIR;
			testBasePackage = projectName.toLowerCase();
			testcasePackage = testBasePackage +"."+ SRC_TESTCASES_SUBDIR;
			testrunPackage =  testBasePackage +"."+ SRC_TESTRUNS_SUBDIR;

		} else if (projectType.equalsIgnoreCase(PROJECTTYPE_ADVANCE)){

			srcDir = SRC_SRC_DIR;
			testBasePackage = "com." + companyName.toLowerCase() + "."+ projectName.toLowerCase();
			testcasePackage = testBasePackage + "."+ SRC_TESTS_SUBDIR;
			testrunPackage =  testBasePackage + "."+ SRC_SUITES_SUBDIR;

		}else if (projectType.equalsIgnoreCase(PROJECTTYPE_TESTLEVEL)){

			srcDir = SRC_TEST_DIR;
			testBasePackage = projectName.toLowerCase();
			testcyclePackage =  testBasePackage +"."+ SRC_TESTCYCLE_SUBDIR;
			testsuitePackage =  testBasePackage +"."+ SRC_TESTSUITE_SUBDIR;
			testcasePackage = testBasePackage +"."+ SRC_TESTCASE_SUBDIR;

		} else {
			// internal error
			throw new RuntimeException("Unsupported Project type '"+projectType+"'!");
		}

		IProject project = createBaseProject(projectName, srcDir, location);

		try {

			addNature(project);

			/** Create test classes */
			if(testcyclePackage!=null){//create hierarchical structure (cycle, suite, testcases)
				/** Create test cycle classes */
				addTestLevelClass(project.getFolder(FileTemplates.toProjectPath(srcDir, testcyclePackage)), TestFileType.TestCycle, testcyclePackage+"."+TESTCYCLE_FILE, MAPCLASS_FILE, testsuitePackage+"."+TESTSUITE_FILE);
				/** Create test suite classes */
				addTestLevelClass(project.getFolder(FileTemplates.toProjectPath(srcDir, testsuitePackage)), TestFileType.TestSuite, testsuitePackage+"."+TESTSUITE_FILE, MAPCLASS_FILE, testcasePackage+"."+TESTCASE_FILE);
				/** Create test test case classes */
				addTestLevelClass(project.getFolder(FileTemplates.toProjectPath(srcDir, testcasePackage)), TestFileType.TestCase, testcasePackage+"."+TESTCASE_FILE, MAPCLASS_FILE, null);

				/** Create spring configuration file */
				addSpringConfigFile(project.getFolder(srcDir), testBasePackage);
			}else if(testrunPackage!=null){//create hierarchical structure (testrun, testcases)
				/** Create test run classes */
				addTestLevelClass(project.getFolder(FileTemplates.toProjectPath(srcDir, testrunPackage)), TestFileType.TestRunClass, testrunPackage+"."+TESTRUNCLASS_FILE, MAPCLASS_FILE, testcasePackage+"."+TESTCASECLASS_FILE);
				/** Create test case classes */
				addTestLevelClass(project.getFolder(FileTemplates.toProjectPath(srcDir, testcasePackage)), TestFileType.TestClass, testcasePackage+"."+TESTCASECLASS_FILE, MAPCLASS_FILE, null);
			}

			/**  Map and Map order files */
			IFolder mapFolder = project.getFolder(DATAPOOL_DIR);
			addMapFile(mapFolder, MapFileType.Map);
			addMapFile(mapFolder, MapFileType.MapEn);
			addMapFile(mapFolder, MapFileType.Order);

			/** create Actual/Bench/Diff/Logs folder */
			createFolder(project.getFolder(TEST_DIR));
			createFolder(project.getFolder(BENCH_DIR));
			createFolder(project.getFolder(DIF_DIR));
			createFolder(project.getFolder(LOGS_DIR));

			/** create test.ini file */
			addIniFile(project, IniFileType.Normal);

			/** create command-line script file runAutomation.bat/runAutomation.sh */
			addStartScriptFile(project);

			/** Create log4j configuration file */
			addLog4jConfigFile(project.getFolder(srcDir));

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
	public static IProject createBaseProject(String projectName, String sourceDirName, URI location) {

		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

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
				org.eclipse.jdt.core.IClasspathEntry src = JavaCore.newSourceEntry(srcFolder.getFullPath());
				IClasspathEntry jre = JavaCore.newContainerEntry(
						new Path(JavaRuntime.JRE_CONTAINER),
						new IAccessRule[0],
						new IClasspathAttribute[] { JavaCore.newClasspathAttribute("owner.project.facets","java") },
						false);
				IClasspathEntry[] entries = new IClasspathEntry[] { src, jre };

				if (SELENIUM_PLUS != null) {
					try {
						IClasspathEntry[] seplusEntries = CommonLib.getLatestSeleniumPlusJARS();
						IClasspathEntry[] totalEntries = new IClasspathEntry[entries.length+seplusEntries.length];
						System.arraycopy(entries, 0, totalEntries, 0, entries.length);
						System.arraycopy(seplusEntries, 0, totalEntries, entries.length, seplusEntries.length);
						entries = totalEntries;
					} catch (ExecutionException e) {
						Activator.warn("Failed to append SeleniumPlus specific Classpath Entries, the proejct '"+projectName+"' risks failing to build. Due to "+e.toString());
					}
				}

				javaProject.setRawClasspath(entries, newProject.getFullPath().append("bin"), new NullProgressMonitor());

			} catch (CoreException e) {
				Activator.error("Failed to create SeleniumPlus project '"+projectName+"', due to "+e.toString());
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

	public static void createFolder(IFolderHolder folder) throws CoreException {
		IContainerHolder parent = (IContainerHolder) folder.getParent();
		if (parent.getIContainer() instanceof IFolder) {
			IFolderHolder holder = new IFolderHolder((IFolder) parent.getIContainer());
			createFolder(holder);
		}
		IFolder ifolder = folder.getIFolder();
		if (!ifolder.exists()) {
			ifolder.create(false, true, null);
		}
	}

	private static void addFile(IContainer container, IFile file, InputStream stream) throws CoreException, IOException {
		if(!container.exists() && (container instanceof IFolder)){
			createFolder((IFolder) container);
		}

		if (container.exists()){
			file.create(stream, true, null);
			if (stream != null) stream.close();
		}
	}

	/**
	 * Add test class java file to the test folder of certain test level.<br>
	 * We suppose the test class file has the format as "&lt;srcFolderName>/&lt;projectname>/&lt;testlevel>/&lt;testClassName>.java".<br>
	 *
	 * @param folder IFolder, the folder holding test classes of certain level (cycle, suite, testcase)
	 * @param fileType TestFileType, the test class file type.
	 * @param testClassName String, the full qualified name of the test class
	 * @param mapName String, the simple (without package part) name of the map file.
	 * @param childLevelTestClassName String, the full qualified name of the test class of child level. It is null if there is no child level.
	 * @throws CoreException
	 * @throws IOException
	 */
	private static void addTestLevelClass(IFolder folder, TestFileType fileType, String testClassName, String mapName, String childLevelTestClassName) throws CoreException, IOException{
		IProject project = folder.getProject();
		String[] testPackageAndClassname = FileTemplates.splitClassName(testClassName);

		//create the full Map class name. The mapPackage is the parent package of the testPackage.
		String mapPackage = FileTemplates.splitClassName(testPackageAndClassname[0]/* testPackage */)[0];
		String mapClassName = mapPackage.isEmpty()? mapName : mapPackage + "."+ mapName;

		IFile testclass = folder.getFile(testPackageAndClassname[1] /* simpleTestClassname */ + ".java");
		InputStream testclassstream = FileTemplates.getTestLevelClass(project.getName(), fileType, testClassName, mapClassName, childLevelTestClassName);

		addFile(folder, testclass, testclassstream);
	}

	/**
	 * Add map/order file to the map folder.
	 * @param folder IFolder, the folder holding map/order files.
	 * @param fileType MapFileType, the map file type.
	 * @throws CoreException
	 * @throws IOException
	 */
	private static void addMapFile(IFolder folder, MapFileType fileType) throws CoreException, IOException {

		IFile file = null;
		String projectName = folder.getProject().getName();
		InputStream mapstream = FileTemplates.getAppMap(projectName, fileType);

		if(MapFileType.Map.equals(fileType)){
			file = folder.getFile(projectName+APPMAP_FILE);
		}else if(MapFileType.MapEn.equals(fileType)){
			file = folder.getFile(projectName+APPMAP_EN_FILE);
		}else if(MapFileType.Order.equals(fileType)){
			file = folder.getFile(APPMAP_ORDER_FILE);
		}

		addFile(folder, file, mapstream);
	}

	/**
	 * Add .ini configuration file. It is under the project folder.
	 * @param project IProject, the project holding the .ini file.
	 * @param fileType IniFileType, the ini file type
	 * @throws CoreException
	 * @throws IOException
	 */
	private static void addIniFile(IProject project , IniFileType fileType) throws CoreException, IOException {
		IFile iniFile = project.getFile(new Path(TESTINI_FILE));
		InputStream stream = FileTemplates.testINI(SELENIUM_PLUS, project.getName());

		addFile(project, iniFile, stream);
	}

	/**
	 * Add the 'start script', such as runAutomation.bat. It is normally under the project folder.
	 * @param folder IContainer, the container holding the 'start script'.
	 * @throws CoreException
	 * @throws IOException
	 */
	private static void addStartScriptFile(IContainer folder) throws CoreException, IOException {
		IFile batfile =  null;
		InputStream batstream = null;

		if(Console.isWindowsOS()){
			batfile = folder.getFile(new Path(RUNAUTOMATION_WIN_FILE));
			batstream = Thread.currentThread().getContextClassLoader().getResourceAsStream(RUNAUTOMATION_WIN_RESOURCE);
		}else{
			throw new RuntimeException("Not supported OS: '"+Console.getOsFamilyName()+"'!");
		}

		addFile(folder, batfile, batstream);
	}

	/**
	 * Add spring configuration file. It should be under the source folder.
	 * @param srcFolder IFolder, the 'src' folder  holding the spring configuration file.
	 * @throws CoreException
	 * @throws IOException
	 */
	private static void addSpringConfigFile(IFolder srcFolder, String testBasePackage) throws CoreException, IOException {

		IFile file = srcFolder.getFile(new Path(Constants.SPRING_CONFIG_CUSTOM_FILE));
		InputStream stream = FileTemplates.springConfig(testBasePackage);

		addFile(srcFolder, file, stream);
	}

	/**
	 * Add log4j configuration file. It should be under the source folder.
	 * @param srcFolder IFolder, the 'src' folder  holding the spring configuration file.
	 * @throws CoreException
	 * @throws IOException
	 */
	private static void addLog4jConfigFile(IFolder srcFolder) throws CoreException, IOException {
		IFile file = srcFolder.getFile(new Path(Constants.LOG4J2_CONFIG_FILE));
		if(!file.exists()){
			InputStream stream = FileTemplates.log4j2Config();
			addFile(srcFolder, file, stream);
		}else{
			Activator.log("Log4j config file "+file.getName()+" has alreday existed!");
		}
	}

	/**
	 * Update the project to add the log4j configuration file.
	 * @param project
	 */
	public static void refreshLog4jConfigFile(IProject project){
		IFolder srcFolder = null;

		for(String src:SRC_SRC_DIRS){
			srcFolder = project.getFolder(src);
			if(srcFolder.exists()) break;
		}
		if(!srcFolder.exists()){
			Activator.log("Cannot detect the project's source folder!");
			return;
		}

		try {
			addLog4jConfigFile(srcFolder);
		} catch (Exception e) {
			Activator.error("Cannot update log4j config file, Met "+e.toString());
		}
	}

	public static void addNature(IProject project) throws CoreException {
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

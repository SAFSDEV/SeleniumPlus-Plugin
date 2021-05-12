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
package com.sas.seleniumplus;
/**
 * Developer logs:
 * APR 20, 2017	(LeiWang)	Load custom-resource-bundle before trying the default one.
 * MAY 18, 2018	(LeiWang)	Added methods to get .ini ConfigureInterface of a project.
 *                          Added methods to Initialize/showe 'Selenium Plus Console'.
 *                          Created 'out' as output stream to 'Selenium Plus Console'.
 *                          Created 'err' as error output stream to 'Selenium Plus Console'.
 * MAY 21, 2018	(LeiWang)	Used try-catch block to enclose the code for creating Console, Console's stream and Console's Color to
 *                          avoid the problems (such as "Cannot load 64-bit SWT libraries on 32-bit JVM") met during the plugin-test.
 * FEB 26, 2019	(LeiWang)	Added checkDefaultINI(): Adjust %SELENIUM_PLUS%/extra/automation/safstid.ini according to plugin settings.
 *                                                   Add entries for section [SAFS_DIRECTORIES] if they are not present in %SELENIUM_PLUS%/extra/automation/safstid.ini
 */
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.safs.StringUtils;
import org.safs.text.INIFileReadWrite;
import org.safs.text.INIFileReader;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.ConfigureLocatorInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.DataServiceConstant;
import org.safs.tools.drivers.DriverConstant.SafsDirectories;

import com.sas.seleniumplus.natures.ProjectNature;
import com.sas.seleniumplus.preferences.PreferenceConstants;
import com.sas.seleniumplus.projects.BaseProject;
import com.sas.seleniumplus.projects.ProjectAddListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements org.eclipse.ui.IStartup {

	/** The plug-in ID "SeleniumPlus" */
	public static final String PLUGIN_ID = "SeleniumPlus";

	/** Classpath Variable name "SELENIUMPLUS_HOME", this variable is defined for Eclipse */
	public static final String SELENIUMPLUS_HOME = "SELENIUMPLUS_HOME";

	/** The shared Plugin instance */
	private static Activator plugin;

	/** The shared Preference ResourceBundle instance */
	private static ResourceBundle preferences = null;

	/** The SeleniumPlus home directory got from environment {@link BaseProject#SELENIUM_PLUS_ENV} */
	public static final String seleniumhome = System.getenv(BaseProject.SELENIUM_PLUS_ENV);

	/**
	 * The default constructor
	 */
	public Activator() {
		super();
		if(seleniumhome == null) throw new IllegalStateException("SELENIUM_PLUS System Environment '"+BaseProject.SELENIUM_PLUS_ENV+"' was not set!");
	}

	/**
	 * @see org.eclipse.ui.IStartup
	 */
	@Override
	public void earlyStartup() {
		try{
			checkSeleniumPlusClasspath();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(ProjectAddListener.LISTENER, IResourceChangeEvent.POST_CHANGE);
		}catch(Exception x){
			Activator.log("Ignoring "+ x.getClass().getName()+", "+x.getMessage());
		}
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		initResourceBundle();
		checkDefaultINI();
	}

	/**
	 * Modify the SAFSTID.INI located in the Se+ installation directory at ./extra/automation/safstid.ini.
	 * <ol>
	 * <li>That INI file should be modified to provide the default SAFS Data Service configuration
	 *     information as found in the Se+ PlugIn preferences.properties file.
	 *     The ./extra/automation/safstid.ini is served as the default configuration, but the project's configuration
	 *     file test.ini has the higher priority, it overrides ./extra/automation/safstid.ini.
	 * <li>Add the section [SAFS_DIRECTORIES] and entries as below:
	 *     <pre>
	 *     [SAFS_DIRECTORIES]
	 *     TESTDIR=Actuals
	 *     DIFFDIR=Diffs
	 *     BENCHDIR=Benchmarks
	 *     LOGDIR=Logs
	 *     DATADIR=Maps
	 *     </pre>
	 * </ol>
	 */
	private void checkDefaultINI(){
		String debugmsg = StringUtils.debugmsg(false);

		INIFileReadWrite iniFile = null;

		try{
			iniFile = new INIFileReadWrite(new File(seleniumhome+PreferenceConstants.EXTRA_AUTOMATION_FOLDER, "safstid.ini"), INIFileReader.IFR_MEMORY_MODE_STORED);

			//get the "SAFS Data Service URL" from the preference, and use this value to update the default configuration file
			String dataServiceURL = getResource(PreferenceConstants.SAFS_DATA_SERVICE_URL+PreferenceConstants.SUFFIX_DEFAULT);
			String actualValue = null;
			boolean rewriteINI = false;
			if(StringUtils.isValid(dataServiceURL)){
				actualValue = iniFile.getAppMapItem(DataServiceConstant.SECTION_NAME, DataServiceConstant.ITEM_SERVER_URL);
				if(!dataServiceURL.equals(actualValue)){
					log(debugmsg+" Replacing the "+DataServiceConstant.ITEM_SERVER_URL+"'s value from "+actualValue+" to "+dataServiceURL);
					iniFile.setAppMapItem(DataServiceConstant.SECTION_NAME, DataServiceConstant.ITEM_SERVER_URL, dataServiceURL);
					rewriteINI = true;
				}
			}

			//Add the [SAFS_DIRECTORIES] entries if they are not present in the default configuration file.
			actualValue = iniFile.getAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_BENCHDIR);
			if(!StringUtils.isValid(actualValue)){
				iniFile.setAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_BENCHDIR, SafsDirectories.DEFAULT_BENCHDIR);
				rewriteINI = true;
			}
			actualValue = iniFile.getAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_DATADIR);
			if(!StringUtils.isValid(actualValue)){
				iniFile.setAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_DATADIR, SafsDirectories.DEFAULT_DATADIR);
				rewriteINI = true;
			}
			actualValue = iniFile.getAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_DIFFDIR);
			if(!StringUtils.isValid(actualValue)){
				iniFile.setAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_DIFFDIR, SafsDirectories.DEFAULT_DIFFDIR);
				rewriteINI = true;
			}
			actualValue = iniFile.getAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_LOGDIR);
			if(!StringUtils.isValid(actualValue)){
				iniFile.setAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_LOGDIR, SafsDirectories.DEFAULT_LOGDIR);
				rewriteINI = true;
			}
			actualValue = iniFile.getAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_TESTDIR);
			if(!StringUtils.isValid(actualValue)){
				iniFile.setAppMapItem(SafsDirectories.SECTION_NAME, SafsDirectories.ITEM_TESTDIR, SafsDirectories.DEFAULT_TESTDIR);
				rewriteINI = true;
			}

			if(rewriteINI){
				iniFile.writeINIFile(null);
			}

		}finally{
			if(iniFile!=null){
				try{ iniFile.close(); }catch(Exception e){}
			}
		}

	}

	/**
	 * Check if our Workspace has a SELENIUMPLUS_HOME Classpath Variable.
	 * Attempt to validate it and create it if necessary.
	 * @throws Exception if a problem exists or we do NOT detect that SeleniumPlus is properly installed.
	 */
	private void checkSeleniumPlusClasspath() throws Exception{
		File dir = null;
		IPath home = JavaCore.getClasspathVariable(SELENIUMPLUS_HOME);
		if(home != null){
			dir = home.toFile();
			if(dir.isDirectory()) return;
		}
		Activator.log("Activator.checkSeleniumPlusClasspath() attempting to fix Classpath Variable SELENIUMPLUS_HOME.");
		if(seleniumhome == null) throw new IllegalStateException("SELENIUM_PLUS Environment does NOT seem to be installed on the system!");
		dir = new CaseInsensitiveFile(seleniumhome).toFile();
		if(! dir.isDirectory()) throw new IllegalStateException("SELENIUM_PLUS Environment does NOT point to a valid directory!");
		Path path = new Path(dir.getCanonicalPath());
		JavaCore.setClasspathVariable(SELENIUMPLUS_HOME, path, null);
		Activator.log("Activator.checkSeleniumPlusClasspath() set Classpath Variable SELENIUMPLUS_HOME to: "+ path.toPortableString());
		Activator.log("Activator.checkSeleniumPlusClasspath() forcing Refresh on all Projects...");
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject iP : projects) {
			if(iP.hasNature(ProjectNature.NATURE_ID)){
				iP.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(ProjectAddListener.LISTENER);
		super.stop(context);
	}

	/**
	 * This method will initialize the 'resource bundle'.<br/>
	 * It is strongly suggested to call it after method {@link #start(BundleContext)}<br/>
	 */
	public final static void initResourceBundle(){
		//We should firstly try to load the resource bundle from the %SELENIUM_PLUS%\eclipse\configuration\com.sas.seleniumplus\preferences.properties
		File configFolder = new File(seleniumhome+PreferenceConstants.RESOURCE_BUNDLE_CUSTOM_FOLDER);
		ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
//		ResourceBundle.Control control = new ResourceBundle.Control(){
//			public long getTimeToLive(String baseName, Locale locale){
//				return 0;
//			}
//			public boolean needsReload(String baseName,
//                    Locale locale,
//                    String format,
//                    ClassLoader loader,
//                    ResourceBundle bundle,
//                    long loadTime){
//				return true;
//			}
//		};
		try {
			if(configFolder.exists()){
				URL[] urls = new URL[1];
				urls[0] = configFolder.toURI().toURL();
				ClassLoader loader = new URLClassLoader(urls);
				preferences = ResourceBundle.getBundle(PreferenceConstants.RESOURCE_BUNDLE_PREFERENCES, Locale.getDefault(), loader, control );
				Activator.log("Loaded custom resource bundle '"+PreferenceConstants.RESOURCE_BUNDLE_PREFERENCES+"' from '"+configFolder.getAbsolutePath()+"'");
			}
		} catch (Exception ex){
			Activator.warn("Failed to load resource bundle '"+PreferenceConstants.RESOURCE_BUNDLE_PREFERENCES+"' from "+configFolder.getAbsolutePath()+", due to "+ex.toString());
		}
		//If we cannot load the custom resource bundle, then try to load the default resource bundle
		if(preferences==null){
			try{
				preferences = ResourceBundle.getBundle(PreferenceConstants.RESOURCE_BUNDLE_PREFERENCES, control);
				Activator.log("Loaded default resource bundle '"+PreferenceConstants.RESOURCE_BUNDLE_PREFERENCES+"'");
			}catch(Exception e){
				Activator.error("Failed to load resource bundle '"+PreferenceConstants.RESOURCE_BUNDLE_PREFERENCES+"', due to "+e.toString());
			}
		}
		if(preferences==null){
			throw new IllegalStateException("Failed to load resource bundle '"+PreferenceConstants.RESOURCE_BUNDLE_PREFERENCES+"'!");
		}
	}

	/**
	 * Get the preference's value from the resource bundle 'preferences'.<br/>
	 * <b>Note:</b><br/>
	 * 1. This should be called after the invocation of {@link #startup()}, inside which the method {@link #initResourceBundle()} is called.<br/>
	 * 2. User needs to catch all the RuntimeExceptions himself.<br/>
	 * @param preferenceKey String, the preference key in the resource bundle properties file
	 * @return String, the preference default value
	 */
	public static String getResource(String preferenceKey){
		String value = preferences.getString(preferenceKey);
		return value.trim();
	}

	/**
	 * Returns the shared Activator instance, it is also a Plugin object.
	 *
	 * @return Activator, the shared Activator instance; or null if this Activator has not been properly initialized.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Provide Eclipse-based debug logging for the PlugIn.
	 * This will log INFO level messages to the Eclipse PlugIn log.
	 * @param msg
	 * @param e
	 */
	public static void log(String msg, Exception e){
		try{
			getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, Status.INFO, msg, e));
		}catch(Exception ex){
			System.out.println(PLUGIN_ID+":INFO: "+msg+", "+e);
		}
	}
	public static void error(String msg, Exception e){
		try{
			getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, Status.INFO, msg, e));
		}catch(Exception ex){
			System.err.println(PLUGIN_ID+":ERROR: "+msg+", "+e);
		}
	}
	public static void warn(String msg, Exception e){
		try{
			getDefault().getLog().log(new Status(Status.WARNING, PLUGIN_ID, Status.INFO, msg, e));
		}catch(Exception ex){
			System.out.println(PLUGIN_ID+":WARN: "+msg+", "+e);
		}
	}
	/**
	 * Simply calls log(msg, null)
	 * @param msg
	 * @see #log(String, Exception)
	 */
	public static void log(String msg){
		log(msg, null);
	}
	public static void error(String msg){
		error(msg, null);
	}
	public static void warn(String msg){
		warn(msg, null);
	}

	/**
	 * Attempt to locate a Source root folder.<br>
	 * Currently we seek a 'src' or 'Tests' folder root in the Project.
	 * @param project IProject. If null, we will attempt to deduce the Project.
	 * @return IFolder or null if not determined.
	 * @throws ExecutionException
	 * @see Activator#getSelectedProject(ISelectionService)
	 */
	public static IFolder getRootSourceFolder(IProject project) throws ExecutionException{
		if(project == null) project = getSelectedProject(null);
		if(project == null) return null;
		IFolder srcfolder = project.getFolder("src");
		if(srcfolder == null || !srcfolder.exists()) {
			Activator.log("Activator.getRootSourceFolder did not detect a Source folder 'src'. Trying 'Tests'");
			srcfolder = project.getFolder("Tests");
		}
		if(srcfolder == null || !srcfolder.exists()){
			Activator.log("Activator.getRootSourceFolder did not detect either Source folder 'src' or 'Tests'!");
			return null;
		}
		return srcfolder;
	}

	/**
	 * Attempt to locate a Source file.<br>
	 * @param project IProject, If null, we will attempt to deduce the Project.
	 * @param sourcefile String, the source file name relative to the Project-Source-Folder.
	 * @return IFile or null if not determined.
	 * @throws ExecutionException
	 * @see Activator#getSourceFile(IProject)
	 */
	public static IFile getSourceFile(IProject project, String sourcefile) throws ExecutionException{
		IFile file = getRootSourceFolder(project).getFile(sourcefile);
		if(file!=null && file.exists()) return file;
		return null;
	}

	/**The view name of of package explorer, 'Package Explorer'*/
	public static final String VIEW_PACKAGE_EXPLORER = "Package Explorer";
	/**
	 * Activate the {@link #VIEW_PACKAGE_EXPLORER} view.
	 * @return true if the view {@link #VIEW_PACKAGE_EXPLORER} has been activated.
	 */
	public static boolean activateProjectExplorer(){
		return activateView(VIEW_PACKAGE_EXPLORER);
	}
	/**
	 * Activate a view.
	 * @param viewName String, the view to activate
	 * @return boolean true if the view has been activated
	 */
	public static boolean activateView(String viewName){
		try{
			if(viewName==null || viewName.trim().isEmpty()) return false;

			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = win.getActivePage();

			IViewReference[] views = page.getViewReferences();
			for(IViewReference view:views){
				log("Name="+view.getPartName()+" title="+view.getTitle()+" description="+view.getContentDescription());
				if(viewName.equals(view.getPartName())){
					log("Activating "+viewName+" .... ");
					page.activate(view.getPart(true));
					return true;
				}
			}
		}catch(Exception e){
			error(StringUtils.debugmsg(false)+" failed.", e);
		}

		return false;
	}

	/**
	 * Attempt to locate a Source file of current active project.<br>
	 * @param sourcefile String, the source file name relative to the Project-Source-Folder.
	 * @return IFile or null if not determined.
	 * @throws ExecutionException
	 * @see Activator#getSourceFile(IProject)
	 */
	public static IFile getActiveProjectSourceFile(String sourcefile){
		String debugmsg = StringUtils.debugmsg(false);
		IFile file = null;

		if(sourcefile!=null){
			//activate the  "Package Explorer" view before we can get the IProject object.
			if(!activateProjectExplorer()){
				warn("The project explorer has not been activated successfully! You may not get the active project.");
			}

			try{
				file = getSourceFile(getActiveProject(), sourcefile);
			}catch(Exception e){
				warn(debugmsg+StringUtils.debugmsg(e));
			}

			try{
				if(file==null || !file.exists()) file = getSourceFile(null, sourcefile);
			}catch(Exception e){
				warn(debugmsg+StringUtils.debugmsg(e));
			}

			//Finally, if we cannot find the file, we will try to find it in all possible projects
			//but this-try may give us a wrong file if multiple projects have the same file
			try{
				if(file==null || !file.exists()){
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

					for(IProject p: projects){
						file = getSourceFile(p, sourcefile);
						if(file!=null && file.exists()) break;
					}

				}
			}catch(Exception e){
				warn(debugmsg+StringUtils.debugmsg(e));
			}
			if(file==null || !file.exists()){
				warn(debugmsg+" WE CANNOT FIND THE SOURCE FILE '"+sourcefile+"'!");
			}
		}

		return file;
	}

	/**
	 * Attempt to return the full package name of the currently selected Resource.
	 * @param resource if null, we will attempt to deduce the currently selected IResource.
	 * @return String package name ("sample.testcases", "sample.testruns", etc..), or null if not deduced.
	 * @throws ExecutionException
	 */
    public static String getSelectedSourcePackage(IResource resource) throws ExecutionException{
    	if(resource == null) resource = getSelectedResource(null);
    	if(resource == null) {
			Activator.log("Activator.getSelectedSourcePackage did not detect Source folder!");
    		return null;
    	}
    	IProject project = resource.getProject();
		IFolder srcfolder = getRootSourceFolder(project);
		if(srcfolder == null || !srcfolder.exists()){
			Activator.log("Activator.getSelectedSourcePackage did not detect Source folder!");
			return null;
		}

		String srcDir = srcfolder.getFullPath().toPortableString();
		String resourcePath = resource.getFullPath().toPortableString();

		String packageName = null;
		String[] newpackage = resourcePath.split(srcDir);
		try{
			packageName = newpackage[1].replace("/", ".");
			if(packageName.startsWith(".")) packageName = packageName.substring(1);
		}catch(Exception x){
			Activator.log("Activator.getSelectedSourcePackage "+x.getClass().getName()+", "+ x.getMessage());
		}
    	return packageName;
    }

	/**
	 * selectionService available to your calling routine, or null if we should try to deduce it.
	 * @param selectionService
	 * @return IProject or null if not determined.
	 * @throws ExecutionException
	 */
	public static IProject getSelectedProject(ISelectionService selectionService) throws ExecutionException{
		IResource resource = getSelectedResource(selectionService);
		if(resource == null){
			Activator.log("Activator.getSelectedProject: The current selected Resource is null.");
			return null;
		}
		IProject project = resource.getProject();
		if(project == null) {
			Activator.log("Activator.getSelectedProject: The Project is null.");
			return null;
		}
		if(!project.isOpen()) {
			Activator.log("Activator.getSelectedProject: The Project is NOT open.");
			return null;
		}
		try{
			if(!project.hasNature(ProjectNature.NATURE_ID)){
				Activator.log("Activator.getSelectedProject: The Project is not a SeleniumPlus project.");
				return null;
			}
		}catch(CoreException x){
			Activator.log("Activator.getSelectedProject: "+ x.getClass().getName() +" when confirming SeleniumPlus project type.");
			return null;
		}
		return project;
	}


	/**
	 * selectionService available to your calling routine, or null if we should try to deduce it.
	 * @param selectionService
	 * @return IResource or null if not determined.
	 * @throws ExecutionException
	 */
	public static IResource getSelectedResource(ISelectionService selectionService) throws ExecutionException{
		if(selectionService == null){
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			if(win == null){
				Activator.log("Activator.getSelectedResource returning null because it could NOT deduce the Active Workbench Window.");
				return null;
			}
			selectionService = win.getSelectionService();
			if(selectionService == null){
				Activator.log("Activator.getSelectedResource returning null because it could NOT deduce a Selection Service.");
				return null;
			}
		}
		ISelection selection = selectionService.getSelection();
		if(! (selection instanceof IStructuredSelection)){
			Activator.log("Activator.getSelectedResource returning null because it could NOT deduce a proper StructuredSelection.");
			return null;
		}
		IResource resource = null;
		IStructuredSelection item = (IStructuredSelection) selection;
		Object element = item.getFirstElement();
		if(element instanceof IResource){
			resource = (IResource) element;
		}else if(!(element instanceof IAdaptable)){
			Activator.log("Activator.getSelectedResource returning null because it could NOT deduce a proper IAdaptable Resource.");
			return null;
		}else{
			IAdaptable adaptable = (IAdaptable) element;
			resource = (IResource) adaptable.getAdapter(IResource.class);
		}
		return resource;
	}

	public static IProject getActiveProject(){
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("");
		return project;
	}

	/**
	 * Get the absolute path to the Project location.
	 * @param iproject
	 * @return File object with absolute path to the Project directory.
	 * @throws ExecutionException
	 */
	public static File getProjectLocation(IProject iproject) throws ExecutionException{
		File rootdir = null;
		try{ rootdir = iproject.getLocation().toFile();}catch(NullPointerException np){}
		if(rootdir == null){
			Activator.log("getProjectLocation: The Project Location is null.");
			throw new ExecutionException("The SAFS Project RootDir is not a valid directory: null.");
		}
		if(!rootdir.isDirectory()){
			throw new ExecutionException("The SAFS Project root is not a valid directory: "+ rootdir.getPath());
		}
		return rootdir;
	}

	/**
	 * Detect a file under the selected project.<br>
	 * @param selectionService ISelectionService, a selected stuff in a project
	 * @param fileRelativeToProjectRoot String, the name of the file (relative to project's root) to detect.
	 * @return File, the detected file in the selected project.
	 * @throws ExecutionException if cannot detect a SeleniumPlus Project
	 * @throws NotEnabledException if cannot detect the specified file in the project's root.
	 */
	public static File getProjectFile(ISelectionService selectionService, String fileRelativeToProjectRoot) throws ExecutionException, NotEnabledException{
		IProject iproject = getSelectedProject(selectionService);
		if(iproject == null){
			throw new NotEnabledException("Cannot detecte a SeleniumPlus Project, please select one.");
		}

		File rootdir = getProjectLocation(iproject);
		File file = new CaseInsensitiveFile(rootdir, fileRelativeToProjectRoot).toFile();
		if(file == null || !file.isFile())
			throw new ExecutionException("Did not detect a file '"+fileRelativeToProjectRoot+"' under the Project root directory '"+rootdir.getAbsolutePath()+"'.");

		return file;
	}

	/**
	 * @return File, the test.ini configuration file in the selected project.
	 * @throws ExecutionException if cannot detect a SeleniumPlus Project
	 * @throws NotEnabledException if cannot detect the file 'test.ini' in the project's root.
	 */
	private static File getProjectINIFile() throws ExecutionException, NotEnabledException{
		return getProjectFile(null, BaseProject.TESTINI_FILE);
	}

	/**
	 * @return ConfigureInterface for test.ini configuration file in the selected project.
	 * @throws ExecutionException if cannot detect a SeleniumPlus Project
	 * @throws NotEnabledException if cannot detect the file 'test.ini' in the project's root.
	 */
	public static ConfigureInterface getPorjectConfiguration() throws ExecutionException, NotEnabledException{
		File iniFile = getProjectINIFile();
		return getPorjectConfiguration(iniFile.getParent(), iniFile.getName());
	}

	/**
	 * Get the ConfigureInterface for 'configPath' under 'rootDir'.
	 * @param rootDir String, the root directory
	 * @param configPath String, the relative configuration file
	 * @return ConfigureInterface
	 */
	private static ConfigureInterface getPorjectConfiguration(String rootDir, String configPath){
		ConfigureLocatorInterface locator = ConfigureLocatorInterface.getConfigureLocator(DriverConstant.DEFAULT_CONFIGURE_LOCATOR);
		return locator.locateConfigureInterface(rootDir, configPath);
	}

	private static MessageConsole findConsole(String name) {
		try{
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			IConsole[] consoles = consoleManager.getConsoles();
			for (int i = 0; i < consoles.length; i++){
				if(name==null){
					return (MessageConsole) consoles[i];
				}
				if (name.equals(consoles[i].getName()))
					return (MessageConsole) consoles[i];
			}

			//no console found, so create a new one
			if(name==null){
				name = DEFAULT_CONSOLE;
			}
			MessageConsole myConsole = new MessageConsole(name, null);
			consoleManager.addConsoles(new IConsole[]{myConsole});
			return myConsole;
		}catch(Exception e){
			error(StringUtils.debugmsg(false)+" Failed to initialize console.", e);
			return null;
		}
	}

	/** "SeleniumPlus Plugin Console" */
	public static final String DEFAULT_CONSOLE = "SeleniumPlus Plugin Console";

	private static boolean defaultConsoleIsShowing = false;
	private static MessageConsole defaultConsole = null;

	/** The standard output stream to {@link #DEFAULT_CONSOLE}, it might be null. */
	public static MessageConsoleStream out = null;
	/** The error output stream to {@link #DEFAULT_CONSOLE}, it might be null */
	public static MessageConsoleStream err = null;

	static{
		try{
			defaultConsole = findConsole(null);
			out = defaultConsole.newMessageStream();
			err = defaultConsole.newMessageStream();

			org.eclipse.swt.graphics.Color RED = new org.eclipse.swt.graphics.Color(org.eclipse.swt.widgets.Display.getCurrent (), 255, 0, 0);
			err.setColor(RED);
		}catch(Exception e){
			error(StringUtils.debugmsg(false)+" Failed to initialize console's error/stdout stream.", e);
		}
	}

	/**
	 * Show the {@link #DEFAULT_CONSOLE} on the current active page.<br>
	 * <font color='red'>Be careful: this will change the current selected item. </font>
	 */
	public static void showConsole(){
		if(!defaultConsoleIsShowing) showConsole(defaultConsole);
	}
	/**
	 * Show the Console on the current active page.<br>
	 * <font color='red'>Be careful: this will change the current selected item. </font>
	 * @param console MessageConsole
	 */
	private static void showConsole(MessageConsole console){
		try {
			IConsoleView view = (IConsoleView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IConsoleConstants.ID_CONSOLE_VIEW);
			view.display(console);
			defaultConsoleIsShowing = true;
		} catch (Exception e) {
			error(StringUtils.debugmsg(false)+" Failed to show console '"+console.getName()+"'", e);
		}
	}

	/**
	 * @return IProxyService
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IProxyService getProxyService() {
        try {
        	Bundle bundle = FrameworkUtil.getBundle(getClass());
        	BundleContext bundleCntx = bundle.getBundleContext();
        	ServiceTracker proxyTracker = new ServiceTracker(bundleCntx, IProxyService.class.getName(), null);
        	proxyTracker.open();
			return (IProxyService) proxyTracker.getService();
		} catch (Exception e) {
			Activator.error("fail to get IProxyService", e);
			return null;
		}
    }
}

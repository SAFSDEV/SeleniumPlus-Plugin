package com.sas.seleniumplus;
/**
 * Developer logs:
 * APR 20, 2017	(Lei Wang)	Load custom-resource-bundle before trying the default one.
 */
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.safs.StringUtils;
import org.safs.tools.CaseInsensitiveFile;

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

	// The shared instance
	private static Activator plugin;

	private static ResourceBundle preferences = null;

	public static final String seleniumhome = System.getenv(BaseProject.SELENIUM_PLUS_ENV);

	/**
	 * The constructor
	 */
	public Activator() {
		if(seleniumhome == null) throw new IllegalStateException("SELENIUM_PLUS System Environment '"+BaseProject.SELENIUM_PLUS_ENV+"' was not set!");
	}

	/*
	 * @see org.eclipse.ui.IStartup
	 */
	public void earlyStartup() {
		try{
			checkSeleniumPlusClasspath();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(ProjectAddListener.LISTENER, IResourceChangeEvent.POST_CHANGE);
		}catch(Exception x){
			Activator.log("Ignoring "+ x.getClass().getName()+", "+x.getMessage());
		}
	}

	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		initResourceBundle();
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
	 *
	 * @param key String, the preference key in the resource bundle properties file
	 * @return String, the preference default value
	 */
	//User needs to catch all the RuntimeExceptions himself
	public static String getPreference(String key){
		String value = preferences.getString(key);
		return value.trim();
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
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
	 * @return IProxyService
	 */
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

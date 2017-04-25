package com.sas.seleniumplus;
/**
 * APR 25, 2017	(SBJLWA) Moved a lot of methods from UpdateSeleniumPlus.java
 *                       Moved this class from package com.sas.seleniumplus.popupmenu
 *
 */
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.safs.StringUtils;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.popupmenu.TopMostOptionPane;
import com.sas.seleniumplus.preferences.PreferenceConstants;
import com.sas.seleniumplus.projects.BaseProject;

public class CommonLib {
	/** '<b>source</b>' */
	public static final String DIR_SOURCE 			= "source";
	/** '<b>libs</b>' */
	public static final String DIR_LIBS 			= "libs";
	/** '<b>update_bak</b>' */
	public static final String DIR_UPDATE_BAK		="update_bak";
	/** '<b>plugins</b>' */
	public static final String DIR_ELIPSE_PLUGINS 	="plugins";
	/** '<b>safsupdate.jar</b>' */
	public static final String JAR_SAFSUPDATE 	="safsupdate.jar";

	private static Shell shell = null;
	private static IPreferenceStore store = null;
	private static String javaexe = null;
	private static String updatejar = null;
	private static String latestServer = null;

	/**
	 * Locate the latest (last modified) selenium-server-standalone JAR file in the libs directory
	 * where SeleniumPlus is installed.<br>
	 * Updates the local latestServer setting for use in refreshBuildPath message dialog.
	 *
	 * @return File pointing to the desired JAR file.
	 * @throws ExecutionException if SELENIUM_PLUS does not appear to be properly installed on the system.
	 * @see #refreshBuildPath()
	 */
	public static File getLatestSeleniumServerJARFile() throws ExecutionException{
		//This has been checked when starting SeleniumPlus (loading plugin), see Activator.java#earlyStartup(). We don't need to check again.
//		String seleniumdir = System.getenv(BaseProject.SELENIUM_PLUS_ENV);
//
//		if (seleniumdir == null || seleniumdir.length() == 0) {
//			Activator
//					.log("RefreshBuildPath path cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
//			throw new ExecutionException(
//					"RefreshBuildPath cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
//		}
//
//		File rootdir = new CaseInsensitiveFile(seleniumdir).toFile();
//		if (!rootdir.isDirectory()) {
//			Activator
//					.log("RefreshBuildPath cannot deduce SELENIUM_PLUS install directory at: "
//							+ rootdir.getAbsolutePath());
//			throw new ExecutionException(
//					"RefreshBuildPath cannot deduce SELENIUM_PLUS install directory at: "
//							+ rootdir.getAbsolutePath());
//		}

		File libsdir = new CaseInsensitiveFile(Activator.seleniumhome, DIR_LIBS).toFile();

		if (!libsdir.isDirectory()) {
			Activator.log("RefreshBuildPath cannot deduce valid SELENIUM_PLUS/libs directory at: "+ libsdir.getAbsolutePath());
			throw new ExecutionException("RefreshBuildPath cannot deduce valid SELENIUM_PLUS/libs directory at: "+ libsdir.getAbsolutePath());
		}

		File[] files = libsdir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				try {
					return name.toLowerCase().startsWith(BaseProject.SELENIUM_SERVER_JAR_PART_NAME);
				} catch (Exception x) {
					return false;
				}
			}
		});

		File jarfile = null;

		if (files.length == 0) {
			Activator.log("RefreshBuildPath cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
			throw new ExecutionException("RefreshBuildPath cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
		}

		// if more than one, find the latest
		if (files.length > 1) {
			long diftime = 0;
			for (File afile : files) {
				if (afile.lastModified() > diftime) {
					diftime = afile.lastModified();
					jarfile = afile;
				}
			}
		} else {
			jarfile = files[0];
		}
		if(jarfile != null) latestServer = jarfile.getName();
		return jarfile;
	}

	/**
	 * @return An array of SeleniumPlus specific JAR files we wish to make sure are in the Project's Classpath.
	 * <p>
	 * Currently that is the selenium standalone server JAR, seleniumplus JAR, and JSTAFEmbedded JAR.
	 * @throws ExecutionException
	 */
	public static IClasspathEntry[] getLatestSeleniumPlusJARS() throws ExecutionException{
		IPath path = null;
		IPath sourcepath = null;

		//selenium-server-standalone.jar
		File seleniumjar = getLatestSeleniumServerJARFile();
		path = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_LIBS+"/" + seleniumjar.getName());
		IClasspathEntry selenium_server_jar = JavaCore.newVariableEntry(path, null, null);

		//seleniumplus.jar, attache "javadoc" and "source code"
		IClasspathAttribute[] attrs = null;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if(store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_JAVADOC)){
			String javadocURL = store.getString(PreferenceConstants.UPDATESITE_JAVADOC_URL);
			attrs = new  IClasspathAttribute[]{
				JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, javadocURL)
			};
		}
		if(store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_SOURCE_CODE)){
			sourcepath = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_SOURCE+"/"+ BaseProject.SAFSSELENIUM_PLUS_SOURCE_CORE);
		}

		path = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_LIBS+"/"+ BaseProject.SELENIUMPLUS_JAR);
		IClasspathEntry seleniumplus_jar = JavaCore.newVariableEntry(path, sourcepath, null, null, attrs, false);

		//JSTAFEmbedded.jar
		path = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_LIBS+"/"+ BaseProject.JSTAF_EMBEDDDED_JAR);
		IClasspathEntry jstaf_embedded_jar = JavaCore.newVariableEntry(path, null, null);

		return new IClasspathEntry[]{seleniumplus_jar, jstaf_embedded_jar, selenium_server_jar};
	}

	/**
	 * Refresh a single Project Build Path with the latest SeleniumPlus JAR files as provided in cpEntries.<br>
	 * This method is called repetitively by refreshBuildPath() for each Project it finds in the Workspace.<br>
	 * may be called by other routines wishing to refresh the build path on just a single Project.
	 * @param iP -- the Project to refresh with updated SeleniumPlus jars.
	 * @param cpEntries the latest version of JARS we want to make sure our added or modified.<br>
	 * Get these from getLatestSeleniumPlusJARS().
	 * @return true only if this was a SeleniumPlus project that got updated.
	 * @throws ExecutionException
	 * @throws CoreException -- most likely if we are trying to process a Closed project.
	 * @see #getLatestSeleniumPlusJARS()
	 */
	public static boolean refreshBuildPath(IProject iP, IClasspathEntry... cpEntries) throws CoreException, ExecutionException {
		try {

			Activator.log("CommonLib.refreshBuildPath(IProject) processing Project " + iP.getName()+" with Classpath Entries: "+Arrays.toString(cpEntries));

			ArrayList<IClasspathEntry> entriesToSave = new ArrayList<IClasspathEntry>();
			boolean isSeleniumPlus = false;
			IJavaProject javaProject = (IJavaProject) iP.getNature(JavaCore.NATURE_ID);
			IClasspathEntry[] existingEntries = new IClasspathEntry[]{};

		    if (javaProject != null)
		    	existingEntries = javaProject.getRawClasspath();

		    for (int i = 0; i < existingEntries.length; i++) {
		    	IClasspathEntry entry = existingEntries[i];

		    	if (entry.getPath().toString().contains(BaseProject.SELENIUMPLUS_JAR) ||
		    		entry.getPath().toString().contains(BaseProject.JSTAF_EMBEDDDED_JAR)) {

		    		isSeleniumPlus = true;
		    		entry = null;
		    		break;
		    	}
		    }

		    IClasspathEntry seleniumPlusEntry = null;

			if (isSeleniumPlus) {
				Activator.log(iP.getName()+" does appear to be a SeleniumPlus Project.");
				String pathName = null;

				//Add selenium-plus dependencies except 'seleniumplus.jar', which will be handled later for 'javadoc' and 'source attachment'
				if(cpEntries!=null){
					for(IClasspathEntry cp: cpEntries){
						if(cp.getPath().toString().contains(BaseProject.SELENIUMPLUS_JAR)){
							seleniumPlusEntry = cp;
						}else{
							entriesToSave.add(cp);
						}
					}
				}

				//add custom specific jar entries
				for (int j = 0; j < existingEntries.length; j++) {

					IClasspathEntry entry = existingEntries[j];

					if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE ||
					    entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						pathName = entry.getPath().toString();

						if (pathName.contains(BaseProject.SELENIUM_SERVER_JAR_PART_NAME) ||
							pathName.contains(BaseProject.JSTAF_EMBEDDDED_JAR)){
							continue;
						}else if(pathName.contains(BaseProject.SELENIUMPLUS_JAR)){
							//Handle 'javadoc' and 'source attachment'
							entriesToSave.add(checkSleneiumPlusJarEntry(iP, entry, seleniumPlusEntry));
							continue;
						}

					}
					entriesToSave.add(entry);
				}

				IClasspathEntry[] newClasspath = entriesToSave.toArray(new IClasspathEntry[0]);
				Activator.log(iP.getName()+" set new Classpath Entries: "+Arrays.toString(newClasspath));
				javaProject.setRawClasspath(newClasspath, null);
				javaProject.save(null, true);
				entriesToSave = null;

			}

			return isSeleniumPlus;

		} catch (JavaModelException jme) {
			Activator.log("CommonLib.refreshBuildPath(IProject) "+ iP.getName() +" "+ jme.getClass().getSimpleName()+", "+ jme.getMessage(), jme);
		}
		return false;
	}

	/**
	 * Check if the "javadoc" and "source attachment" are the same in old and new ClasspathEntry.<br/>
	 * Dialog will be prompted for user to choose if they are different.<br/>
	 * @param iP IProject, the selenium plus test project
	 * @param oldSePlusEntry IClasspathEntry, the old selenium plus classpath entry
	 * @param newSePlusEntry IClasspathEntry, the new selenium plus classpath entry
	 * @return IClasspathEntry, The class path entry with user preferred "javadoc" and "source attachment".
	 */
	private static IClasspathEntry checkSleneiumPlusJarEntry(IProject iP, IClasspathEntry oldSePlusEntry, IClasspathEntry newSePlusEntry){

		//Compare javadoc.
		IClasspathAttribute[] attrs = oldSePlusEntry.getExtraAttributes();
		String oldJavadocValue = null;
		for(IClasspathAttribute attr: attrs){
			if(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME.equals(attr.getName())){
				oldJavadocValue = attr.getValue();
				break;
			}
		}

		IClasspathAttribute[] newAttrs = newSePlusEntry.getExtraAttributes();
		String newJavadocValue = null;
		for(IClasspathAttribute attr: newAttrs){
			if(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME.equals(attr.getName())){
				newJavadocValue = attr.getValue();
				break;
			}
		}
		String javadocValue = chooseAsset(iP, oldJavadocValue, newJavadocValue, "SeleniumPlus Java Doc");

		//Compare source attachment
		IPath oldSourcePath = oldSePlusEntry.getSourceAttachmentPath();
		IPath newSourcePath = newSePlusEntry.getSourceAttachmentPath();
		IPath sourcePath = chooseAsset(iP, oldSourcePath, newSourcePath, "SeleniumPlus Java Source");

		//Use the newSePlusEntry
		if(sourcePath==newSourcePath && javadocValue==newJavadocValue){
			return newSePlusEntry;
		}

		//Use the user selected "javadoc" and "source code attachment"
		IPath latestSePlusJar = newSePlusEntry.getPath();
		return JavaCore.newVariableEntry(latestSePlusJar, sourcePath, null, null,
				//IClasspathAttribute[], TODO Should we add the attributes of the oldSePlusEntry?
				new IClasspathAttribute[]{JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, javadocValue)},
				false);
	}

	private static <T> T chooseAsset(IProject iP, T oldAsset, T newAsset, String assetName){
		T asset = newAsset;

		if(newAsset==null){
			if(oldAsset!=null){
				//Do you want to clean the old asset?
				Object[] options = {
						"Clean "+assetName,
						"Keep "+assetName
				};
				int selected = TopMostOptionPane.showOptionDialog(null,
								"SeleniumPlus project '"+iP.getName()+"':\n"+
								"Old "+assetName+" location is '"+oldAsset+"'\n\n"+
								"Would you like to clean it?",
								"Update "+assetName,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null,
								options,
								options[0]);
				if(JOptionPane.YES_OPTION==selected){
					asset = newAsset;
				}else{
					asset = oldAsset;
				}
			}
		}else{
			//if oldAsset is null, use the newAsset
			if(oldAsset!=null && !newAsset.toString().equals(oldAsset.toString())){
				Object[] options = {
						"Use New "+assetName,
						"Use Old "+assetName
				};
				int selected = TopMostOptionPane.showOptionDialog(null,
						"SeleniumPlus project '"+iP.getName()+"':\n"+
								"Old "+assetName+" location is '"+oldAsset+"'\n"+
								"New "+assetName+" location is '"+newAsset+"'\n\n"+
								"Would you like to use the new one?",
								"Update "+assetName,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null,
								options,
								options[0]);
				if(JOptionPane.YES_OPTION==selected){
					asset = newAsset;
				}else{
					asset = oldAsset;
				}
			}
		}

		return asset;
	}

	public static void refreshBuildPath() throws ExecutionException {

		int updatedProject = 0;
		int nonSelProject = 0;
		int closeProject = 0;

		IClasspathEntry[] latestJars = getLatestSeleniumPlusJARS(); //updates latestServer setting
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (IProject iP : projects) {
			try {
				boolean isSeleniumPlus = refreshBuildPath(iP, latestJars);
				if(isSeleniumPlus){
					updatedProject++;
				}else{
					nonSelProject++;
				}
			} catch (CoreException ce) {
				closeProject++;
				Activator.log("Closed project " +iP.getName());
			}
		}

		String jmsg = "Project(s) Status:\n";
		if (updatedProject != 0)
			jmsg = jmsg + updatedProject + " SeleniumPlus project(s) updated.\n";

		if (closeProject != 0)
			jmsg = jmsg + closeProject + " Closed project(s) were NOT updated.\n";

		if (nonSelProject != 0)
			jmsg = jmsg + nonSelProject + " non-Sel+ project(s) were NOT updated.\n";

		String msg = "Following jars added the build path:\n"
			+ latestServer + "\n"
			+ BaseProject.SAFSSELENIUM_JAR + "\n"
			+ BaseProject.JSTAF_EMBEDDDED_JAR + "\n\n"
			+ jmsg;

		TopMostOptionPane.showConfirmDialog(null, msg,
				"Build path updated..", JOptionPane.CLOSED_OPTION);
	}

	public static Shell getShell(){
		if(shell==null){
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		return shell;
	}

	public static IPreferenceStore getPreferenceStore(){
		if(store==null){
			store = Activator.getDefault().getPreferenceStore();
		}
		return store;
	}

	private static String getJavaExe(String seleniumhome){
		if(javaexe==null){
			File javadir = new CaseInsensitiveFile(seleniumhome, "Java/jre/bin").toFile();
			if(javadir.isDirectory()){
				javaexe = javadir.getAbsolutePath()+"/java";
			}else{
				javaexe = "java";
			}
		}
		return javaexe;
	}

	/**
	 * Get seleniumhome/update_bak/libs/{@link #JAR_SAFSUPDATE}
	 */
	private static String getUpdateJar(String seleniumhome) throws ExecutionException, IOException{
		if(updatejar==null){
			String update_bakdir = getBackupDir(seleniumhome);
			File update_bak_libs_dir = new CaseInsensitiveFile(update_bakdir, DIR_LIBS).toFile();
			if(!update_bak_libs_dir.exists()){
				update_bak_libs_dir.mkdir();
			}

			File safsupdate_backup_jar = new CaseInsensitiveFile(update_bak_libs_dir, JAR_SAFSUPDATE).toFile();
			if(!safsupdate_backup_jar.exists()){
				copyUpdateJar(seleniumhome);
			}

			updatejar = safsupdate_backup_jar.getAbsolutePath();
		}

		return updatejar;
	}

	public static String getPluginDir() throws ExecutionException{
		String eclipseDir = System.getProperty("user.dir");//SeleniumPlus embedded Eclipse home directory
		File plugindir = new CaseInsensitiveFile(eclipseDir, DIR_ELIPSE_PLUGINS).toFile();

		if(!plugindir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
		}

		return plugindir.getAbsolutePath();
	}

	/**
	 * Copy {@link #JAR_SAFSUPDATE} from seleniumhome/libs to seleniumhome/update_bak/libs.
	 */
	public static void copyUpdateJar(String seleniumhome) throws ExecutionException, IOException{
		File libsdir = new CaseInsensitiveFile(seleniumhome, DIR_LIBS).toFile();
		if(!libsdir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
		}

		String update_bakdir = getBackupDir(seleniumhome);
		File update_bak_libs_dir = new CaseInsensitiveFile(update_bakdir, DIR_LIBS).toFile();// lib backup
		if(!update_bak_libs_dir.exists()){
			update_bak_libs_dir.mkdir();
		}

		//copy the safsupdate.jar to a backup folder and use the copied-safsupdate.jar to do the update work
		File safsupdate_jar = new CaseInsensitiveFile(libsdir, JAR_SAFSUPDATE).toFile();
		File safsupdate_backup_jar = new CaseInsensitiveFile(update_bak_libs_dir, JAR_SAFSUPDATE).toFile();
		FileUtilities.copyFileToFile(safsupdate_jar, safsupdate_backup_jar);

	}

	/**
	 * Append {@link #DIR_UPDATE_BAK} to parameter destdir to form a backup directory.
	 * @param destdir String, the destination directory
	 * @return String, the backup directory
	 * @throws ExecutionException
	 */
	private static String getBackupDir(String destdir) throws ExecutionException{
		File backupdir = new CaseInsensitiveFile(destdir, DIR_UPDATE_BAK).toFile();
		if(!backupdir.exists()){
			backupdir.mkdir();
		}

		if(!backupdir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS backup directory at: "+backupdir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS backup directory at: "+backupdir.getAbsolutePath());
		}

		return backupdir.getAbsolutePath();
	}

	public static int getUpdateTimeout(){
		int timeout = CommonLib.getPreferenceStore().getInt(PreferenceConstants.TIME_OUT);
		if (timeout < 0 ){
			Activator.warn("UpdateSeleniumPlus: 'update timeout' cannot be negative, using default value.");
			timeout = CommonLib.getPreferenceStore().getDefaultInt(PreferenceConstants.TIME_OUT);
		}
		timeout = timeout * 60;
		return timeout;
	}

	public static int updateLibrary(String destdir, int timeout) throws URISyntaxException, ExecutionException, IOException{
		if(!getPreferenceStore().getBoolean(PreferenceConstants.BOOLEAN_VALUE_LIB)){
			return UPDATE_CODE_NON_ENABLED;
		}

		String url = getPreferenceStore().getString(PreferenceConstants.UPDATESITE_LIB_URL);
		url = url==null ? null: url.trim();

		return update(true, true, "SeleniumPlus Library Update", url, destdir, timeout);
	}

	public static int updateSource(String destdir, int timeout) throws URISyntaxException, ExecutionException, IOException{
		if(!getPreferenceStore().getBoolean(PreferenceConstants.BOOLEAN_VALUE_SOURCE_CODE)){
			return UPDATE_CODE_NON_ENABLED;
		}

		String url = getPreferenceStore().getString(PreferenceConstants.UPDATESITE_SOURCECODE_URL);
		url = url==null ? null: url.trim();

		return update(true, true, "SeleniumPlus Source Code Update", url, destdir, timeout);
	}

	public static int updatePlugin(String destdir, int timeout) throws URISyntaxException, ExecutionException, IOException{
		if(!getPreferenceStore().getBoolean(PreferenceConstants.BOOLEAN_VALUE_PLUGIN)){
			Activator.log("Update Plugin is not enbaled.");
			return UPDATE_CODE_NON_ENABLED;
		}

		String url = getPreferenceStore().getString(PreferenceConstants.UPDATESITE_PLUGIN_URL);
		url = url==null ? null: url.trim();

		Shell shell = getShell();

		if(! shell.getMinimized()) shell.setMinimized(true);

		int plugin_update = update(false, false, "SeleniumPlus Plugin Update", url, destdir, timeout);

		if (plugin_update > 0) {
			Object[] options = {
					"Refresh Now",
					"I will do it Later"
			};
			int selected = TopMostOptionPane.showOptionDialog(null,
					"SeleniumPlus PlugIn was Updated.\n"+
							"Eclipse Workspace will need to be refreshed.\n\n"+
							"Refresh Now? Or do it yourself Later.",
							"Update Requires Refresh",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]);
			if(shell.getMinimized()) shell.setMinimized(false);

			if(JOptionPane.YES_OPTION == selected){
				PlatformUI.getWorkbench().restart();
			}
		}

		return plugin_update;
	}

	private static int update(boolean recursvie, boolean allTypes, String title,
			                  String sourceURL, String destdir, int timeout) throws URISyntaxException, ExecutionException, IOException{
		return update(getShell(), getJavaExe(Activator.seleniumhome), recursvie, recursvie, title,
				getUpdateJar(Activator.seleniumhome), sourceURL, destdir, timeout);

	}

	private static int update(Shell shell, String javaexe,
			                  boolean recursvie, boolean allTypes,
			                  String title,
			                  String safsupdate_jar, String sourceURL, String destdir, int timeout) throws URISyntaxException, ExecutionException{

		String proxySettings = null;
		proxySettings = getProxySettings(new URI(sourceURL));
		Activator.log("Try to set proxy '"+proxySettings+"'");

		String backupdir = getBackupDir(destdir);

		String cmdline = javaexe +
				proxySettings +//Add HTTP PROXY setting as JVM arguments
				" -jar "+ safsupdate_jar +
				" -prompt:\""+title+"\"" +
				" -s:\"" + sourceURL +"\""+
				(recursvie? " -r":" ") +
				(allTypes? " -a":" ") +
				" -t:\"" + destdir +"\""+
				" -b:\"" + backupdir+"\""
				;

		if(! shell.getMinimized()) shell.setMinimized(true);
		Activator.log("Launching "+title+" with cmdline: "+ cmdline);

		int updateResult = runCommand(cmdline,timeout);
		if(updateResult >= 0){
			Activator.log(title+" exited normally.");
		}else{
			Activator.log(title+" DID NOT exit normally.");
		}

		return updateResult;
	}

	/**
	 * Get the JVM HTTP PROXY settings, such as -Dhttp.proxyHost=proxy.server.host -Dhttp.proxyPort=80 -Dhttp.nonProxyHosts="local.site.1|local.site.2|*.domain"
	 * @return String, the JVM HTTP PROXY settings; "" if PROXY is not found.
	 */
	public static String getProxySettings(URI updateURI){
		StringBuffer proxy = new StringBuffer();
		try{
			String proxyHost = System.getProperty(StringUtils.SYSTEM_PROPERTY_PROXY_HOST);
			String proxyPort = System.getProperty(StringUtils.SYSTEM_PROPERTY_PROXY_PORT);
			String proxyBypass = System.getProperty(StringUtils.SYSTEM_PROPERTY_NON_PROXY_HOSTS);

			//Try to get from ProxyService
			IProxyService proxyService = Activator.getDefault().getProxyService();
			IProxyData[] proxydatas = proxyService.select(updateURI);
			if(proxydatas.length==0) {
				Activator.log("Eclipse reports no Proxy necessary for "+ updateURI.toString());
				return "";
			}

			for(IProxyData data: proxydatas){
				if(data.getHost()!=null){
					proxyHost = data.getHost();
					proxyPort = String.valueOf(data.getPort());
					break;
				}
			}
			if(proxyHost == null) {
				Activator.log("Proxy data finds no Proxy host for "+ updateURI.toString());
				return "";
			}

			proxy.append(" -D"+StringUtils.SYSTEM_PROPERTY_PROXY_HOST+"="+proxyHost);
			if(proxyPort != null)
				proxy.append(" -D"+StringUtils.SYSTEM_PROPERTY_PROXY_PORT+"="+proxyPort);
			if(proxyBypass != null)
				proxy.append(" -D"+StringUtils.SYSTEM_PROPERTY_NON_PROXY_HOSTS+"="+proxyBypass);

		}catch(Exception e){
			Activator.error("Fail to get HTTP Proxy settings", e);
		}

		return proxy.toString();
	}

	public static final int UPDATE_CODE_USER_CANCEL = -1;
	public static final int UPDATE_CODE_ERROR 		= -2;
	public static final int UPDATE_CODE_NON_ENABLED	= -3;

	/**
	 *
	 * @param cmd
	 * @param timeout
	 * @return exitcode<br>
	 * {@link #UPDATE_CODE_ERROR} error occurred.<br>
	 * {@link #UPDATE_CODE_USER_CANCEL} user cancelled.<br>
	 * 0/+N number of modified files.
	 */
	private static int runCommand(String cmd, int timeout){

		int exitcode = UPDATE_CODE_ERROR;
		try {

			Process process = Runtime.getRuntime().exec(cmd);
			long now = System.currentTimeMillis();
		    long timeoutInMillis = 1000L * timeout;
		    long finish = now + timeoutInMillis;

		    while(isAlive(process)){

		    	Thread.sleep(10);
		    	if ( System.currentTimeMillis() > finish) {
		    		process.destroy();
		    		TopMostOptionPane.showConfirmDialog(null, "Update process timeout.\n"
		    				+ "It could be slow network connection or \n"
		    				+ "Not enough timeout set into Selenium+ preference.",
                            "Update timeout", JOptionPane.CLOSED_OPTION);
		    		return UPDATE_CODE_ERROR;
		    	}
		    }
		    try{ exitcode = process.exitValue();}catch(Exception ignore){}
		} catch (Exception e) {
			Activator.log("Update failed: " + e.getMessage());
			return UPDATE_CODE_ERROR;
		}
		return exitcode;
	}

	private static boolean isAlive(Process p){

		try {
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException  e) {
			return true;
		}
	}
}

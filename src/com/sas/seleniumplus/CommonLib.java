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
 * APR 25, 2017	(LeiWang) Moved a lot of methods from UpdateSeleniumPlus.java
 *                       Moved this class from package com.sas.seleniumplus.popupmenu
 * APR 26, 2017	(LeiWang) Modified getLatestSeleniumPlusJARS(): Use JSTAF.jar if JSTAFEmbedded.jar doesn't exist.
 * JUL 28, 2017	(LeiWang) Modified getJavaExe() etc.: Copy the embedded JRE to a temporary folder and use it so that the "embedded Java" under folder %SELENIUM_PLUS% can be modified.
 * AUG 21, 2017	(LeiWang) Modified to stop SeleniumPlus IDE if it is using "embedded JRE" and if we are going to update it:
 *                                copy "embedded JRE" to temp folder
 *                                change eclipse.ini to use the JVM in the temp folder
 *                       Modified to ignore the sub folder "Java" and "Java64" if we are not going to update "embedded JRE".
 * SEP 01, 2017 (LEIWANG) Modified updateLibrary(): After library update, try to restore the JRE setting of file eclipse.ini
 * JUL 03, 2018 (LEIWANG) Modified copyUpdateJar(): Copy also safsupdate.jar's dependencies to update backup folder.
 *                        Modified updateLibrary(): Add extra parameter "-tools:GHOSTSCRIPT" to say "GHOSTSCRIPT" will be updated.
 *                        Added updateGhostscript() to install the ghostscript; don't install ghostscript inside updateLibrary().
 *                        Modified runCommand(): use ConsumOutStreamProcess to fix the nasty "hang for ever" problem.
 * NOV 13, 2018 (LEIWANG) Modified refreshBuildPath(): added the log4j config file if it doesn't exist.
 */
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.install.ConsumOutStreamProcess;
import org.safs.install.GhostScriptInstaller;
import org.safs.install.InstallerImpl;
import org.safs.install.LibraryUpdate;
import org.safs.sockets.DebugListener;
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
	/** '<b>jna-4.2.2.jar</b>' required by safsupdate.jar */
	public static final String JAR_JNA 			="jna-4.2.2.jar";
	/** '<b>jna-platform-4.2.2.jar</b>' required by safsupdate.jar */
	public static final String JAR_JNA_PLATFORM	="jna-platform-4.2.2.jar";
	/** '<b>win32-x86.jar</b>' required by safsupdate.jar */
	public static final String ZIP_WIN32_86 	="win32-x86.zip";

	/** An array of files to copy from folder 'libs' to 'backup' for updating.
	 * [{@link #JAR_JNA}, {@link #JAR_JNA_PLATFORM}, {@link #JAR_SAFSUPDATE}, {@link #ZIP_WIN32_86} ]
	 */
	public static final String[] UPDATER_FILES 	= {JAR_SAFSUPDATE, JAR_JNA, JAR_JNA_PLATFORM, ZIP_WIN32_86};

	/** '<b>eclipse.ini</b>' */
	public static final String FILE_ELIPSE_INI 	="eclipse.ini";

	/** '<b>.bak</b>' */
	public static final String SUFFIX_BAK 	=".bak";

	/** '<b>-vm</b>' */
	public static final String OPTION_VM_ELIPSE_INI 	="-vm";

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

		File libsdir = new CaseInsensitiveFile(Activator.seleniumhome, DIR_LIBS).toFile();

		if (!libsdir.isDirectory()) {
			Activator.log("RefreshBuildPath cannot deduce valid SELENIUM_PLUS/libs directory at: "+ libsdir.getAbsolutePath());
			throw new ExecutionException("RefreshBuildPath cannot deduce valid SELENIUM_PLUS/libs directory at: "+ libsdir.getAbsolutePath());
		}

		File[] files = libsdir.listFiles(new FilenameFilter() {
			@Override
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
	 *
	 * @return An array of SeleniumPlus specific JAR files we wish to make sure are in the Project's Classpath.
	 * <p>
	 * Currently that is the selenium standalone server JAR, seleniumplus JAR, and JSTAFEmbedded JAR.
	 * Meanwhile, we try to attach source code and javadoc to seleniumplus.jar, if the source code doesn't exist
	 * then we try to download it.
	 * @throws ExecutionException
	 */
	public static IClasspathEntry[] getLatestSeleniumPlusJARS() throws ExecutionException{
		IPath path = null;
		IPath sourcepath = null;

		//1. selenium-server-standalone.jar
		File seleniumjar = getLatestSeleniumServerJARFile();
		path = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_LIBS+"/" + seleniumjar.getName());
		IClasspathEntry selenium_server_jar = JavaCore.newVariableEntry(path, null, null);

		//2. seleniumplus.jar, attache "javadoc" and "source code"
		IClasspathAttribute[] attrs = null;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if(store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_JAVADOC)){
			String javadocURL = store.getString(PreferenceConstants.UPDATESITE_JAVADOC_URL);
			if(isValidJavaDocPath(javadocURL)){
				attrs = new  IClasspathAttribute[]{
						JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, javadocURL)
				};
			}else{
				Activator.error("Failed to attach javadoc for Classpath Entry '"+BaseProject.SELENIUMPLUS_JAR+"', '"+javadocURL+"' is NOT valid!");
			}
		}
		if(store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_SOURCE_CODE)){
			String sourceZipFile = Activator.seleniumhome+File.separator+DIR_SOURCE+File.separator+ BaseProject.SAFSSELENIUM_PLUS_SOURCE_CORE;
			if(!fileExist(sourceZipFile)){
				try {
					int updatedFileNumber = CommonLib.updateSource(Activator.seleniumhome, CommonLib.getUpdateTimeout());
					Activator.log("Updated "+updatedFileNumber+" source files.");
				} catch (Exception e) {
					Activator.warn("Failed to update source code! Due to "+e.toString());
				}
			}
			if(fileExist(sourceZipFile)){
				sourcepath = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_SOURCE+"/"+ BaseProject.SAFSSELENIUM_PLUS_SOURCE_CORE);
			}else{
				Activator.error("Failed to attach source for Classpath Entry '"+BaseProject.SELENIUMPLUS_JAR+"', '"+sourceZipFile+"' does NOT exist!");
			}
		}

		path = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_LIBS+"/"+ BaseProject.SELENIUMPLUS_JAR);
		IClasspathEntry seleniumplus_jar = JavaCore.newVariableEntry(path, sourcepath, null, null, attrs, false);

		//3. JSTAFEmbedded.jar or JSTAF.jar, we prefer JSTAFEmbedded.jar
		IClasspathEntry jstafJAR = null;
		try{
			String embeddedjarFile = Activator.seleniumhome+File.separator+DIR_LIBS+File.separator+ BaseProject.JSTAF_EMBEDDDED_JAR;
			if(fileExist(embeddedjarFile)){
				path = new Path(Activator.SELENIUMPLUS_HOME + "/"+DIR_LIBS+"/"+ BaseProject.JSTAF_EMBEDDDED_JAR);
				jstafJAR = JavaCore.newVariableEntry(path, null, null);
			}else{
				Activator.warn("Failed to create Classpath Entry for '"+BaseProject.JSTAF_EMBEDDDED_JAR+"', due to '"+embeddedjarFile+"' does NOT exist!");
			}
		}catch(Exception e){
			Activator.warn("Failed to create Classpath Entry for '"+BaseProject.JSTAF_EMBEDDDED_JAR+"', due to "+e.toString());
		}
		if(jstafJAR==null){
			try{
				if(BaseProject.STAFDIR==null){
					BaseProject.STAFDIR = System.getenv(BaseProject.STAFDIR_ENV);
				}
				path = new Path(BaseProject.STAFDIR + BaseProject.STAF_JAR_PATH);
				jstafJAR = JavaCore.newLibraryEntry(path, null, null);
			}catch(Exception e){
				Activator.error("Failed to create Classpath Entry for '"+BaseProject.STAF_JAR+"', due to "+e.toString());
			}
		}

		return new IClasspathEntry[]{seleniumplus_jar, jstafJAR, selenium_server_jar};
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
					BaseProject.refreshLog4jConfigFile(iP);
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

		TopMostOptionPane.showConfirmDialog(null, msg, "Build path updated..", JOptionPane.CLOSED_OPTION);

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

	/**
	 * @param seleniumhome String, the SeleniumPlus installation folder
	 * @param updateJRE boolean, Indicates if we are going to update "embedded JRE".
	 * @return String, the Java executable
	 */
	private static String getJavaExe(String seleniumhome, boolean updateJRE){
		if(javaexe==null || updateJRE){

			if(embeddedJRE.isDirectory()){
				File jreBinFolder = null;

				if(updateJRE){
					//copy the JRE to a temporary folder so that we can update the "embedded JRE"
					File tempjre = getTempFolder(tempJREFolderName+".for.script");

					try {
						Activator.log("copying JRE from '"+embeddedJRE.getCanonicalPath()+"' to '"+tempjre.getCanonicalPath()+"' ...");
						FileUtilities.copyDirectoryRecursively(embeddedJRE, tempjre);
						jreBinFolder = new CaseInsensitiveFile(tempjre, "bin").toFile();
					} catch (Exception e) {
						Activator.error("Met "+e.toString());
					}
				}else{
					jreBinFolder = new CaseInsensitiveFile(embeddedJRE, "bin").toFile();
				}

				if(jreBinFolder!=null && jreBinFolder.isDirectory()){
					javaexe = jreBinFolder.getAbsolutePath()+File.separator+"java";
				}
			}

			if(javaexe==null){
				javaexe = "java";
			}
		}

		return javaexe;
	}

	public static void main(String[] args){
		IndependantLog.setDebugListener(new DebugListener(){
			@Override
			public String getListenerName() {
				return "CommonLib.DebugListener";
			}

			@Override
			public void onReceiveDebug(String message) {
				System.out.println(message);
			}
		});

		String javaexe = getJavaExe(Activator.seleniumhome, true);
		System.out.println(javaexe);
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
		File plugindir = new CaseInsensitiveFile(getEclipseHome(), DIR_ELIPSE_PLUGINS).toFile();

		if(!plugindir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
		}

		return plugindir.getAbsolutePath();
	}

	/**
	 * Copy {@link #UPDATER_FILES} from seleniumhome/libs to seleniumhome/update_bak/libs.
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

		//copy the safsupdate.jar, jna-4.2.2.jar, jna-platform-4.2.2.jar and win32-x86.zip
		//to a backup folder and use the copied-safsupdate.jar to do the update work
		for(String name: UPDATER_FILES){
			File updater = new CaseInsensitiveFile(libsdir, name).toFile();
			File updater_backup = new CaseInsensitiveFile(update_bak_libs_dir, name).toFile();
			FileUtilities.copyFileToFile(updater, updater_backup);
		}
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

	/** The SeleniumPlus Eclipse folder, it normally is %SELENIUM_PLUS%\eclipse\ */
	private static String eclipseHome = null;
	public static String getEclipseHome(){
		if(eclipseHome==null){
			//SeleniumPlus embedded Eclipse home directory
			eclipseHome = System.getProperty("user.dir");
		}

		return eclipseHome;
	}

	/** The file represents the embedded 32 bits Java folder */
	private static final  File embeddedJava = new CaseInsensitiveFile(Activator.seleniumhome, "Java").toFile();
	/** The file represents the embedded 32 bits JRE folder */
	private static final  File embeddedJRE = new CaseInsensitiveFile(Activator.seleniumhome, "Java/jre").toFile();
	/** The file represents the embedded 64 bits JRE folder */
	private static final File embeddedJRE64 = new CaseInsensitiveFile(Activator.seleniumhome, "Java64/jre").toFile();

	/** "safs.jre" the temporary folder to hold the 32 bits Java */
	private static final String tempJavaFolderName = "safs.java";
	/** "safs.jre" the temporary folder to hold the 32 bits JRE */
	private static final String tempJREFolderName = "safs.jre";
	/** "safs.jre64" the temporary folder to hold the 64 bits JRE */
	private static final String tempJRE64FolderName = "safs.jre64";

	/** The file represents the embedded eclipse's configuration file eclipse.ini */
	private static final File eclipseINIFile = new CaseInsensitiveFile(getEclipseHome(), FILE_ELIPSE_INI).toFile();
	/** The backup file of the embedded eclipse's configuration file eclipse.ini */
	private static final File eclipseINIFileBackup = new CaseInsensitiveFile(eclipseINIFile.getAbsolutePath()+SUFFIX_BAK).toFile();

	/**
	 * Create a clean temporary folder. The existing temporary will be deleted.
	 * @param folderName String, the folder name.
	 * @return File, the temporary folder. It might be null.
	 */
	private static File getTempFolder(String folderName){
		File tempFolder = null;

		try {
			tempFolder = new File(System.getProperty("java.io.tmpdir"), folderName);
			FileUtilities.deleteDirectoryRecursively(tempFolder.getAbsolutePath(), false);
			tempFolder.mkdirs();
		} catch (Exception e) {
			Activator.error("Met "+e.toString());
		}

		if(tempFolder==null || !tempFolder.exists() || !tempFolder.isDirectory()){
			try {
				tempFolder = Files.createTempDirectory(folderName).toFile();
			} catch (IOException e) {
				Activator.error("Met "+e.toString());
			}
		}

		return tempFolder;
	}

	/**
	 * SeleniumPlus Eclipse IDE needs to be restart if it is using "embedded JRE" (specified in the %SELENIUM_PLUS%\eclipse\eclipse.ini)<br>
	 * This method will check eclipse.ini file to see if the "embedded JRE" is being used.<br>
	 * If "embedded JRE" is being used, this method will copy it to a temporary folder; and modify<br>
	 * eclipse.ini file to use the JRE in temporary folder; Finally stop the IDE and terminate the JVM.<br>
	 *
	 * @param updateJRE boolean, indicates if we want to update the embedded Java/JRE
	 * @return boolean, indicates if we still want to update the embedded Java/JRE
	 * @throws IOException
	 * @see {@link #updateLibrary(String, int)}
	 * @see #restoreJRESetting()
	 */
	private static boolean modifyEclipseINI(boolean updateJRE) throws IOException{

		if (updateJRE) {
			String configedJVMString = null;
			File configedJVMFile = null;
			File embeddedJREInUse = null;
			File tempJREDir = null;
			String tempJREFile = null;
			int jvmLine = -1;//The line number in the eclipse.ini where the JVM is specified.
			//Read the eclipse.ini to check what JRE is being used.

			String[] contents = FileUtilities.readLinesFromFile(eclipseINIFile.getAbsolutePath());
			for(int i=0;i<contents.length;i++){
				if(OPTION_VM_ELIPSE_INI.equals(contents[i].trim())){
					jvmLine = i+1;
					if(jvmLine<contents.length){
						configedJVMString = contents[jvmLine];
						break;
					}
				}
			}
			if(configedJVMString!=null){
				configedJVMFile = new File(configedJVMString);
				if(!configedJVMFile.exists()){
					//maybe jvm is specified as relative path "../Java64/jre/bin/javaw.exe"
					configedJVMFile = new File(getEclipseHome(), configedJVMString);
				}
				//Get the canonical file so that the file path will be canonical so that
				//the comparison with predefined embedded Java/JRE will give us a correct answer
				configedJVMFile = configedJVMFile.getCanonicalFile();

				if(configedJVMFile.exists()){
					Activator.log("Current Eclipse JVM '"+configedJVMFile.getAbsolutePath()+"'");
					if(configedJVMFile.toPath().startsWith(embeddedJRE.toPath())){
						embeddedJREInUse = embeddedJRE;
						tempJREDir = getTempFolder(tempJREFolderName);
					}else if(configedJVMFile.toPath().startsWith(embeddedJava.toPath())){
						embeddedJREInUse = embeddedJava;
						tempJREDir = getTempFolder(tempJavaFolderName);
					}else if(configedJVMFile.toPath().startsWith(embeddedJRE64.toPath())){
						embeddedJREInUse = embeddedJRE64;
						tempJREDir = getTempFolder(tempJRE64FolderName);
					}else{
						Activator.log("SeleniumPlus embedded JRE is not being used.");
					}
				}else{
					Activator.warn("The JVM '"+configedJVMFile.getAbsolutePath()+"' does NOT exist.");
				}
			}else{
				Activator.log("NO JVM has been specified for SeleniumPlus Eclipse IDE.");
			}

			if(embeddedJREInUse!=null && tempJREDir!=null){
				Activator.log("SeleniumPlus embedded JRE '"+embeddedJREInUse.getAbsolutePath()+"' is being used.");

				Object[] options = {
						"Yes",
						"No"
				};
				int option = TopMostOptionPane.showOptionDialog(null,
								"SeleniumPlus embedded Java is being used. Do you want to update it?\n\n"+
								"If [Yes], then eclipse config file '"+eclipseINIFile+"' will be modified,\n"+
							    "the original config file will be copied to backup file '"+eclipseINIFileBackup+"'\n"+
								"and SeleniumPlus IDE will be terminated.\n"+
								"You need to start the SelniumPlus and update again.\n\n",
								"Embedded JRE Update Requires Restart Eclipse",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null,
								options,
								options[0]);

				if(JOptionPane.YES_OPTION == option){
					//copy the "embedded JRE" to another place 'tempJREDir'
					FileUtilities.copyDirectoryRecursively(embeddedJREInUse, tempJREDir);

					//backup eclipse.ini file
					FileUtilities.copyFileToFile(eclipseINIFile, eclipseINIFileBackup);

					//Before restarting, modify the eclipse.ini to use the copied "embedded JRE"
					String relativeJVMPath = configedJVMFile.getAbsolutePath().substring(embeddedJREInUse.getAbsolutePath().length());
					tempJREFile = new File(tempJREDir, relativeJVMPath).getAbsolutePath();
					contents[jvmLine] = tempJREFile;
					FileUtilities.writeCollectionToUTF8File(eclipseINIFile.getAbsolutePath(), Arrays.asList(contents));

					//Stop the Eclipse IDE
					PlatformUI.getWorkbench().close();
					//Exit the JVM (SeleniumPlus embedded JRE) being used.
					System.exit(0);

					//After restarting, change the eclipse.ini back to use the original "embedded JRE"

				}else{
					//Choose NO, we are not going to update embedded JRE
					updateJRE = false;
				}
			}//embeddedJVMInUse==null, so we are not using embedded JVM, return the updateJRE as it is
		}//no updateJRE, return updateJRE as it is

		return updateJRE;
	}

	/**
	 * When updating library, we might also have updated the JRE and modified the eclipse.ini to use a temporary
	 * JRE. After update, We need to modify the eclipse.ini again to set back the "-vm setting" to use the
	 * "embedded Java/JRE", which can be got from the backup file eclipse.ini.bak.
	 *
	 * @throws IOException
	 * @see {@link #updateLibrary(String, int)}
	 * @see #modifyEclipseINI(boolean)
	 */
	private static void restoreJRESetting() throws IOException{
		String configedJVMString = null;
		String backupConfigedJVMString = null;

		int jvmLine = -1;//The line number in the eclipse.ini where the JVM is specified.

		//Read the eclipse.ini to check what JRE is being used.
		String[] contents = FileUtilities.readLinesFromFile(eclipseINIFile.getAbsolutePath());
		for(int i=0;i<contents.length;i++){
			if(OPTION_VM_ELIPSE_INI.equals(contents[i].trim())){
				jvmLine = i+1;
				if(jvmLine<contents.length){
					configedJVMString = contents[jvmLine];
					break;
				}
			}
		}

		//Read the backup file eclipse.ini.bak to get the original JRE setting
		if(eclipseINIFileBackup.exists()){
			String[] backupContents = FileUtilities.readLinesFromFile(eclipseINIFileBackup.getAbsolutePath());
			for(int i=0;i<backupContents.length;i++){
				if(OPTION_VM_ELIPSE_INI.equals(backupContents[i].trim())){
					if((i+1)<backupContents.length){
						backupConfigedJVMString = backupContents[i+1];
						break;
					}
				}
			}
		}

		if(configedJVMString!=null && backupConfigedJVMString!=null && !backupConfigedJVMString.equals(configedJVMString)){
			Activator.log("SeleniumPlus Eclipse IDE is using JRE '"+configedJVMString+"'.");
			Activator.log("We are going to change that JRE back to the original JRE '"+backupConfigedJVMString+"'.");

			Object[] options = {
					"Yes",
					"No"
			};
			int option = TopMostOptionPane.showOptionDialog(null,
					        "SeleniumPlus Eclipse IDE is using JRE '"+configedJVMString+"'\n"+
					        "Do you want to change it back to the original JRE '"+backupConfigedJVMString+"'?\n\n"+
							"If [Yes], then eclipse config file '"+eclipseINIFile+"' will be modified,\n"+
							"the backup file '"+eclipseINIFileBackup+"' will be deleted.\n"+
							"This modification will not take effect until the Eclipse get restarted.\n\n",
							"Use bakcup Embedded JRE: Requires Restart Eclipse",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]);

			if(JOptionPane.YES_OPTION == option){
				//The eclipse.ini file will be modified to use the original JRE, but it will not take effect until the Eclipse gets restarted
				contents[jvmLine] = backupConfigedJVMString;
				FileUtilities.writeCollectionToUTF8File(eclipseINIFile.getAbsolutePath(), Arrays.asList(contents));

				//Delete the backup file.
				if(!eclipseINIFileBackup.delete()){
					Activator.warn("Failed to delete the backup eclipse.ini file '"+eclipseINIFileBackup.getAbsolutePath()+"'");
				}

			}else{
				Activator.log("Chose NO, the configurated JRE will NOT be changed back to its original value!");
			}
		}else{
			Activator.log("There is no need to replace the configedJVMString="+configedJVMString+"\n by \nbackupConfigedJVMString="+backupConfigedJVMString);
		}

	}

	public static int updateLibrary(String destdir, int timeout) throws URISyntaxException, ExecutionException, IOException{
		if(!getPreferenceStore().getBoolean(PreferenceConstants.BOOLEAN_VALUE_LIB)){
			return UPDATE_CODE_NON_ENABLED;
		}

		boolean updateJRE = getPreferenceStore().getBoolean(PreferenceConstants.BOOLEAN_VALUE_UPDATE_JRE);
		//If we are going to update the JRE, then we might need to restart the Eclipse firstly
		//as the SeleniumPlus IDE may start using the "embedded JRE" (specified in the %SELENIUM_PLUS%\eclipse\eclipse.ini)
		updateJRE = modifyEclipseINI(updateJRE);

		String url = getPreferenceStore().getString(PreferenceConstants.UPDATESITE_LIB_URL);
		url = url==null ? null: url.trim();

		int result = update(true, true, updateJRE, "SeleniumPlus Library Update", url, destdir, timeout);

		//we need to change the eclipse.ini to use the "embedded Java/JRE" instead of the temporary one
		//we can get that setting from the backup of eclipse.ini
		restoreJRESetting();

		return result;
	}

	public static int updateSource(String destdir, int timeout) throws URISyntaxException, ExecutionException, IOException{
		if(!getPreferenceStore().getBoolean(PreferenceConstants.BOOLEAN_VALUE_SOURCE_CODE)){
			return UPDATE_CODE_NON_ENABLED;
		}

		String url = getPreferenceStore().getString(PreferenceConstants.UPDATESITE_SOURCECODE_URL);
		url = url==null ? null: url.trim();

		return update(true, true, false, "SeleniumPlus Source Code Update", url, destdir, timeout);
	}

	public static int updatePlugin(String destdir, int timeout) throws URISyntaxException, ExecutionException, IOException{
		if(!getPreferenceStore().getBoolean(PreferenceConstants.BOOLEAN_VALUE_PLUGIN)){
			Activator.log("Update Plugin is not enbaled.");
			return UPDATE_CODE_NON_ENABLED;
		}

		String url = getPreferenceStore().getString(PreferenceConstants.UPDATESITE_PLUGIN_URL);
		url = url==null ? null: url.trim();

		int plugin_update = update(false, false, false, "SeleniumPlus Plugin Update", url, destdir, timeout);

		if (plugin_update > 0) {
			Object[] options = {
					"Refresh Now",
					"I will do it Later"
			};
			int option = TopMostOptionPane.showOptionDialog(null,
							"SeleniumPlus PlugIn was Updated.\n"+
							"Eclipse Workspace will need to be refreshed.\n\n"+
							"Refresh Now? Or do it yourself Later.",
							"Update Requires Refresh",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]);

			if(JOptionPane.YES_OPTION == option){
				PlatformUI.getWorkbench().restart();
			}
		}

		return plugin_update;
	}

	private static int update(boolean recursvie, boolean allTypes, boolean updateJRE, String title,
			                  String sourceURL, String destdir, int timeout, String... extraParams) throws URISyntaxException, ExecutionException, IOException{
		List<String> params = new ArrayList<String>();
		if(!updateJRE){
			//If we don't update JRE, then ignore the sub folder 'Java' and 'Java64' of SeleniumPlus
			//TODO Will use the constant LibraryUpdate.ARG_PREFIX_E_SEP later, sometimes the update fails to update seleniumplus.jar, and we cannot get that constant available
			//params.add(LibraryUpdate.ARG_PREFIX_E+"Java"+LibraryUpdate.ARG_PREFIX_E_SEP+"Java64");
			params.add(LibraryUpdate.ARG_PREFIX_E+"Java;Java64");
		}
		if(extraParams!=null){
			for(String param: extraParams){
				params.add(param);
			}
		}

		return update(getShell(), getJavaExe(Activator.seleniumhome, updateJRE), recursvie, allTypes, title,
				getUpdateJar(Activator.seleniumhome), sourceURL, destdir, timeout, params.toArray(new String[0]));
	}

	private static int update(Shell shell, String javaexe,
			                  boolean recursvie, boolean allTypes,
			                  String title,
			                  String safsupdate_jar, String sourceURL, String destdir, int timeout, String... extraParams) throws URISyntaxException, ExecutionException{

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
		for(String extraParam: extraParams){
			cmdline += " "+extraParam+" ";
		}

		if(! shell.getMinimized()) shell.setMinimized(true);
		Activator.log("Launching "+title+" with cmdline: "+ cmdline);

		int updateResult = runCommand(cmdline,timeout);
		if(updateResult >= 0){
			Activator.log(title+" exited normally.");
		}else{
			Activator.log(title+" DID NOT exit normally.");
		}

		if(shell.getMinimized()) shell.setMinimized(false);

		return updateResult;
	}

	/**
	 * Update/Install the ghostscript tool.<br>
	 * It is strongly recommended to call this after {@link #updateLibrary(String, int)}, which will
	 * put necessary assets (such as tool's installer, dependency jar files) into the SAFS/SeleniumPlus installation directory.<br>
	 *
	 * @param safsdir String, the SAFS/SeleniumPlus installation directory, where we can find the tool's installer
	 * @param tooldir String, the directory to install the tool. It can be null, the tool will be installed into its default directory.
	 * @param timeout int, the timeout in seconds to wait the installation
	 * @param silent boolean, if the tool's installer works in silent mode
	 * @param verbose boolean, if the installer works in verbose mode
	 * @param debug boolean, if the installer works in debug mode
	 * @param extraParams String[], more extra parameters
	 * @return boolean, if the installation succeeds
	 */
	public static boolean updateGhostscript(String safsdir, String tooldir, int timeout, boolean silent, boolean verbose, boolean debug, String... extraParams){

		Shell shell = getShell();
		String javaexe = getJavaExe(safsdir, false);

		//We have finished updating library, so we use %SELNIUM_PLUS%\libs\safsupdate.jar
		String safsupdate_jar = new CaseInsensitiveFile(safsdir, DIR_LIBS+File.separator+JAR_SAFSUPDATE).getAbsolutePath();

		//java org.safs.install.GhostScriptInstaller -safs safsHome [-u] [-installdir home] [-silent] [-v] [-debug]
		String installer = GhostScriptInstaller.class.getName();
		String cmdline = javaexe +
				" -classpath "+ safsupdate_jar +
				" " + installer+
				" " + InstallerImpl.ARG_SAFS_DIR + " "+safsdir +
				" " + (tooldir!=null? InstallerImpl.ARG_INSTALLDIR+" \""+tooldir+"\"":"")+
				" " + (silent? InstallerImpl.ARG_SILENT:"")+
				" " + (verbose? InstallerImpl.ARG_VERBOSE:"")+
				" " + (debug? InstallerImpl.ARG_DEBUG:"")
				;
		for(String extraParam: extraParams){
			cmdline += " "+extraParam+" ";
		}

		if(! shell.getMinimized()) shell.setMinimized(true);
		Activator.log("Launching "+installer+" with cmdline: "+ cmdline);

		int updateResult = runCommand(cmdline, timeout);
		if(shell.getMinimized()) shell.setMinimized(false);

		if(updateResult >= 0){
			Activator.log(installer+" exited normally.");
			return true;
		}else{
			Activator.error(installer+" did NOT exit normally with code "+updateResult);
			return false;
		}
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
	 * @param cmd String, the command to run
	 * @param timeout int, the timeout in seconds to wait. It can be {@link ConsumOutStreamProcess#WAIT_FOREVER}.
	 * @return exitcode<br>
	 * {@link #UPDATE_CODE_ERROR} error occurred.<br>
	 * {@link #UPDATE_CODE_USER_CANCEL} user cancelled.<br>
	 * 0/+N number of modified files.
	 */
	private static int runCommand(String cmd, int timeout){
		int exitcode = UPDATE_CODE_ERROR;

		ConsumOutStreamProcess process = new ConsumOutStreamProcess(cmd, true, true);
		process.setTimeout(timeout);
		exitcode = process.start();

		if(ConsumOutStreamProcess.PROCESS_NORMAL_END==exitcode){
			return exitcode;
		}

		if(ConsumOutStreamProcess.PROCESS_COMMAND_TIMEOUT==exitcode){
			TopMostOptionPane.showConfirmDialog(null, "Update process timeout.\n"
					+ "It could be slow network connection or \n"
					+ "Not enough timeout set into Selenium+ preference.",
					"Update timeout", JOptionPane.CLOSED_OPTION);
		}

		return UPDATE_CODE_ERROR;
	}

	private static boolean fileExist(String filename){
		return Files.exists(FileSystems.getDefault().getPath(filename)) ||
			   new File(filename).exists();
	}
	private static boolean isValidJavaDocPath(String javaDocPath){
		if(javaDocPath==null || javaDocPath.isEmpty()) return false;
		//Check valid javadoc path, such as url, zip file etc.
		return true;
	}
}

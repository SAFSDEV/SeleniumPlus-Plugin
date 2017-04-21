package com.sas.seleniumplus.popupmenu;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.projects.BaseProject;

public class CommonLib {

	private String latestServer;

	/**
	 * Locate the latest (last modified) selenium-server-standalone JAR file in the libs directory
	 * where SeleniumPlus is installed.<br>
	 * Updates the local latestServer setting for use in refreshBuildPath message dialog.
	 *
	 * @return File pointing to the desired JAR file.
	 * @throws ExecutionException if SELENIUM_PLUS does not appear to be properly installed on the system.
	 * @see #refreshBuildPath()
	 */
	public File getLatestSeleniumServerJARFile() throws ExecutionException{
		String seleniumdir = System.getenv(BaseProject.SELENIUM_PLUS_ENV);

		if (seleniumdir == null || seleniumdir.length() == 0) {
			Activator
					.log("RefreshBuildPath path cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
			throw new ExecutionException(
					"RefreshBuildPath cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
		}

		File rootdir = new CaseInsensitiveFile(seleniumdir).toFile();
		if (!rootdir.isDirectory()) {
			Activator
					.log("RefreshBuildPath cannot deduce SELENIUM_PLUS install directory at: "
							+ rootdir.getAbsolutePath());
			throw new ExecutionException(
					"RefreshBuildPath cannot deduce SELENIUM_PLUS install directory at: "
							+ rootdir.getAbsolutePath());
		}

		File libsdir = new CaseInsensitiveFile(rootdir, "libs").toFile();

		if (!libsdir.isDirectory()) {
			Activator
					.log("RefreshBuildPath cannot deduce valid SELENIUM_PLUS/libs directory at: "
							+ libsdir.getAbsolutePath());
			throw new ExecutionException(
					"RefreshBuildPath cannot deduce valid SELENIUM_PLUS/libs directory at: "
							+ libsdir.getAbsolutePath());
		}

		File[] files = libsdir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				try {
					return name.toLowerCase().startsWith(
							BaseProject.SELENIUM_SERVER_JAR_PART_NAME);
				} catch (Exception x) {
					return false;
				}
			}
		});

		File jarfile = null;

		if (files.length == 0) {
			Activator
					.log("RefreshBuildPath cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
			throw new ExecutionException(
					"RefreshBuildPath cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
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
	public IClasspathEntry[] getLatestSeleniumPlusJARS() throws ExecutionException{
		File jarfile = getLatestSeleniumServerJARFile();
		IClasspathEntry selenium_server_jar = JavaCore.newVariableEntry(
				new Path(Activator.SELENIUMPLUS_HOME + "/libs/"
						+ jarfile.getName()), null, null);
		IClasspathEntry seleniumplus_jar = JavaCore.newVariableEntry(new Path(
				Activator.SELENIUMPLUS_HOME + "/libs/"
						+ BaseProject.SELENIUMPLUS_JAR), null, null);
		IClasspathEntry jstaf_embedded_jar = JavaCore.newVariableEntry(
				new Path(Activator.SELENIUMPLUS_HOME + "/libs/"
						+ BaseProject.JSTAF_EMBEDDDED_JAR), null, null);

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
	public boolean refreshBuildPath(IProject iP, IClasspathEntry... cpEntries) throws CoreException, ExecutionException {
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

			if (isSeleniumPlus) {
				Activator.log(iP.getName()+" does appear to be a SeleniumPlus Project.");
				String pathName = null;

				for (int j = 0; j < existingEntries.length; j++) {

					IClasspathEntry entry = existingEntries[j];

					if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE
							|| entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						pathName = entry.getPath().toString();

						if (pathName.contains(BaseProject.SELENIUM_SERVER_JAR_PART_NAME) ||
							pathName.contains(BaseProject.SELENIUMPLUS_JAR) ||
							pathName.contains(BaseProject.JSTAF_EMBEDDDED_JAR)){
							continue;
						}

					}
					entriesToSave.add(entry);
				}

				if(cpEntries!=null && cpEntries.length>0){
					for(IClasspathEntry cp: cpEntries) entriesToSave.add(cp);
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

	public void refreshBuildPath() throws ExecutionException {

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
}

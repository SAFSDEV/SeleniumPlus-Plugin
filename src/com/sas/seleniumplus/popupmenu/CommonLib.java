package com.sas.seleniumplus.popupmenu;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

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

	private int updatedProject = 0;
	private int nonSelProject = 0;
	private int closeProject = 0;
	private String msg;
	
	public void refreshBuildPath() throws ExecutionException {

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

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		IClasspathEntry selenium_server_jar = JavaCore.newVariableEntry(
				new Path(Activator.SELENIUMPLUS_HOME + "/libs/"
						+ jarfile.getName()), null, null);
		IClasspathEntry seleniumplus_jar = JavaCore.newVariableEntry(new Path(
				Activator.SELENIUMPLUS_HOME + "/libs/"
						+ BaseProject.SELENIUMPLUS_JAR), null, null);
		IClasspathEntry jstaf_embedded_jar = JavaCore.newVariableEntry(
				new Path(Activator.SELENIUMPLUS_HOME + "/libs/"
						+ BaseProject.JSTAF_EMBEDDDED_JAR), null, null);

		Activator.log(selenium_server_jar
				+ " is the new selenium-server-standalone jar");

			for (IProject iP : projects) {
		
				try {
					
					Activator.log("Project name " + iP.getName());
				
					ArrayList entriesToSave = new ArrayList();
					boolean isSeleniumPlus = false;
					IJavaProject javaProject = (IJavaProject) iP
							.getNature(JavaCore.NATURE_ID);
					IClasspathEntry[] existingEntries = new IClasspathEntry[]{};
					
				    if (javaProject != null) 
				    	existingEntries = javaProject.getRawClasspath();
				    
									
					for (int i = 0; i < existingEntries.length; i++) {
							IClasspathEntry entry = existingEntries[i];
		
							if (entry.getPath().toString()
									.contains(BaseProject.SELENIUMPLUS_JAR)
									|| entry.getPath().toString()
											.contains(BaseProject.JSTAF_EMBEDDDED_JAR)) {
		
								isSeleniumPlus = true;								
								entry = null;
								break;
							}
						}
		
						if (isSeleniumPlus) {
		
							for (int j = 0; j < existingEntries.length; j++) {
		
								IClasspathEntry entry = existingEntries[j];
		
								if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE
										|| entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
		
									if (entry
											.getPath()
											.toString()
											.contains(
													BaseProject.SELENIUM_SERVER_JAR_PART_NAME))
										continue;
		
									if (entry.getPath().toString()
											.contains(BaseProject.SELENIUMPLUS_JAR))
		
										continue;
		
									if (entry.getPath().toString()
											.contains(BaseProject.JSTAF_EMBEDDDED_JAR))
										continue;
		
								}
		
								entriesToSave.add(entry);
		
							}
		
							entriesToSave.add(selenium_server_jar);
							entriesToSave.add(seleniumplus_jar);
							entriesToSave.add(jstaf_embedded_jar);
							IClasspathEntry[] newClasspath = (IClasspathEntry[]) entriesToSave
									.toArray(new IClasspathEntry[0]);
							javaProject.setRawClasspath(newClasspath, null);
							javaProject.save(null, true);
							updatedProject++;
							entriesToSave = null;
							
						} else {
							// non selenium projects
							nonSelProject++;
						}
					
				} catch (JavaModelException jme) {
					JOptionPane.showConfirmDialog(null, jme.getMessage(),
							"No new selenium server jar", JOptionPane.CLOSED_OPTION);
				} catch (CoreException ce) {
					//ce.printStackTrace();
					closeProject++;
					Activator.log("Close project " +iP.getName());
				}		

			}

			String jmsg = "Project(s) Status:\n";
			if (updatedProject != 0)
				jmsg = jmsg + updatedProject + " SeleniumPlus project(s) updated.\n";
			
			if (closeProject != 0)
				jmsg = jmsg + closeProject + " Closed project(s) were NOT updated.\n";
			
			if (nonSelProject != 0)
					jmsg = jmsg + nonSelProject + " non-Sel+ project(s) were NOT updated.\n";
			
			msg = "Following jars added the build path:\n"
				+ jarfile.getName() + "\n"
				+ BaseProject.SAFSSELENIUM_JAR + "\n"
				+ BaseProject.JSTAF_EMBEDDDED_JAR + "\n\n" 
				+ jmsg;			

			JOptionPane.showConfirmDialog(null, msg,
					"Build path updated..", JOptionPane.CLOSED_OPTION);
	}
}

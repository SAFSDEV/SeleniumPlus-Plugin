package com.sas.seleniumplus.popupmenu;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.jar.Attributes;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;
import static org.safs.Constants.ENV_SELENIUM_PLUS;

import java.util.Map;
import java.util.jar.Attributes.Name;

import javax.swing.JOptionPane;

public class CheckVersion extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String rootdir = System.getenv(ENV_SELENIUM_PLUS);
		final String SELENIUM_SERVER_JAR_PART_NAME = "selenium-server-standalone";

		try {
			File libsdir = new CaseInsensitiveFile(rootdir, "libs").toFile();

			if(!libsdir.isDirectory()){
				Activator.log("CheckVersion cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
				throw new ExecutionException("CheckVersion cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
			}
			File[] files = libsdir.listFiles(new FilenameFilter(){ public boolean accept(File dir, String name){
				try{ return name.toLowerCase().startsWith(SELENIUM_SERVER_JAR_PART_NAME);}catch(Exception x){ return false;}
			}});

			File jarfile = null;

			if(files.length ==0){
				Activator.log("CheckVersion cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
				throw new ExecutionException("CheckVersion cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
			}

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

			if (jarfile == null)
				JOptionPane.showMessageDialog(null,
						"selenium-server-standalone jar not found in SeleniumPlus libs dir.",
						"File Not Found", JOptionPane.NO_OPTION);

			java.util.jar.JarFile jar = new java.util.jar.JarFile(jarfile);
			java.util.jar.Manifest manifest = jar.getManifest();

			String versionNumber = "";

			final Map<String, Attributes> attrs = manifest.getEntries();

			for (String name : attrs.keySet()) {
				final Attributes attr = attrs.get(name);
				for (Object a : attr.keySet()) {

					if (a.toString().equalsIgnoreCase("Selenium-Version")) {
						versionNumber = attr.getValue((Name) a);
						break;
					}
				}
			}

			jar.close();

			JOptionPane.showMessageDialog(null,
					"selenium-server-standalone jar version: " + versionNumber,
					"Selenium Version", JOptionPane.NO_OPTION);

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
						"selenium-server-standalone.jar not found",
						"Jar not found", JOptionPane.NO_OPTION);
		}

		return event;

	}
}

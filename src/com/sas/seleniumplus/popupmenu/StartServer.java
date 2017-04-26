package com.sas.seleniumplus.popupmenu;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.safs.android.auto.lib.Process2;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.projects.BaseProject;

public class StartServer extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String seleniumdir = System.getenv(BaseProject.SELENIUM_PLUS_ENV);

		if(seleniumdir == null || seleniumdir.length()==0){
			Activator.log("StartServer cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
			throw new ExecutionException("StartServer cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
		}

		File rootdir = new CaseInsensitiveFile(seleniumdir).toFile();
		if(!rootdir.isDirectory()){
			Activator.log("StartServer cannot deduce SELENIUM_PLUS install directory at: "+rootdir.getAbsolutePath());
			throw new ExecutionException("StartServer cannot deduce SELENIUM_PLUS install directory at: "+rootdir.getAbsolutePath());
		}

		File extradir = new File(rootdir, "extra");
		if(!extradir.isDirectory()){
			Activator.log("StartServer cannot deduce SELENIUM_PLUS/extra directory at: "+extradir.getAbsolutePath());
			throw new ExecutionException("StartServer cannot deduce SELENIUM_PLUS/extra  directory at: "+extradir.getAbsolutePath());
		}

		String javaexe = "java";
		File javadir = new CaseInsensitiveFile(rootdir, "Java64/jre/bin").toFile();
		if(javadir.isDirectory()) javaexe = javadir.getAbsolutePath()+"/java";

		File libsdir = new CaseInsensitiveFile(rootdir, "libs").toFile();
		if(!libsdir.isDirectory()){
			Activator.log("StartServer cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
			throw new ExecutionException("StartServer cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
		}
		File[] files = libsdir.listFiles(new FilenameFilter(){ public boolean accept(File dir, String name){
			try{ return name.toLowerCase().startsWith("selenium-server-standalone");}catch(Exception x){ return false;}
		}});

		File jarfile = null;

		if(files.length ==0){
			Activator.log("StartServer cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
			throw new ExecutionException("StartServer cannot deduce SELENIUM_PLUS selenium-server-standalone* JAR file in /libs directory.");
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
		// we are now set with a remote server jarfile
		Activator.log("StartServer using selenium server jarfile '"+ jarfile.getAbsolutePath()+"'");

		String consoledir = null;
		IProject iproject = Activator.getSelectedProject(null);
		if(iproject == null){
			JOptionPane.showConfirmDialog(null, "A SeleniumPlus Project must be selected.",
					                            "Invalid Project", JOptionPane.OK_OPTION);
			throw new ExecutionException("A SeleniumPlus Project must be selected.");
		}
		File projectroot = Activator.getProjectLocation(iproject);
		if(projectroot != null ){
			consoledir = projectroot.getAbsolutePath();
		}else{
			consoledir = rootdir.getAbsolutePath();
		}
		Activator.log("Selenium Server runtime consoles expected at "+ consoledir);

		File chromedriver = new CaseInsensitiveFile(rootdir, "/extra/chromedriver.exe").toFile();
		if(!chromedriver.isFile()) chromedriver = new CaseInsensitiveFile(rootdir, "/extra/chromedriver").toFile();
		File iedriver = new CaseInsensitiveFile(rootdir, "/extra/IEDriverServer.exe").toFile();

		try{
			IJavaProject jproject = JavaCore.create(iproject);
			String[] jars = JavaRuntime.computeDefaultRuntimeClassPath(jproject);
			String cp = jarfile.getAbsolutePath();
			for(String jar:jars){
				cp += File.pathSeparatorChar + jar;
			}
			if(cp.contains(" ")) cp ="\""+ cp + "\"";
			cp = " -cp "+ cp;

			String cmdline = javaexe + " -Xms512m -Xmx2g " + cp +" org.safs.selenium.util.SeleniumServerRunner "+
						     "-jar "+ jarfile.getAbsolutePath() +
					         " -Dwebdriver.log.file=\""+consoledir+"/webdriver.console\""+
					         " -Dwebdriver.firefox.logfile=\""+consoledir+"/firefox.console\""+
					         " -Dwebdriver.safari.logfile=\""+consoledir+"/safari.console\""+
					         " -Dwebdriver.ie.logfile=\""+consoledir+"/ie.console\""+
					         " -Dwebdriver.opera.logfile=\""+consoledir+"/opera.console\""+
					         " -Dwebdriver.chrome.logfile=\""+consoledir+"/chrome.console\"";

			if(chromedriver.isFile()) cmdline += " -Dwebdriver.chrome.driver=\""+ chromedriver.getAbsolutePath() +"\"";
			if(iedriver.isFile()) cmdline += " -Dwebdriver.ie.driver=\""+ iedriver.getAbsolutePath() +"\"";

			cmdline += " -timeout=20 -browserTimeout=60";

			Activator.log("StartServer launching Selenium Server with cmdline: "+ cmdline);
			new Process2(Runtime.getRuntime().exec(cmdline), true).discardStderr().discardStdout();
		}catch(Exception x){
			Activator.log("StartServer failed to launch Selenium Server due to "+x.getClass().getName()+": "+x.getMessage(), x);
			ExecutionException e = new ExecutionException(x.getMessage());
			e.initCause(x);
			throw e;
		}
		return null;

//		String selenv = System.getenv(BaseProject.SELENIUM_PLUS_ENV);
//
//		// FIXED Above? Need to support Unix/Linux/Mac
//		boolean isWin = true;
//		if(isWin){
//			try {
//				Runtime.getRuntime().exec("cmd.exe /c start "+selenv+"/extra/RemoteServer.bat");
//			} catch (Exception e) {
//				System.out.println("RemoteServer failed to execute "+ e.getMessage());
//			}
//		}else{
//			System.out.println("RemoteServer only valid on Windows OS at this time.");
//		}
//
//		return null;
	}

}

package com.sas.seleniumplus.popupmenu;

/**
 * JUN 30, 2015	(LeiWang) Modify getProxySettings(): Add "*.sas.com" as the PROXY-bypass host.
 * JUL 10, 2015 (CANAGL) Refactor getProxySettings and execute to use existing System network configuration provided by Eclipse.
 * SEP 03, 2015 (CANAGL) trim lib url and plugin url from Preferences to prevent update errors.
 */

import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.safs.StringUtils;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.preferences.PreferenceConstants;
import com.sas.seleniumplus.projects.BaseProject;

public class UpdateSeleniumPlus extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		String javaexe = "java";
		String safsupdatejar ="safsupdate.jar";
		String update_bak="update_bak";
			
		String libstatus = Activator.getDefault().getPreferenceStore()
		        .getString(PreferenceConstants.BOOLEAN_VALUE_LIB);
		
		String pluginstatus = Activator.getDefault().getPreferenceStore()
		        .getString(PreferenceConstants.BOOLEAN_VALUE_PLUGIN);
		
		String url = Activator.getDefault().getPreferenceStore()
			        .getString(PreferenceConstants.UPDATESITE_LIB_URL);		
		url = url==null ? null: url.trim();
		
		String pluginurl = Activator.getDefault().getPreferenceStore()
		        .getString(PreferenceConstants.UPDATESITE_PLUGIN_URL);
		pluginurl = pluginurl==null ? null: pluginurl.trim();
		
		String timeout_st = Activator.getDefault().getPreferenceStore()
		        .getString(PreferenceConstants.TIME_OUT);
		
		int timeout = PreferenceConstants.TIME_OUT_VALUE;
		if (timeout_st != null){
			try{timeout = Integer.parseInt(timeout_st);}catch(Exception x){
				Activator.log("UpdateSeleniumPlus using default update timeout value due to "+x.getClass().getName()+", "+ x.getMessage());
			} 
		}
		
		timeout = timeout * 60;
		
		String seleniumdir = System.getenv(BaseProject.SELENIUM_PLUS_ENV);
		String userdir		= System.getProperty("user.dir");
		
		if(seleniumdir == null || seleniumdir.length()==0){
			Activator.log("UpdateSeleniumPlus cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce SELENIUM_PLUS Environment Variable/Installation Directory.");
		}
		
		File rootdir = new CaseInsensitiveFile(seleniumdir).toFile();
		if(!rootdir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce SELENIUM_PLUS install directory at: "+rootdir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce SELENIUM_PLUS install directory at: "+rootdir.getAbsolutePath());
		}
		
		File libsdir = new CaseInsensitiveFile(rootdir, "libs").toFile();
		
		if(!libsdir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
		}

		File plugindir = new CaseInsensitiveFile(userdir, "plugins").toFile();
		
		if(!plugindir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
		}				
		
		try {
			
			new CaseInsensitiveFile(rootdir, update_bak).mkdir(); // lib backup
			File update_bakdir = new CaseInsensitiveFile(rootdir, update_bak).toFile();
			
			if(!update_bakdir.isDirectory()){
				Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/update_bak directory at: "+update_bakdir.getAbsolutePath());
				throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/update_bak directory at: "+update_bakdir.getAbsolutePath());
			}	
			
			new CaseInsensitiveFile(update_bakdir, "libs").mkdir(); // lib backup
			File update_bak_libs_dir = new CaseInsensitiveFile(update_bakdir, "libs").toFile();
		
			File safsupdate_backup_jar = new CaseInsensitiveFile(update_bak_libs_dir, safsupdatejar).toFile();
			File safsupdate_jar = new CaseInsensitiveFile(libsdir, safsupdatejar).toFile();
			
			FileUtilities.copyFileToFile(safsupdate_jar,safsupdate_backup_jar);
		
			new CaseInsensitiveFile(plugindir, update_bak).mkdir(); // plugin backup
			File update_bakdir_plugin = new CaseInsensitiveFile(plugindir, update_bak).toFile();
			
			
			File javadir = new CaseInsensitiveFile(rootdir, "Java/jre/bin").toFile();		
			if(javadir.isDirectory()) javaexe = javadir.getAbsolutePath()+"/java";
			
			int plugin_update = -2;
			int library_update = -2;
			
			
			//Get HTTP PROXY setting
			String proxySettings = getProxySettings(new URI(url));
			
			Activator.log("Try to set lib update proxy '"+proxySettings+"'");
			
			String cmdline = javaexe + 
					proxySettings +//Add HTTP PROXY setting as JVM arguments
            		" -jar "+ safsupdate_backup_jar +
            		" -prompt:\"SeleniumPlus Libs Update\"" +
            		" -s:\"" + url +"\""+ 
            		" -r" +
            		" -a" +
            		" -t:\"" + rootdir +"\""+ 
            		" -b:\"" + update_bakdir+"\""
            		;
		
			if (libstatus.equals("true")) {
				if(! shell.getMinimized()) shell.setMinimized(true);
				Activator.log("Launching SeleniumPlus Library update with cmdline: "+ cmdline);
				library_update = runCommand(cmdline,timeout);
				if(library_update >= 0){
					Activator.log("SeleniumPlus Library update exited normally.");
				}else{
					Activator.log("SeleniumPlus Library update DID NOT exit normally.");
				}
			}

			proxySettings = getProxySettings(new URI(pluginurl));
			
			Activator.log("Try to set plugin update proxy '"+proxySettings+"'");

			String cmdline_plugin = javaexe +
					proxySettings +//Add HTTP PROXY setting as JVM arguments
            		" -jar "+ safsupdate_jar +
            		" -prompt:\"SeleniumPlus Plugin Update\"" +
            		" -s:\"" + pluginurl + "\""+
            		" -t:\"" + plugindir + "\"" + 
            		" -b:\"" + update_bakdir_plugin + "\""
            		;
				
			if (pluginstatus.equals("true")) {
				if(! shell.getMinimized()) shell.setMinimized(true);
				Activator.log("Launching SeleniumPlus PlugIn update with cmdline: "+ cmdline_plugin);
				plugin_update = runCommand(cmdline_plugin,timeout);
				if(plugin_update >= 0){
					Activator.log("SeleniumPlus PlugIn update exited normally.");
				}else{
					Activator.log("SeleniumPlus PlugIn update DID NOT exit normally.");
				}
			}						

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
				if(selected == JOptionPane.CLOSED_OPTION || selected ==1){
					return null;
				}									
				PlatformUI.getWorkbench().restart();
			}
			
			if (library_update > 0) {
				CommonLib clib = new CommonLib();						
				clib.refreshBuildPath();
				TopMostOptionPane.showConfirmDialog(null, "SeleniumPlus refreshed Java Build Path successfully.", 
	                    "Update Complete", JOptionPane.CLOSED_OPTION);
			}
			TopMostOptionPane.showOptionDialog(null, "SeleniumPlus Update process has completed.", 
                    "Update Complete", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, new Object[]{"OK"}, "OK");

			return null;
			
		}catch(Exception x){
			Activator.log("Update failed to launch due to "+x.getClass().getName()+": "+x.getMessage(), x);
			ExecutionException e = new ExecutionException(x.getMessage());
			e.initCause(x);
			throw e;
		}
		finally{
			if(shell.getMinimized()) shell.setMinimized(false);			
		}
	}

	/**
	 * Get the JVM HTTP PROXY settings, such as -Dhttp.proxyHost=proxy.server.host -Dhttp.proxyPort=80 -Dhttp.nonProxyHosts="local.site.1|local.site.2|*.domain"
	 * @return String, the JVM HTTP PROXY settings; "" if PROXY is not found.
	 */
	private String getProxySettings(URI updateURI){
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
	
	/**
	 * 
	 * @param cmd
	 * @param timeout
	 * @return exitcode<br>
	 * -2 error occurred.<br>
	 * -1 user cancelled.<br>
	 * 0/+N number of modified files.
	 */
	private int runCommand(String cmd, int timeout){
					
		int exitcode = -2;
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
		    		return -2;
		    	}
		    }
		    try{ exitcode = process.exitValue();}catch(Exception ignore){}		    
		} catch (Exception e) {
			Activator.log("Update failed: " + e.getMessage());
			return -2;
		}		
		return exitcode;
	}
	
	private boolean isAlive(Process p){
		
		try {
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException  e) {
			return true;
		}
	}	
	
}
package com.sas.seleniumplus.popupmenu;

/**
 * JUN 30, 2015	(LeiWang) Modify getProxySettings(): Add "*.sas.com" as the PROXY-bypass host.
 * JUL 10, 2015 (CANAGL) Refactor getProxySettings and execute to use existing System network configuration provided by Eclipse.
 * SEP 03, 2015 (CANAGL) trim lib url and plugin url from Preferences to prevent update errors.
 */

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.safs.StringUtils;
import org.safs.text.FileUtilities;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.preferences.PreferenceConstants;

public class UpdateSeleniumPlus extends AbstractHandler {
	public static final String update_bak="update_bak";
	public static final String safsupdatejar ="safsupdate.jar";


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell shell = null;
		try {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();

			String javaexe = "java";
			int timeout = store.getInt(PreferenceConstants.TIME_OUT);
			if (timeout < 0 ){
				Activator.warn("UpdateSeleniumPlus: 'update timeout' cannot be negative, using default value.");
				timeout = store.getDefaultInt(PreferenceConstants.TIME_OUT);
			}
			timeout = timeout * 60;

			File rootdir = new CaseInsensitiveFile(Activator.seleniumhome).toFile();
			if(!rootdir.isDirectory()){
				Activator.error("UpdateSeleniumPlus cannot deduce SELENIUM_PLUS install directory at: "+rootdir.getAbsolutePath());
				throw new ExecutionException("UpdateSeleniumPlus cannot deduce SELENIUM_PLUS install directory at: "+rootdir.getAbsolutePath());
			}

			File libsdir = new CaseInsensitiveFile(rootdir, "libs").toFile();
			if(!libsdir.isDirectory()){
				Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
				throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/libs directory at: "+libsdir.getAbsolutePath());
			}

			String update_bakdir = getBackupDir(rootdir.getAbsolutePath());
			File update_bak_libs_dir = new CaseInsensitiveFile(update_bakdir, "libs").toFile();// lib backup
			update_bak_libs_dir.mkdir();

			//copy the safsupdate.jar to a backup folder and use the copied-safsupdate.jar to do the update work
			File safsupdate_jar = new CaseInsensitiveFile(libsdir, safsupdatejar).toFile();
			File safsupdate_backup_jar = new CaseInsensitiveFile(update_bak_libs_dir, safsupdatejar).toFile();
			FileUtilities.copyFileToFile(safsupdate_jar, safsupdate_backup_jar);

			File javadir = new CaseInsensitiveFile(rootdir, "Java/jre/bin").toFile();
			if(javadir.isDirectory()) javaexe = javadir.getAbsolutePath()+"/java";

			//Update selenium-plus library
			int library_update = updateLibrary(shell, store, javaexe, safsupdate_backup_jar.toString(), rootdir.toString(), timeout);

			//Refresh the "Java build path" for SeleniumPlus projects
			if (library_update>0) {
				//Update the source code if there are some jar files updated.
				updateSource(shell, store, javaexe, safsupdate_backup_jar.toString(), rootdir.toString(), timeout);
				//refresh build path
				CommonLib.refreshBuildPath();
				TopMostOptionPane.showConfirmDialog(null, "SeleniumPlus refreshed Java Build Path successfully.",
						"SeleniumPlus Java Build Path Refresh Complete", JOptionPane.CLOSED_OPTION);
			}

			//Update selenium-plus plugin
			updatePlugin(shell, store, javaexe, safsupdate_backup_jar.toString(), timeout);

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
			if(shell!=null && shell.getMinimized()) shell.setMinimized(false);
		}
	}

	private static int updateLibrary(Shell shell, IPreferenceStore store, String javaexe, String safsupdate_jar, String destdir, int timeout) throws URISyntaxException, ExecutionException{
		if(!store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_LIB)){
			return UPDATE_CODE_NON_ENABLED;
		}

		String url = store.getString(PreferenceConstants.UPDATESITE_LIB_URL);
		url = url==null ? null: url.trim();

		return update(shell, javaexe, true, true, "SeleniumPlus Library Update",
				safsupdate_jar, url, destdir, timeout);
	}

	private static int updateSource(Shell shell, IPreferenceStore store, String javaexe, String safsupdate_jar, String destdir, int timeout) throws URISyntaxException, ExecutionException{
		if(!store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_SOURCE_CODE)){
			return UPDATE_CODE_NON_ENABLED;
		}

		String url = store.getString(PreferenceConstants.UPDATESITE_SOURCECODE_URL);
		url = url==null ? null: url.trim();

		return update(shell, javaexe, true, true, "SeleniumPlus Source Code Update",
				safsupdate_jar, url, destdir, timeout);
	}

	private static int updatePlugin(Shell shell, IPreferenceStore store, String javaexe, String safsupdate_jar, int timeout) throws URISyntaxException, ExecutionException{
		if(!store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_PLUGIN)){
			Activator.log("Update Plugin is not enbaled.");
			return UPDATE_CODE_NON_ENABLED;
		}

		String eclipseDir = System.getProperty("user.dir");//SeleniumPlus embedded Eclipse home directory
		File plugindir = new CaseInsensitiveFile(eclipseDir, "plugins").toFile();

		if(!plugindir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS/plugins directory at: "+plugindir.getAbsolutePath());
		}

		String destdir = plugindir.toString();

		String url = store.getString(PreferenceConstants.UPDATESITE_PLUGIN_URL);
		url = url==null ? null: url.trim();

		if(! shell.getMinimized()) shell.setMinimized(true);

		int plugin_update = update(shell, javaexe, false, false, "SeleniumPlus Plugin Update",
				               safsupdate_jar, url, destdir, timeout);

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

	private static String getBackupDir(String destdir) throws ExecutionException{
		File backupdir = new CaseInsensitiveFile(destdir, update_bak).toFile();
		if(!backupdir.exists()){
			backupdir.mkdir();
		}

		if(!backupdir.isDirectory()){
			Activator.log("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS backup directory at: "+backupdir.getAbsolutePath());
			throw new ExecutionException("UpdateSeleniumPlus cannot deduce valid SELENIUM_PLUS backup directory at: "+backupdir.getAbsolutePath());
		}

		return backupdir.getAbsolutePath();
	}

	/**
	 * Get the JVM HTTP PROXY settings, such as -Dhttp.proxyHost=proxy.server.host -Dhttp.proxyPort=80 -Dhttp.nonProxyHosts="local.site.1|local.site.2|*.domain"
	 * @return String, the JVM HTTP PROXY settings; "" if PROXY is not found.
	 */
	private static String getProxySettings(URI updateURI){
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
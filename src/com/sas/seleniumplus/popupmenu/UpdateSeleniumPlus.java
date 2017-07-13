package com.sas.seleniumplus.popupmenu;

/**
 * JUL 10, 2015 (Carl Nagle) Refactor getProxySettings and execute to use existing System network configuration provided by Eclipse.
 * SEP 03, 2015 (Carl Nagle) trim lib url and plugin url from Preferences to prevent update errors.
 * APR 25, 2017 (Lei Wang) Moved most code to CommonLib.
 */

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;

public class UpdateSeleniumPlus extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell shell = CommonLib.getShell();
		try {
			int timeout = CommonLib.getUpdateTimeout();

			CommonLib.copyUpdateJar(Activator.seleniumhome);

			//Update selenium-plus library
			int library_update = CommonLib.updateLibrary(Activator.seleniumhome, timeout);
			int source_update = CommonLib.UPDATE_CODE_ERROR;

			if(library_update>0 ||
			   library_update==CommonLib.UPDATE_CODE_NON_ENABLED){
				//Update the source code if
				//1. there are some jar files updated.
				//2. or library update is not enabled, we should still try to update source
				source_update = CommonLib.updateSource(Activator.seleniumhome, timeout);
			}

			//Refresh the "Java build path" for SeleniumPlus projects, if library or source get updated
			if (library_update>0 || source_update>0) {
				CommonLib.refreshBuildPath();
				TopMostOptionPane.showConfirmDialog(null, "SeleniumPlus refreshed Java Build Path successfully.",
						"SeleniumPlus Java Build Path Refresh Complete", JOptionPane.CLOSED_OPTION);
			}

			//Update selenium-plus plugin
			CommonLib.updatePlugin(CommonLib.getPluginDir(), timeout);

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

}
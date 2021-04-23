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
package com.sas.seleniumplus.popupmenu;

/**
 * JUL 10, 2015 (CarlNagle) Refactor getProxySettings and execute to use existing System network configuration provided by Eclipse.
 * SEP 03, 2015 (CarlNagle) trim lib url and plugin url from Preferences to prevent update errors.
 * APR 25, 2017 (LeiWang) Moved most code to CommonLib.
 * JUL 05, 2018 (LeiWang) Don't install ghostscript inside CommonLib.updateLibrary(), install it by CommonLib.updateGhostscript().
 * JUL 10, 2018 (LeiWang) Modified execute(): catch NoClassDefFoundError and restart the workbench.
 */

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.safs.install.ConsumOutStreamProcess;

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

			//Update/Install new tools
			CommonLib.updateGhostscript(Activator.seleniumhome, null, ConsumOutStreamProcess.WAIT_FOREVER, true, true, true);

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
		}catch(NoClassDefFoundError ncfe){
			Activator.error("SeleniumPlus PlugIn restarted due to error\n"+ncfe.getClass().getSimpleName()+":"+ncfe.getMessage());
			try{
				Object[] options = {"OK"};
				TopMostOptionPane.showOptionDialog(null,
						"SeleniumPlus PlugIn met an error during update!\n"+
								"SeleniumPlus will restart, please update again.\n\n",
								"Update Requires Refresh",
								JOptionPane.YES_OPTION,
								JOptionPane.ERROR_MESSAGE,
								null,
								options,
								options[0]);
			}catch(Throwable th){

			}finally{
				PlatformUI.getWorkbench().restart();
			}

			return null;
		}
		finally{
			if(shell!=null && shell.getMinimized()) shell.setMinimized(false);
		}
	}

}

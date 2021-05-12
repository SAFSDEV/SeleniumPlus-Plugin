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


import java.io.File;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.safs.projects.seleniumplus.projects.BaseProject;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;

/**
 * @author CarlNagle
 */
public class LaunchProcessContainer extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		try{
			IVMInstall vm = JavaRuntime.getDefaultVMInstall();
			IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
			IProject iproject = Activator.getSelectedProject(null);
			if(iproject == null){
				JOptionPane.showConfirmDialog(null, "A SeleniumPlus Project must be selected.",
						                            "Invalid Project", JOptionPane.OK_OPTION);
				throw new ExecutionException("A SeleniumPlus Project must be selected.");
			}
			File rootdir = Activator.getProjectLocation(iproject);
			String iniFileName = BaseProject.TESTINI_FILE;
			File inifile = new CaseInsensitiveFile(rootdir, iniFileName).toFile();
			if(inifile == null || !inifile.isFile())
				throw new ExecutionException("Did not detect a SAFS Project '"+iniFileName+"' file in the Project root directory.");
			IJavaProject jproject = JavaCore.create(iproject);
			String[] jars = JavaRuntime.computeDefaultRuntimeClassPath(jproject);
			String jarslog = "";
			for(String jar:jars) jarslog += jar +"\n";
			Activator.log("LaunchProcessContainer: The Computed Default Runtime Classpath: \n"+ jarslog);

			VMRunnerConfiguration config = new VMRunnerConfiguration("org.safs.selenium.spc.WDSPC", jars);
			config.setWorkingDirectory(rootdir.getAbsolutePath());
			config.setVMArguments(new String[]{"-Dsafs.project.config="+ inifile.getAbsolutePath()});
			ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
			vmr.run(config,  launch,  null);

		}catch(Exception x){
			Activator.log("LaunchProcessContainer: Failed to Launch ProcessContainer due to "+x.getClass().getName()+": "+x.getMessage(), x);
			ExecutionException e = new ExecutionException(x.getMessage());
			e.initCause(x);
			throw e;
		}
		return null;
	}
}

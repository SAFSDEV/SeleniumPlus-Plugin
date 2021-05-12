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
import org.safs.android.auto.lib.Process2;
import org.safs.tools.CaseInsensitiveFile;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.projects.BaseProject;

public class StartStaf extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String stafdir = System.getenv(BaseProject.STAFDIR_ENV);

		File rootdir = new CaseInsensitiveFile(stafdir).toFile();
		if(!rootdir.isDirectory()){
			Activator.log("StartSTAF cannot deduce STAFDIR install directory at: "+rootdir.getAbsolutePath());
			throw new ExecutionException("StartSTAF cannot deduce STAFDIR install directory at: "+rootdir.getAbsolutePath());
		}

		File bindir = new File(rootdir, "bin");
		if(!bindir.isDirectory()){
			Activator.log("StartSTAF cannot deduce STAFDIR/bin directory at: "+bindir.getAbsolutePath());
			throw new ExecutionException("StartSTAF cannot deduce STAFDIR/bin  directory at: "+ bindir.getAbsolutePath());
		}

		String stafexe = "STAFProc";
		File stafFile = new CaseInsensitiveFile(bindir, stafexe).toFile();
		if(stafFile.isDirectory()) stafexe = stafFile.getAbsolutePath()+"/STAFProc";

		String consoledir = null;
		IProject iproject = Activator.getSelectedProject(null);
		if(iproject == null){
			JOptionPane.showConfirmDialog(null, "A SeleniumPlus Project must be selected.",
					                            "Invalid Project", JOptionPane.CANCEL_OPTION);
			throw new ExecutionException("A SeleniumPlus Project must be selected.");
		}
		File projectroot = Activator.getProjectLocation(iproject);
		if(projectroot != null ){
			consoledir = projectroot.getAbsolutePath();
		}else{
			consoledir = rootdir.getAbsolutePath();
		}
		Activator.log("StartSTAF runtime consoles expected at "+ consoledir);

		try{
			String cmdline = stafexe ;

			Activator.log("StartSTAF launching STAF with cmdline: "+ cmdline);
			new Process2(Runtime.getRuntime().exec(cmdline), true).discardStderr().discardStdout();
		}catch(Exception x){
			Activator.log("StartSTAF failed to launch STAF due to "+x.getClass().getName()+": "+x.getMessage(), x);
			ExecutionException e = new ExecutionException(x.getMessage());
			e.initCause(x);
			throw e;
		}
		return null;

	}

//	@Override
//	public Object execute(ExecutionEvent event) throws ExecutionException {
//
//		String selenv = System.getenv(BaseProject.STAFDIR_ENV);
//
//		// Need to support Unix/Linux/Mac
//		boolean isWin = true;
//		if(isWin){
//			try {
//				Runtime.getRuntime().exec("cmd.exe /c start "+selenv+"/startSTAFProc.bat");
//			} catch (Exception e) {
//				System.out.println("StartSTAF failed to execute "+ e.getMessage());
//			}
//		}else{
//			System.out.println("StartSTAF only valid on Windows OS at this time.");
//		}
//
//		return null;
//	}
}


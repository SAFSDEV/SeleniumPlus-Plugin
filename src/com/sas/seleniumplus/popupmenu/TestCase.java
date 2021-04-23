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

import java.io.InputStream;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.projects.BaseProject;

public class TestCase extends AbstractHandler {

	private String projectName = "";
	String mapPkgName;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String fileName = "";
		IProgressMonitor monitor = null;
		final Shell shell = HandlerUtil.getActiveShell(event);
		IResource resource =Activator.getSelectedResource(null);
		if(resource == null){
			Activator.log("Create TestCase did not detect a valid selected project package.");
			MessageDialog.openInformation(shell, "Invalid SeleniumPlus Package",
			          "A SeleniumPlus project PACKAGE must be selected.");
			throw new ExecutionException("A SeleniumPlus project PACKAGE must be selected.");
		}
		IProject project = resource.getProject();
		if(project == null){
			Activator.log("Create TestCase did not detect a valid selected Project.");
			MessageDialog.openInformation(shell, "Invalid SeleniumPlus Project",
					"A SeleniumPlus Project must be selected.");
			throw new ExecutionException("A SeleniumPlus Project must be selected.");
		}
		projectName = project.getName();
		mapPkgName = Map.getMapPackageName(project) + "."+BaseProject.MAPCLASS_FILE;

		// so what is our IResource, anyway?
		Activator.log("Create TestCase user selected resource: "+ resource.getProjectRelativePath().toPortableString());
		Activator.log("Create TestCase selected project: "+ projectName);
		Activator.log("Create TestCase project Map: "+ mapPkgName);

		String packageName = Activator.getSelectedSourcePackage(resource);
		Activator.log("Create TestCase detected Resource packageName: "+ packageName);

		TestCaseWizard dialog = new TestCaseWizard(shell);

		if (packageName != null && packageName.length() > 0 ){
			dialog.setPackageName(packageName);
		} else {
			dialog.setPackageName("default package");
		}

		if (dialog.open() == Window.OK) {
			fileName = dialog.getTestClassName();
		} else{
			Activator.log("Create TestCase dialog may have been cancelled by User.");
			return null;
		}
		Activator.log("Create TestCase class name provided by user: "+ fileName);

		//final String newfilename = fileName.substring(0, 1).toUpperCase() + fileName.substring(1).toLowerCase();
		final String newfilename = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
		Activator.log("Create TestCase modifed for class filename: "+ newfilename);

		final IFile file = resource.getParent().getFile(new Path(resource.getName()+"/"+ newfilename+".java"));

		if (file.exists()) {
 			Activator.log("Create TestCase class '"+ newfilename +"' already exists.");
			MessageDialog.openInformation(shell, "Info",
			          "Test Class '"+ newfilename +"' already exists in the package.");
			return null;
		}

		try {
			InputStream stream;

			String check = "."+packageName +".";
			if (check.contains("."+BaseProject.SRC_TESTRUNS_SUBDIR+".")) {
				Activator.log("Create TestCase getting file template 'FileTemplates.testRunClass' using: "+ projectName +", "+ packageName +", "+ mapPkgName +", "+ newfilename);
				stream = FileTemplates.testRunClass(projectName,packageName,mapPkgName,newfilename);
			} else {
				Activator.log("Create TestCase getting file template 'FileTemplates.testClass' using: "+ projectName +", "+ packageName +", "+ mapPkgName +", "+ newfilename);
				stream = FileTemplates.testClass(projectName,packageName,mapPkgName,newfilename);
			}

			file.create(stream, true, monitor);
			Activator.log("Create TestCase finished creating file from FileTemplate stream.");
			stream.close();
		} catch (Exception e) {
			Activator.log("Create TestCase failed to create file from FileTemplate stream: "+ e.getClass().getSimpleName()+", "+e.getMessage());
			MessageDialog.openInformation(shell, "Create File Problem",
                    "TestCase '"+ newfilename +"' may not have successfully been created from FileTemplate.");
			throw new ExecutionException("TestCase '"+ newfilename +"' may not have successfully been created from FileTemplate.");
		}

		Activator.log("Create TestCase opening new Editor for newly created testclass '"+newfilename+".java'");
		shell.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
					Activator.log("Create TestCase open Editor "+e.getClass().getSimpleName()+", "+e.getMessage());
					MessageDialog.openInformation(shell, "Create File Problem",
		                    "TestCase '"+ newfilename +"' may not have successfully been created from FileTemplate.");
				}
			}
		});

		return null;
	}
}

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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.projects.BaseProject;

public class MapOrder extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IProgressMonitor monitor = null;
		Shell shell = HandlerUtil.getActiveShell(event);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		ISelection iSelection = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService().getSelection();

		Object firstElement = ((IStructuredSelection) iSelection).getFirstElement();

		IPath loc =  ((IFolder) firstElement).getFullPath();

		IResource resource = root.findMember(loc);

		String projectName = null;
		if(firstElement instanceof IAdaptable){
			IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
			if(project==null) project = resource.getProject();
			if(project==null){
				Activator.error("MapOrder.execute(): could not deduce the project.");
				return null;
			}
			projectName = project.getName();
		}

		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(BaseProject.APPMAP_ORDER_FILE));
		try {
			InputStream stream = FileTemplates.appMapOrder(projectName);
			if (file.exists()) {
				 MessageDialog.openInformation(shell, "Info",
				          file.getName() +" already exists.");
				 return null;
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (Exception e) {
		}

		shell.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});

		return null;
	}
}

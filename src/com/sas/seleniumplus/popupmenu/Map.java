package com.sas.seleniumplus.popupmenu;

import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.sas.seleniumplus.Activator;

public class Map extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String fileName = "";
		String packageName = "";
		IProgressMonitor monitor = null;
		Shell shell = HandlerUtil.getActiveShell(event);
		MapWizard dialog = new MapWizard(shell);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		ISelection iSelection = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService().getSelection();

		Object o = ((IStructuredSelection) iSelection).getFirstElement();

		IPath loc =  ((Folder) o).getFullPath();

		IResource resource = root.findMember(loc);

		IContainer container = (IContainer) resource;

		packageName = container.getFullPath().toOSString();

		dialog.setPackageName(packageName);

		if (dialog.open() == Window.OK) {
			fileName = dialog.getTestClassName();
		} else{
			return null;
		}

		//String newfilename = fileName.substring(0, 1).toUpperCase() + fileName.substring(1).toLowerCase();

		final IFile file = container.getFile(new Path(fileName + ".map"));
		try {
			InputStream stream = FileTemplates.appMap();
			if (file.exists()) {
				 MessageDialog.openInformation(shell, "Info", file.getName() +" already exists.");
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

	/**
	 * Returns the dot-separated package name of the package expected to hold the Map file.
	 * <p>
	 * This is normally the package name (whereever it is in the path) that is the lower-case
	 * equivalent of the Project name.
	 * <p>
	 * Example:
	 * <p>
	 * <ul>
	 * sample<br>
	 * com.sas.sample<br>
	 * com.company.project<br>
	 * </ul>
	 * @param iproject
	 * @return the dot-separated package name of the package expected to hold the Map file.
	 * @see Package#getDefaultPackageName(IProject)
	 */
	public static String getMapPackageName(IProject iproject){
		 IJavaProject javaProject = JavaCore.create(iproject);
		 String packageTest = iproject.getName().toLowerCase();
		 //Activator.log("Map.getMapPackageName using packageTest value:"+ packageTest);
		 String rootName = null;
		 try {
			for (IPackageFragment root : javaProject.getPackageFragments()) {
				 if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					 rootName = root.getElementName();
					 //Activator.log("Map.getMapPackageName evaluating: "+ root.getElementName());
					 if (rootName.endsWith("."+ packageTest) || rootName.equals(packageTest)){
						 //Activator.log("Map.getMapPackageName matched on: "+ rootName);
						 return rootName;
					 }
				 }
			 }
		} catch (JavaModelException e) {
		}
		return null;
	}

}

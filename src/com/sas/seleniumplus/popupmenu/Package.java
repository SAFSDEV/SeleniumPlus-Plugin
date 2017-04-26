package com.sas.seleniumplus.popupmenu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sas.seleniumplus.Activator;

public class Package extends AbstractHandler{

	private Shell shell;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		shell = HandlerUtil.getActiveShell(event);
		PackageWizard dialog = new PackageWizard(shell);
		IResource resource = Activator.getSelectedResource(null);
		IProject project = resource.getProject();

		String defaultPkgName = getDefaultPackageName(project);
		String packageName = Activator.getSelectedSourcePackage(resource);

		if(packageName == null || packageName.length() ==0){
			dialog.setPrePackageName(defaultPkgName);
		}else{
			dialog.setPrePackageName(packageName);
		}

		if (dialog.open() == Window.OK) {
			String temp = dialog.getPackageName();
			packageName = temp.replace(".", "/");
		} else{
			return null;
		}
		IFolder sourceroot = Activator.getRootSourceFolder(project);
		IFile file = sourceroot.getFile(new Path(packageName + "/foo"));
		prepare((IFolder) file.getParent());

		return null;
	}

	public void prepare(IFolder folder) {
	    if (!folder.exists()) {
	        prepare((IFolder) folder.getParent());
	        try {
				folder.create(false, false, null);
			} catch (CoreException e) {
				 MessageDialog.openInformation(shell, "Info",
				          "Package not created at " + folder.toString() );
				 return;
			}
	    }
	}

	public static String getDefaultPackageName(IProject iproject){

		 IJavaProject javaProject = JavaCore.create(iproject);
		 String packageTest = iproject.getName().toLowerCase();
		 String rootName = null;
		 try {
			for (IPackageFragment root : javaProject.getPackageFragments()) {
				 if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					 rootName = root.getElementName();
					 if (rootName.endsWith("." + packageTest) ||
					     rootName.equals(packageTest)){
						 return rootName;
					 }
				 }
			 }
		} catch (JavaModelException e) {
		}

		 return null;
		}
}

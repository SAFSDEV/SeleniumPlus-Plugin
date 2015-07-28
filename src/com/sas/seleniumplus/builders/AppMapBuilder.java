package com.sas.seleniumplus.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import com.sas.seleniumplus.projects.BaseProject;

public class AppMapBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = AppMapBuilder.class.getName();

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {

		//System.out.println("Custom builder triggered");
		String srcDir = "";

		String projectPath = getProject().getLocation().toString();
		if (getProject().getFolder(BaseProject.SRC_SRC_DIR).exists()){
			srcDir = "/"+ BaseProject.SRC_SRC_DIR +"/";
		} else if (getProject().getFolder(BaseProject.SRC_TEST_DIR).exists()){
			srcDir = "/"+ BaseProject.SRC_TEST_DIR +"/";
		} else {
			srcDir = "/"+ BaseProject.SRC_TEST_DIR +"/";
		}	

		String packageName = null;
		String prjNameTest = null;
		String lcPrjName = getProject().getName().toLowerCase();

		IJavaProject javaProject = JavaCore.create(getProject());
		IPackageFragment[] packages = javaProject.getPackageFragments();

		for (IPackageFragment root : packages) {			 
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				// sometimes the project name is FIRST with no leading "."
				prjNameTest = "."+ root.getElementName();
				if (prjNameTest.endsWith("." + lcPrjName)){
					packageName = root.getElementName();					
					break;
				}
			}			 
		}

		//Try to generate Map file to parent folder of package xxx.testcases
		if (packageName == null){
			for (IPackageFragment root : packages) {			 
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					prjNameTest = root.getElementName();
					if (prjNameTest.endsWith("." + BaseProject.SRC_TESTCASES_SUBDIR)){
						packageName = prjNameTest.substring(0, prjNameTest.indexOf("." + BaseProject.SRC_TESTCASES_SUBDIR));
						break;
					}
				}			 
			}
		}

		List<String> params = new ArrayList<String>();
		params.add("-in");
		params.add(projectPath + "/"+BaseProject.DATAPOOL_DIR);
		params.add("-name");
		params.add(BaseProject.MAPCLASS_FILE);
		if (packageName!=null && !packageName.trim().isEmpty()){
			String packageDir = packageName.replace(".", "/");
			params.add("-package");
			params.add(packageName);
			params.add("-out");
			params.add(projectPath + srcDir + packageDir);
		}else{
			//If no package can be found, then generate the Map file to the default package
			params.add("-out");
			params.add(projectPath + srcDir);
			//TODD It is better to show some warning message to user that the Map.java is generated in the default package.
		}
		
//		for (String string : params) {
//			System.out.println(string);
//		}

		org.safs.model.tools.ComponentGenerator.main(params.toArray(new String[0]));
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		return null;
	}

}

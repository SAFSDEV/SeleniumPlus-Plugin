package com.sas.seleniumplus.eclipse;

import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.safs.projects.common.projects.pojo.POJOFolder;
import org.safs.projects.common.projects.pojo.POJOPackageFragment;
import org.safs.projects.common.projects.pojo.POJOPath;
import org.safs.projects.common.projects.pojo.POJOProject;

/**
 * Holds an Eclipse IProject and delegates calls to it.
 *
 */
public class IProjectHolder extends POJOProject {
	private IProject project;

	public IProjectHolder(IProject project) {
		super(project.getName());
		this.project = project;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public POJOFolder getFolder(String path) {
		IFolder folder = project.getFolder(path);
		return new IFolderHolder(folder);
	}

	public IProject getIProject() {
		return project;
	}

	@Override
	public POJOPath getLocation() {
		IPath path = project.getLocation();
		return new IPathHolder(path);
	}
	
	@Override
	public POJOPackageFragment[] getPackageFragments() throws Exception {
		IJavaProject javaProject = JavaCore.create(project);
		IPackageFragment[] ipackages = javaProject.getPackageFragments();
		ArrayList<POJOPackageFragment> packages = new ArrayList<POJOPackageFragment>();
		for (IPackageFragment iPackageFragment : ipackages) {
			if (iPackageFragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
				packages.add(new IPackageFragmentHolder(iPackageFragment));
			}
		}
		return packages.toArray(new POJOPackageFragment[0]);
	}
}

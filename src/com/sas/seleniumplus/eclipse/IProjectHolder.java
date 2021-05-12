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

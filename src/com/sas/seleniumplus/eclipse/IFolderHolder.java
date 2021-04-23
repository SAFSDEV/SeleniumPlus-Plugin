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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.safs.projects.common.projects.pojo.POJOContainer;
import org.safs.projects.common.projects.pojo.POJOFile;
import org.safs.projects.common.projects.pojo.POJOFolder;

/**
 * Holds an Eclipse IFolder and delegates calls to it.
 *
 */
public class IFolderHolder extends POJOFolder {
	private IFolder folder;

	public IFolderHolder(IFolder folder) {
		this.folder = folder;
	}

	@Override
	public String toString() {
		return folder.toString();
	}

	@Override
	public boolean exists() {
		return folder.exists();
	}

	@Override
	public POJOFile getFile(String path) {
		IFile file = folder.getFile(path);
		POJOFile fileHolder = new IFileHolder(file);
		return fileHolder;
	}

	public IFolder getIFolder() {
		return folder;
	}

	@Override
	public String getPath(){
		if(folder!=null && folder.exists()) return folder.getFullPath().toFile().getAbsolutePath();
		return null;
	}

	@Override
	public POJOContainer getParent() {
		IContainer container = folder.getParent();
		return new IContainerHolder(container);
	}
}

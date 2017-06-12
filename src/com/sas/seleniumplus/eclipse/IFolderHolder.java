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
	public POJOContainer getParent() {
		IContainer container = folder.getParent();
		return new IContainerHolder(container);
	}
}

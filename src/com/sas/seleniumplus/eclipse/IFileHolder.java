package com.sas.seleniumplus.eclipse;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safs.projects.common.projects.pojo.POJOFile;

/**
 * Holds an Eclipse IFile and delegates calls to it.
 *
 */
class IFileHolder extends POJOFile {
	private IFile file;

	public IFileHolder(IFile file) {
		this.file = file;
	}
	
	@Override
	public void create(InputStream source, boolean force, Object monitor) throws Exception {
		file.create(source, force, (IProgressMonitor) monitor);
	}
}

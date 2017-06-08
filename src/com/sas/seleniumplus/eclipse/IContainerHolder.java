package com.sas.seleniumplus.eclipse;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.safs.projects.common.projects.pojo.POJOContainer;
import org.safs.projects.common.projects.pojo.POJOFile;
import org.safs.projects.common.projects.pojo.POJOPath;

/**
 * Holds an Eclipse IContainer and delegates calls to it.
 *
 */
public class IContainerHolder extends POJOContainer {
	private IContainer container;

	public IContainerHolder(IContainer container) {
		this.container = container;
	}

	@Override
	public POJOFile getFile(POJOPath path) {
		IFile file = container.getFile(((IPathHolder) path).getPath());
		return new IFileHolder(file);
	}

	public IContainer getIContainer() {
		return container;
	}
}

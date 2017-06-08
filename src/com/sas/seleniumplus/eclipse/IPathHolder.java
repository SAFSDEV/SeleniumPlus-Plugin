package com.sas.seleniumplus.eclipse;

import org.eclipse.core.runtime.IPath;
import org.safs.projects.common.projects.pojo.POJOPath;

/**
 * Holds an Eclipse IPath and delegates calls to it.
 *
 */
class IPathHolder extends POJOPath {
	private IPath path;

	public IPathHolder(IPath path) {
		this.path = path;
	}

	public IPath getPath() {
		return path;
	}

	@Override
	public String toString() {
		return path.toString();
	}
}

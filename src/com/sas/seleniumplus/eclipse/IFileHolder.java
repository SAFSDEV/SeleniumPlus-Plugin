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

	@Override
	public String getPath(){
		if(file!=null && file.exists()) return file.getFullPath().toFile().getAbsolutePath();
		return null;
	}
}

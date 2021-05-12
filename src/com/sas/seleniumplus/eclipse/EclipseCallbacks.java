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

import static com.sas.seleniumplus.projects.BaseProject.srcDir;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.safs.projects.common.projects.callbacks.Callbacks;
import org.safs.projects.common.projects.callbacks.CreateFolderCallback;
import org.safs.projects.common.projects.callbacks.CreatePathCallback;
import org.safs.projects.common.projects.callbacks.CreateProjectCallback;
import org.safs.projects.common.projects.pojo.POJOPath;
import org.safs.projects.common.projects.pojo.POJOProject;

import com.sas.seleniumplus.projects.BaseProject;

/**
 * Creates the callbacks needed to perform operations with the Eclipse
 * API such as creating projects and folders.
 *
 */
public class EclipseCallbacks extends Callbacks {

	public EclipseCallbacks(final String projectName, final URI location) {
		createProjectCallback = new CreateProjectCallback() {

			@Override
			public POJOProject createProject() {
				IProject project =  BaseProject.createBaseProject(projectName,srcDir,location);
				IProjectHolder projectHolder = new IProjectHolder(project);

				try {
					BaseProject.addNature(project);
				} catch (Exception e) {
					e.printStackTrace();
					project = null;
				}
				return projectHolder;
			}

		};

		createFolderCallback = new CreateFolderCallback() {

			@Override
			public void createFolder(POJOProject project, String path) throws Exception {
				IFolderHolder etcFolders = (IFolderHolder) project.getFolder(path);
				BaseProject.createFolder(etcFolders);
			}

		};

		createPathCallback = new CreatePathCallback() {

			@Override
			public POJOPath createPath(String pathStr) {
				Path eclipsePath = new Path(pathStr);
				IPathHolder pathHolder = new IPathHolder(eclipsePath);
				return pathHolder;
			}
		};

	}
}

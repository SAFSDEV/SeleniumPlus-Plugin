package com.sas.seleniumplus.eclipse;

import static com.sas.seleniumplus.projects.BaseProject.srcDir;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import org.safs.projects.common.projects.callbacks.Callbacks;
import org.safs.projects.common.projects.callbacks.CreateFolderCallback;
import org.safs.projects.common.projects.callbacks.CreatePathCallback;
import org.safs.projects.common.projects.callbacks.CreateProjectCallback;
import org.safs.projects.common.projects.callbacks.GetFolderCallback;
import org.safs.projects.common.projects.pojo.POJOFolder;
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

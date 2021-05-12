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
package com.sas.seleniumplus.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;
import com.sas.seleniumplus.natures.ProjectNature;

public class ProjectAddListener implements IResourceChangeListener {

	public static final ProjectAddListener LISTENER = new ProjectAddListener();

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if(event.getType() == IResourceChangeEvent.POST_CHANGE){
			final List<IProject> projects = getProjects(event.getDelta());
			Activator.log("ProjectListener received "+ projects.size() +" newly ADDED SeleniumPlus Projects.");
			if(projects.isEmpty()) return;
			new java.util.concurrent.ScheduledThreadPoolExecutor(1).schedule(new Runnable(){
				@Override
				public void run() {
					try{
						IClasspathEntry[] jars = CommonLib.getLatestSeleniumPlusJARS();

						String jmsg = "";
						if(jars != null && jars.length > 0) {
							for(IClasspathEntry e:jars){
								jmsg += e.getPath().toFile().getName() +"\n";
							}
							jmsg += "\n";
						}else{
							throw new ExecutionException("ProjectAddListener.resourceChanged UNABLE to retrieve latest SeleniumPlus JARs.");
						}

						for(IProject iP: projects){
							if(CommonLib.refreshBuildPath(iP, jars)){
								Activator.log("ProjectAddListener.resourceChanged successfully refreshed the SeleniumPlus Project JARS.");
								String msg = "Following jars added to the build path for Project "+ iP.getName()+"\n\n"+ jmsg;
								JOptionPane.showConfirmDialog(null, msg, "Build path updated.", JOptionPane.CLOSED_OPTION);
							}else{
								Activator.log("ProjectAddListener.resourceChanged did NOT successfully refresh the SeleniumPlus Project JARS.");
								String msg = "Build Path NOT refreshed for Project "+ iP.getName()+"\n";
								JOptionPane.showConfirmDialog(null, msg, "Build path NOT updated.", JOptionPane.CLOSED_OPTION);
							}
						}
					}catch(ExecutionException xe){
						Activator.log("Ignoring ProjectAddListener.resourceChanged "+ xe.getClass().getName()+", "+xe.getMessage());
					}catch(CoreException ce){
						Activator.log("Ignoring ProjectAddListener.resourceChanged "+ ce.getClass().getName()+", "+ce.getMessage());
					}
				}
			}, 10, TimeUnit.MILLISECONDS);
		}
	}

	private List<IProject> getProjects(IResourceDelta delta) {
		final List<IProject> projects = new ArrayList<IProject>();
		try{
			delta.accept(new IResourceDeltaVisitor(){
				public boolean visit(IResourceDelta delta) throws CoreException{
					if(delta.getKind() == IResourceDelta.ADDED && delta.getResource().getType() == IResource.PROJECT){
						IProject project = (IProject) delta.getResource();
						if(project.isAccessible() &&
						   project.hasNature(ProjectNature.NATURE_ID)){
							projects.add(project);
						}
					}
					return delta.getResource().getType() == IResource.ROOT;
				}
			});
		}catch(CoreException ce){
			Activator.log("ProjectListener.getProjects() ignoring "+ ce.getClass().getName()+", "+ce.getMessage());
		}
		return projects;
	}
}

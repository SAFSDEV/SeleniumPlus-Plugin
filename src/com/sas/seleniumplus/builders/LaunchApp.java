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
package com.sas.seleniumplus.builders;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.projects.BaseProject;

@SuppressWarnings("restriction")
public class LaunchApp implements ILaunchShortcut  {

	@Override
	public void launch(ISelection selection, String mode) {

		Object o = ((IStructuredSelection) selection).getFirstElement();
		IJavaElement ij = ((IJavaElement)o);
		run(ij,mode);
	}

	@Override
	public void launch(IEditorPart editor, String mode) {

		IEditorInput input = editor.getEditorInput();
		IJavaElement o = (IJavaElement) input.getAdapter(IJavaElement.class);
		run(o,mode);
	}

	private void run(IJavaElement iJ, String mode){

		String LaunchRunConfig = "Selenium+";

		IJavaProject jProject = ((IJavaElement) iJ).getJavaProject();

		String selectedClassPath = ((IJavaElement) iJ).getPath().toString();
		String[] tempClass = selectedClassPath.split(jProject.getElementName() + "/"+BaseProject.SRC_TEST_DIR+"/");
		String classNametmp = tempClass[1].replace("/", ".");
		String className = classNametmp.substring(0, classNametmp.lastIndexOf("."));

		LaunchManager manager = (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type =
		 manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

		ILaunchConfigurationWorkingCopy wc;
		try {

			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
			   for (int i = 0; i < configurations.length; i++) {
			      ILaunchConfiguration configuration = configurations[i];
			      if (configuration.getName().equals(LaunchRunConfig)) {
			         configuration.delete();
			         break;
			      }
			   }

			wc = type.newInstance(null, LaunchRunConfig);
			wc.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					jProject.getElementName());
			wc.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					className);
			ILaunchConfiguration config = wc.doSave();
			config.launch(ILaunchManager.RUN_MODE, null);

		} catch (CoreException e) {
			Activator.log("Run As throws: " + e.getLocalizedMessage());
		}

	}

}

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

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class SeleniumProjectWithTestLevelNewWizard extends Wizard implements INewWizard, IExecutableExtension {

	private static final String PAGE_NAME = "Selenium+ Project Wizard";

	private static final String WIZARD_NAME = "New Selenium+ Project With Test Level";

	private static final String WIZARD_DESCRIPTION = "Create new Selenium+ Project With Test Level (Cycle, Suite, TestCase).";

	private SeleniumProjectWithTestLevelPageWizard _pageOne;

	private IConfigurationElement _configurationElement;


	@Override
	public void addPages() {
	    super.addPages();

	    _pageOne = new SeleniumProjectWithTestLevelPageWizard(PAGE_NAME);
	    _pageOne.setTitle(WIZARD_NAME);
	    _pageOne.setDescription(WIZARD_DESCRIPTION);
	    addPage(_pageOne);
	}

	public SeleniumProjectWithTestLevelNewWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		String selenv = System.getenv(BaseProject.SELENIUM_PLUS_ENV);
		if (selenv != null)	{

			File projectdir = new File(selenv);

			if (projectdir.exists()){
				BaseProject.SELENIUM_PLUS = selenv;
				BaseProject.STAFDIR = System.getenv(BaseProject.STAFDIR_ENV);
				return;
			}
		}

		MessageDialog.openError(getShell(), BaseProject.MSG_INSTALL_NOT_FOUND, BaseProject.MSG_INSTALL_AND_RESTART);
	}

	@Override
	public boolean performFinish() {

		String name = _pageOne.getProjectName().toUpperCase();
		URI location = null;
		if (!_pageOne.useDefaults()) {
			location = _pageOne.getLocationURI();

		} // else location == null

		BaseProject.createProject(name,location,"sas",BaseProject.PROJECTTYPE_TESTLEVEL);

		BasicNewProjectResourceWizard.updatePerspective(_configurationElement);

		return true;

	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		_configurationElement = config;
	}

}

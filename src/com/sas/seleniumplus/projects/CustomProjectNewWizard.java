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

public class CustomProjectNewWizard extends Wizard implements INewWizard, IExecutableExtension {

	private static final String PAGE_NAME = "Selenium+ Project Wizard";

	private static final String WIZARD_NAME = "New Selenium+ Project";

	private static final String WIZARD_DESCRIPTION = "Create new Selenium+ Project with all assets.";

	private WizardNewCustomProjectCreationPage _pageOne;

	private IConfigurationElement _configurationElement;


	@Override
	public void addPages() {
	    super.addPages();

	    _pageOne = new WizardNewCustomProjectCreationPage(PAGE_NAME);
	    _pageOne.setTitle(WIZARD_NAME);
	    _pageOne.setDescription(WIZARD_DESCRIPTION);

	    addPage(_pageOne);
	}

	public CustomProjectNewWizard() {
		setWindowTitle(WIZARD_NAME);

	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		String selenv = System.getenv(BaseProject.SELENIUM_PLUS_ENV);

		if (selenv != null)	{

			File projectdir = new File(selenv);

			if (projectdir.exists()){
				CustomProjectSupport.SELENIUM_PLUS = selenv;
				CustomProjectSupport.STAFDIR = System.getenv(BaseProject.STAFDIR_ENV);
				return;
			}
		}

		MessageDialog.openError(getShell(), BaseProject.MSG_INSTALL_NOT_FOUND, BaseProject.MSG_INSTALL_AND_RESTART);
	}

	@Override
	public boolean performFinish() {

		String name = _pageOne.getProjectName().toUpperCase();
		String companyName =  _pageOne.getCompanyInitialName();
		URI location = null;
		if (!_pageOne.useDefaults()) {
			location = _pageOne.getLocationURI();

		} // else location == null

		CustomProjectSupport.createProject(name, location,companyName);

		BasicNewProjectResourceWizard.updatePerspective(_configurationElement);

		return true;

	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		_configurationElement = config;
	}



}

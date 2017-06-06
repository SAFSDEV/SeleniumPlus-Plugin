package com.sas.seleniumplus.projects;

import static org.safs.Constants.ENV_SELENIUM_PLUS;
import static org.safs.seleniumplus.projects.BaseProject.SELENIUM_PLUS;

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

public class SampleProjectNewWizard extends Wizard implements INewWizard, IExecutableExtension {

	private static final String PAGE_NAME = "Selenium+ Sample Project Wizard";

	private static final String WIZARD_NAME = "Selenium+ Sample Project";

	private static final String WIZARD_DESCRIPTION = "Create Selenium+ Sample Project with all assets.";

	private SampleProjectPageWizard _pageOne;

	private IConfigurationElement _configurationElement;

	public static String PROJECT_NAME = "SAMPLE";


	@Override
	public void addPages() {
	    super.addPages();

	    _pageOne = new SampleProjectPageWizard(PAGE_NAME);
	    _pageOne.setTitle(WIZARD_NAME);
	    _pageOne.setDescription(WIZARD_DESCRIPTION);
	    addPage(_pageOne);
	}

	public SampleProjectNewWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		String selenv = System.getenv(ENV_SELENIUM_PLUS);

		if (selenv != null)	{

			File projectdir = new File(selenv);

			if (projectdir.exists()){
				SELENIUM_PLUS = selenv;
				BaseProject.STAFDIR = System.getenv(BaseProject.STAFDIR_ENV);
				return;
			}
		}

		MessageDialog.openError(getShell(), BaseProject.MSG_INSTALL_NOT_FOUND, BaseProject.MSG_INSTALL_AND_RESTART);
	}

	@Override
	public boolean performFinish() {

		String name = PROJECT_NAME;
		URI location = null;
		if (!_pageOne.useDefaults()) {
			location = _pageOne.getLocationURI();

		} // else location == null

		BaseProject.createProject(name, location,"sas", BaseProject.PROJECTTYPE_SAMPLE);

		BasicNewProjectResourceWizard.updatePerspective(_configurationElement);

		return true;

	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		_configurationElement = config;
	}



}

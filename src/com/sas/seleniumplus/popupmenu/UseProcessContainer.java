package com.sas.seleniumplus.popupmenu;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.sas.seleniumplus.projects.BaseProject;


public class UseProcessContainer extends AbstractHandler{

	String selenv = System.getenv(BaseProject.SELENIUM_PLUS_ENV);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			PlatformUI.getWorkbench().getBrowserSupport().createBrowser("UsingProcessContainer").openURL(new File(selenv + "/doc/UsingSeleniumProcessContainer.html").toURI().toURL());
		} catch (Exception e) {
			System.out.println("Action failed to execute "+ e.getMessage());
		}

		return null;
	}

}
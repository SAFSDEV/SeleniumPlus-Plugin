package com.sas.seleniumplus.popupmenu;

import static org.safs.Constants.ENV_SELENIUM_PLUS;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;



public class UseProcessContainer extends AbstractHandler{

	String selenv = System.getenv(ENV_SELENIUM_PLUS);

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
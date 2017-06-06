package com.sas.seleniumplus.popupmenu;

import static org.safs.Constants.ENV_SELENIUM_PLUS;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.sas.seleniumplus.Activator;

public class XpathGroupdoc  extends AbstractHandler {

String selenv = System.getenv(ENV_SELENIUM_PLUS);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			PlatformUI.getWorkbench().getBrowserSupport().createBrowser("XPATHDOCByGroups").openURL(new File(selenv + "/doc/Xpath_Css_selenium_Locators_groups_1_0_2.pdf").toURI().toURL());
		} catch (Exception e) {
			Activator.log("Action failed to execute "+ e.getMessage(), e);
		}

		return null;
	}
}
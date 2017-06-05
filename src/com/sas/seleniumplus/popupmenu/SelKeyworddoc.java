package com.sas.seleniumplus.popupmenu;

import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.sas.seleniumplus.Activator;
import static org.safs.seleniumplus.projects.BaseProject.SELENIUM_PLUS_ENV;

public class SelKeyworddoc  extends AbstractHandler {

String selenv = System.getenv(SELENIUM_PLUS_ENV);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			PlatformUI.getWorkbench().getBrowserSupport().createBrowser("SeleniumPlusKeywordDoc").openURL(new URL("http://safsdev.sourceforge.net/sqabasic2000/SAFSReference.php"));
		} catch (Exception e) {
			Activator.log("Action failed to execute "+ e.getMessage(), e);
		}

		return null;
	}
}

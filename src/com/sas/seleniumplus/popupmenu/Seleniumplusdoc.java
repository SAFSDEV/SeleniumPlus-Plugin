package com.sas.seleniumplus.popupmenu;

import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.sas.seleniumplus.Activator;


public class Seleniumplusdoc extends AbstractHandler{

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		try {
			PlatformUI.getWorkbench().getBrowserSupport().createBrowser("SeleniumPlusDoc").openURL(new URL("http://safsdev.sourceforge.net/selenium/doc/SeleniumPlus-Welcome.html"));		
		} catch (Exception e) {
			Activator.log("Action failed to execute "+ e.getMessage(), e);
		}
		
		return null;
	}	

}
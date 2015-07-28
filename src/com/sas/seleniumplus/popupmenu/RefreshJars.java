package com.sas.seleniumplus.popupmenu;

import java.io.File;
import javax.swing.JOptionPane;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
public class RefreshJars extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		CommonLib lib = new CommonLib();
		lib.refreshBuildPath();	
		return null;
	}
}
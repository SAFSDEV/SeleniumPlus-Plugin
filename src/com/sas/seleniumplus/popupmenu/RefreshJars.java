package com.sas.seleniumplus.popupmenu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;

import com.sas.seleniumplus.CommonLib;
public class RefreshJars extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = CommonLib.getShell();
		try{
			CommonLib.refreshBuildPath();
			return null;
		}finally{
			if(shell!=null && shell.getMinimized()) shell.setMinimized(false);
		}
	}
}
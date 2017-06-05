package com.sas.seleniumplus.popupmenu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import static org.safs.seleniumplus.projects.BaseProject.SELENIUM_PLUS_ENV;

public class StopServer extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String selenv = System.getenv(SELENIUM_PLUS_ENV);

		// TODO Need to support Unix/Linux/Mac
		boolean isWin = true;
		if(isWin){
			try {
				Runtime.getRuntime().exec("cmd.exe /c start "+selenv+"/extra/RemoteServerTerminate.bat");
			} catch (Exception e) {
				System.out.println("RemoteServerTerminate failed to execute "+ e.getMessage());
			}
		}else{
			System.out.println("RemoteServerTerminate only supported on Windows OS at this time.");
		}

		return null;
	}

}
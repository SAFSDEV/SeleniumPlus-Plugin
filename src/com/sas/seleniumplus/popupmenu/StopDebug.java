package com.sas.seleniumplus.popupmenu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import static org.safs.Constants.ENV_SELENIUM_PLUS;

public class StopDebug extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String selenv = System.getenv(ENV_SELENIUM_PLUS);

		// TODO Need to support Unix/Linux/Mac
		boolean isWin = true;
		if(isWin){
			try {
				Runtime.getRuntime().exec("cmd.exe /c start "+selenv+"/extra/DebugShutdown.bat");
			} catch (Exception e) {
				System.out.println("DebugShutdown failed to execute "+ e.getMessage());
			}
		}else{
			System.out.println("DebugShutdown only valid on Windows OS at this time.");
		}

		return null;
	}

}
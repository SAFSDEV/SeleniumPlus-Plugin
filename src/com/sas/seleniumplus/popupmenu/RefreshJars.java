package com.sas.seleniumplus.popupmenu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;
public class RefreshJars extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			int updatedFileNumber = CommonLib.updateSource(Activator.seleniumhome, CommonLib.getUpdateTimeout());
			Activator.log("Updated "+updatedFileNumber+" source files.");
		} catch (Exception e) {
			Activator.warn("Failed to update source code!");
		}
		CommonLib.refreshBuildPath();
		return null;
	}
}
package com.sas.seleniumplus.popupmenu;

import java.io.File;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import com.sas.seleniumplus.Activator;

/**
 * This class is not currently needed or used for SeleniumPlus running without STAF.
 * Debug Logging happens automatically if the following exists in the TEST.INI file:
 * <pre>
 * [STAF]
 * EMBEDDEBUG=TRUE
 * </pre>
 * However, this might need to be used re-enabled if using SeleniumPlus with other engines and STAF is in-use.
 *
 * @author Carl Nagle
 */
public class StartDebug extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try{
			IVMInstall vm = JavaRuntime.getDefaultVMInstall();
			IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
			IProject iproject = Activator.getSelectedProject(null);
			if(iproject == null){
				JOptionPane.showConfirmDialog(null, "A SeleniumPlus Project must be selected.",
						                            "Invalid Project", JOptionPane.OK_OPTION);
				throw new ExecutionException("A SeleniumPlus Project must be selected.");
			}
			File rootdir = Activator.getProjectLocation(iproject);
			IJavaProject jproject = JavaCore.create(iproject);
			String[] jars = JavaRuntime.computeDefaultRuntimeClassPath(jproject);
			String jarslog = "";
			for(String jar:jars) jarslog += jar +"\n";
			Activator.log("StartDebug: The Computed Default Runtime Classpath: \n"+ jarslog);

			VMRunnerConfiguration config = new VMRunnerConfiguration("org.safs.Log", jars);
			config.setWorkingDirectory(rootdir.getAbsolutePath());
			config.setVMArguments(new String[]{"-file:"+ rootdir.getAbsolutePath()+"/DebugLog.txt"});
			ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
			vmr.run(config,  launch,  null);

		}catch(Exception x){
			Activator.log("StartDebug: Failed to Debug Log due to "+x.getClass().getName()+": "+x.getMessage(), x);
			ExecutionException e = new ExecutionException(x.getMessage());
			e.initCause(x);
			throw e;
		}
		return null;
	}
}

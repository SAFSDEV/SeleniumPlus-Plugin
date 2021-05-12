/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-05-17    (LeiWang) Initial release.
 * @date 2018-05-18    (LeiWang) Write message/error-message to default console.
 * @date 2018-06-15    (LeiWang) Modified execute(): get "Test Count Unit" from preference to convert SAFS XML.
 */
package com.sas.seleniumplus.popupmenu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.safs.StringUtils;
import org.safs.tools.logs.processor.XMLSaxProcessor;
import org.safs.tools.logs.processor.XMLSaxToJUnitXMLHandler;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;
import com.sas.seleniumplus.preferences.PreferenceConstants;

/**
 * @author Lei Wang
 *
 */
public class SAFSXMLLogToJunitXML extends AbstractHandler{

	private static final String JUNIT_FILE_SUFFIX = ".junit.xml";
	private static final String XML_FILE_SUFFIX = "xml";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String debugmsg = StringUtils.debugmsg(false);

		Shell shell = HandlerUtil.getActiveShell(event);

		//Get the SAFS XML Log file (selected on the path).
		IFile safsXMLLogFile =  ((IFile) Activator.getSelectedResource(null));

		//Create a JUnit file to hold the parsed result
		String fileExtension = safsXMLLogFile.getFileExtension();
		if(!XML_FILE_SUFFIX.equalsIgnoreCase(fileExtension)){
			Activator.warn(debugmsg+safsXMLLogFile.getName()+"is NOT a '"+XML_FILE_SUFFIX+"' file.");
		}
		String fileName = safsXMLLogFile.getName();
		if(fileExtension!=null){
			//remove the suffix
			fileName = fileName.substring(0, fileName.length()-fileExtension.length()-1);
		}

		IContainer container = safsXMLLogFile.getParent();
		final IFile junitFile = container.getFile(new Path(fileName + JUNIT_FILE_SUFFIX));

		InputStream stream = null;
		try{
			String testCountUnit = CommonLib.getPreferenceStore().getString(PreferenceConstants.SAFS_DATA_SERVICE_TEST_COUNT_UNIT);
			Activator.log(debugmsg+"Try to convert file '"+safsXMLLogFile.getName()+"' to junit file '"+junitFile.getName()+"'. TestCountUnit="+testCountUnit);
			XMLSaxProcessor sxp = new XMLSaxProcessor(safsXMLLogFile.getContents(), new XMLSaxToJUnitXMLHandler(testCountUnit));
			Object result = sxp.parse();

			stream = new ByteArrayInputStream(result.toString().getBytes());

			if (junitFile.exists()) {
				if(!MessageDialog.openConfirm(shell, "Convert SAFS XML to JUnit log.", junitFile.getName() +" already exists, do you want to over-write?")){
					return null;
				}else{
					junitFile.delete(true, null);
				}
			}

			junitFile.create(stream, true, null);

			Activator.out.println("Successfully convert file '"+safsXMLLogFile.getName()+"' to JUnit XML file '"+junitFile.getName()+"'.");

		}catch(Exception e){
			Activator.err.println("Failed to convert file '"+safsXMLLogFile.getName()+"' to JUnit XML file '"+junitFile.getName()+"', due to "+e.getMessage());
			Activator.error(debugmsg+"Failed to convert file '"+safsXMLLogFile.getName()+"' to JUnit XML file '"+junitFile.getName()+"'", e);
		}finally{
			try {if(stream!=null) stream.close();} catch (IOException e) {}
			Activator.showConsole();
		}

		shell.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IDE.openEditor(page, junitFile, true);
				} catch (PartInitException e) {
				}
			}
		});

		return null;
	}

}

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
 * @date 2018-05-22    (LeiWang) Modified execute(): read "SAFS Data Service URL" from preference page if we cannot get it from project's INI file.
 * @date 2018-06-15    (LeiWang) Modified execute(): get "Test Count Unit" from preference to convert SAFS XML.
 */
package com.sas.seleniumplus.popupmenu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.safs.StringUtils;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant.DataServiceConstant;
import org.safs.tools.logs.processor.XMLSaxProcessor;
import org.safs.tools.logs.processor.XMLSaxToRepositoryHandler;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;
import com.sas.seleniumplus.preferences.PreferenceConstants;

/**
 * @author Lei Wang
 *
 */
public class SAFSXMLLogPushedToDataService extends AbstractHandler{

	private static final String XML_FILE_SUFFIX = "xml";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String debugmsg = StringUtils.debugmsg(false);

		//Get the SAFS XML Log file (selected on the path).
		ISelection iSelection = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		Object o = ((IStructuredSelection) iSelection).getFirstElement();
		IFile safsXMLLogFile =  ((IFile) o);
		//Verify the file's suffix is "xml".
		String fileExtension = safsXMLLogFile.getFileExtension();
		if(!XML_FILE_SUFFIX.equalsIgnoreCase(fileExtension)){
			Activator.warn(debugmsg+safsXMLLogFile.getName()+"is NOT a '"+XML_FILE_SUFFIX+"' file.");
		}

		//Get the "safs data service URL" from .ini configuration file
		String safsdataServiceURL = null;
		try {
			ConfigureInterface configInfo = Activator.getPorjectConfiguration();
			safsdataServiceURL = DataServiceConstant.getServiceURL(configInfo);

			if(safsdataServiceURL==null){
				Activator.log("Cannot deduce the safs data service URL from project .ini file.");
			}else if(DataServiceConstant.DEFAULT_SERVER_URL.equals(safsdataServiceURL)){
				Activator.log("Got the default safs data service URL '"+safsdataServiceURL+"' from project .ini file.");
			}

		} catch (NotEnabledException e) {
			Activator.err.println("Failed to push data, due to "+e.getMessage());
			Activator.error(debugmsg+" You should select a project.", e);
			return null;
		}finally{
			Activator.showConsole();
		}

		try{
			if(safsdataServiceURL==null){
				safsdataServiceURL = CommonLib.getPreferenceStore().getString(PreferenceConstants.SAFS_DATA_SERVICE_URL);
				Activator.log(debugmsg+" GOt the SAFS Data Service URL '"+safsdataServiceURL+"' from 'preference'.");
			}else if(DataServiceConstant.DEFAULT_SERVER_URL.equals(safsdataServiceURL)){
				//Even we don't specify anything in INI file, DataServiceConstant.DEFAULT_SERVER_URL will be returned
				//So we still try to see if we can get anything else from the preference page.
				String tempURL = CommonLib.getPreferenceStore().getString(PreferenceConstants.SAFS_DATA_SERVICE_URL);
				Activator.log(debugmsg+" GOt the SAFS Data Service URL '"+tempURL+"' from 'preference'.");
				if(tempURL!=null && !tempURL.trim().isEmpty()){
					safsdataServiceURL = tempURL;
				}
			}
		}catch(Exception e){
			Activator.warn(debugmsg+" Cannot deduce the safs data service URL from 'preference'。", e);
		}

		if(safsdataServiceURL==null){
			throw new ExecutionException("Cannot deduce the safs data service URL from 'preference' or '.ini config'。");
		}

		try{
			String testCountUnit = CommonLib.getPreferenceStore().getString(PreferenceConstants.SAFS_DATA_SERVICE_TEST_COUNT_UNIT);
			Activator.log(debugmsg+"Tried to push data of '"+safsXMLLogFile.getName()+"' to safs data repository at '"+safsdataServiceURL+"'. TestCountUnit="+testCountUnit);
			XMLSaxProcessor sxp = new XMLSaxProcessor(safsXMLLogFile.getContents(), new XMLSaxToRepositoryHandler(safsdataServiceURL, testCountUnit));
			Object result = sxp.parse();
			Activator.log(debugmsg+result.toString());
			Activator.out.println("Successfully pushed data of '"+safsXMLLogFile.getName()+"' to safs data repository.");

		}catch(Exception e){
			Activator.err.println("Failed to push data of '"+safsXMLLogFile.getName()+"' to safs data repository.");
			Activator.error(debugmsg+"Failed to push data of '"+safsXMLLogFile.getName()+"' to safs data repository.", e);
		}

		return null;
	}

}

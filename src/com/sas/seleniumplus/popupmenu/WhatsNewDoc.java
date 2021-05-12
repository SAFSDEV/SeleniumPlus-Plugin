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
package com.sas.seleniumplus.popupmenu;

import java.io.File;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.safs.StringUtils;
import org.safs.net.NetUtilities;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;
import com.sas.seleniumplus.preferences.PreferenceConstants;


public class WhatsNewDoc extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			String baseURL = CommonLib.getPreferenceStore().getString(PreferenceConstants.SAFS_DOC_BASE_URL).trim();
			String relativeURI = CommonLib.getPreferenceStore().getString(PreferenceConstants.SAFS_DOC_WHATSNEW).trim();
			URL url = new URL(StringUtils.concatURL(baseURL, relativeURI));

			if(!NetUtilities.isHttpURLReachable(url, 1000)){
				//If the WhatsNewInSAFS.htm document on web is not accessible, try to open the local file.
				File localFile = new File(Activator.seleniumhome + "/doc/WhatsNewInSAFS.htm");
				if(localFile.exists()){
					url = localFile.toURI().toURL();
				}
			}

			PlatformUI.getWorkbench().getBrowserSupport().createBrowser("WhatsNewDoc").openURL(url);
		} catch (Exception e) {
			Activator.log("Action failed to execute "+ e.getMessage(), e);
		}

		return null;
	}

}

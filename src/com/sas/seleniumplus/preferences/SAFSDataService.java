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
 * 2018-06-15    (LeiWang) Added ComboFieldEditor to this page.
 */
package com.sas.seleniumplus.preferences;

import java.util.LinkedHashMap;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;

public class SAFSDataService extends FieldEditorPreferencePageDefault
						  implements IWorkbenchPreferencePage {

	private static final String[] stringFieldEditorNames = {
		PreferenceConstants.SAFS_DATA_SERVICE_URL
	};
	private static final String[] comboFieldEditorNames = {
			PreferenceConstants.SAFS_DATA_SERVICE_TEST_COUNT_UNIT
	};

	public SAFSDataService(){
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CommonLib.getPreferenceStore());
	    setDescription(Activator.getResource(PreferenceConstants.PAGE_DESCRIPTION_SAFS_DATA_SERVICE));
	}

	@Override
	protected LinkedHashMap<String/*editorName*/, Class<?>/*editor's class*/> getFieldEditorsToAdd(){
		LinkedHashMap<String, Class<?>> fieldEditorsToAdd =  new LinkedHashMap<String, Class<?>>();

		//Add StringFieldEditor
		for(String fieldEditorName:stringFieldEditorNames){
			fieldEditorsToAdd.put(fieldEditorName, StringFieldEditor.class);
		}

		//Add ComboFieldEditor
		for(String fieldEditorName:comboFieldEditorNames){
			fieldEditorsToAdd.put(fieldEditorName, ComboFieldEditor.class);
		}

		return fieldEditorsToAdd;
	}
}

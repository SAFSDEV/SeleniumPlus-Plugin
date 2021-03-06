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
package com.sas.seleniumplus.preferences;

import java.util.LinkedHashMap;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;

public class RemoteServer extends FieldEditorPreferencePageDefault
						  implements IWorkbenchPreferencePage {

	private static final String[] integerFieldEditorNames = {
		PreferenceConstants.SERVER_TIMEOUT,
		PreferenceConstants.BROWSER_TIMEOUT
	};

	public RemoteServer(){
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CommonLib.getPreferenceStore());
	    setDescription(Activator.getResource(PreferenceConstants.PAGE_DESCRIPTION_REMOTE_SERVER));
	}

	@Override
	protected LinkedHashMap<String/*editorName*/, Class<?>/*editor's class*/> getFieldEditorsToAdd(){
		LinkedHashMap<String, Class<?>> fieldEditorsToAdd =  new LinkedHashMap<String, Class<?>>();

		//Add IntegerFieldEditors
		for(String integerFieldEditorName:integerFieldEditorNames){
			fieldEditorsToAdd.put(integerFieldEditorName, IntegerFieldEditor.class);
		}

		return fieldEditorsToAdd;
	}
}

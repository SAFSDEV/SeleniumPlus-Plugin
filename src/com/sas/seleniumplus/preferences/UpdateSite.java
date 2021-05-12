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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sas.seleniumplus.Activator;
import com.sas.seleniumplus.CommonLib;

public class UpdateSite
	extends FieldEditorPreferencePageDefault
	implements IWorkbenchPreferencePage {

	private static Map<String, String> boolEditorToStringEditorMaps = new HashMap<String, String>();
	private Map<String, FieldEditor> boolKeyToStringFields = new HashMap<String, FieldEditor>();

	static{
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_LIB, PreferenceConstants.UPDATESITE_LIB_URL);
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_PLUGIN, PreferenceConstants.UPDATESITE_PLUGIN_URL);
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_SOURCE_CODE, PreferenceConstants.UPDATESITE_SOURCECODE_URL);
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_JAVADOC, PreferenceConstants.UPDATESITE_JAVADOC_URL);
	}

	public UpdateSite() {
		super(GRID);
	}

	/**
	 * This is a sub-set of PreferenceConstants.intEditorNames
	 */
	private static final String[] integerFieldEditorNames = {
		PreferenceConstants.TIME_OUT
	};

	/**
	 * This is a sub-set of PreferenceConstants.booleanEditorNames
	 */
	private static final String[] booleanFieldEditorNames = {
		PreferenceConstants.BOOLEAN_VALUE_LIB,
		PreferenceConstants.BOOLEAN_VALUE_PLUGIN,
		PreferenceConstants.BOOLEAN_VALUE_SOURCE_CODE,
		PreferenceConstants.BOOLEAN_VALUE_JAVADOC,
		PreferenceConstants.BOOLEAN_VALUE_UPDATE_JRE
	};

	@Override
	protected LinkedHashMap<String/*editorName*/, Class<?>/*editor's class*/> getFieldEditorsToAdd(){
		LinkedHashMap<String, Class<?>> fieldEditorsToAdd =  new LinkedHashMap<String, Class<?>>();
		String boolEditorName = null;

		//Add pairs of (BooleanFieldEditor, StringFieldEditor)
		for(int i=0;i<booleanFieldEditorNames.length;i++){
			boolEditorName = booleanFieldEditorNames[i];
			fieldEditorsToAdd.put(boolEditorName, BooleanFieldEditor.class);
			if(boolEditorToStringEditorMaps.containsKey(boolEditorName)){
				fieldEditorsToAdd.put(boolEditorToStringEditorMaps.get(boolEditorName), StringFieldEditor.class);
			}
		}

		//Add IntegerFieldEditors
		for(String integerFieldEditorName:integerFieldEditorNames){
			fieldEditorsToAdd.put(integerFieldEditorName, IntegerFieldEditor.class);
		}

		return fieldEditorsToAdd;
	}

	@Override
	protected void createFieldEditors() {
		super.createFieldEditors();

		//Create the mapping boolKeyToStringFields
		String boolEditorName = null;
		String stringEditorName = null;
		for(FieldEditor booleanEditor: booleanFieldEditors){
			boolEditorName = booleanEditor.getPreferenceName();
			stringEditorName = boolEditorToStringEditorMaps.get(boolEditorName);
			for(FieldEditor stringEditor: stringFieldEditors){
				if(stringEditor.getPreferenceName().equals(stringEditorName)){
					boolKeyToStringFields.put(boolEditorName, stringEditor);
				}
			}
		}

		enableStringFields(true, false);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		//According to the BooleanFieldEditor's value to enable/disable the StringFieldEditors
		enableStringFields(false, false);
	}

	@Override
	public void init(IWorkbench arg0) {
		setPreferenceStore(CommonLib.getPreferenceStore());
		setDescription(Activator.getResource(PreferenceConstants.PAGE_DESCRIPTION_REMOTE_SERVER));
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		//According to the default preference value to enable/disable the StringFieldEditors
		enableStringFields(true, true);
	}

	/**
	 * @param fromPreference boolean, if true then use the preference values; else use the BooleanFieldEditor's value.
	 * @param useDefault boolean (only valid if fromPreference is true), use the default value or user set value.
	 */
	protected void enableStringFields(boolean fromPreference, boolean useDefault){
		String boolEditorName = null;
		FieldEditor fieldEditor = null;
		if(fromPreference){
			boolean enabled = false;
			IPreferenceStore store = CommonLib.getPreferenceStore();

			for(FieldEditor booleanEditor: booleanFieldEditors){
				try{
					boolEditorName = booleanEditor.getPreferenceName();
					enabled = useDefault? store.getDefaultBoolean(boolEditorName): store.getBoolean(boolEditorName);
					fieldEditor = boolKeyToStringFields.get(boolEditorName);
					if(fieldEditor!=null) fieldEditor.setEnabled(enabled, getFieldEditorParent());
				}catch(Exception e){
					Activator.warn("Failed to enable StringFieldEditors, due to "+e.toString());
				}
			}
		}else{//From the boolean fields
			for(FieldEditor booleanEditor: booleanFieldEditors){
				try{
					boolEditorName = booleanEditor.getPreferenceName();
					fieldEditor = boolKeyToStringFields.get(boolEditorName);
					if(fieldEditor!=null) fieldEditor.setEnabled(((BooleanFieldEditor)booleanEditor).getBooleanValue(), getFieldEditorParent());
				}catch(Exception e){
					Activator.warn("Failed to enable StringFieldEditors, due to "+e.toString());
				}
			}
		}
	}

}

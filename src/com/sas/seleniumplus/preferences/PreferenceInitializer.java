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
 * 2018-06-15    (LeiWang) Set default value for ComboFieldEditor.
 */
package com.sas.seleniumplus.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sas.seleniumplus.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		loadDefaultFromResourceBundle();
	}

	/**
	 * Load default values from 'resource bundle'. It <b>MUST</b> be called after the creation of 'resource bundle', see {@link Activator#initResourceBundle()}.
	 */
	public static void loadDefaultFromResourceBundle(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String key = null;

		for(String editorName: PreferenceConstants.stringEditorNames){
			try{
				key = editorName+PreferenceConstants.SUFFIX_DEFAULT;
				store.setDefault(editorName, Activator.getResource(key));
			}catch(Exception ne){
				Activator.warn("PreferenceInitializer Failed to load default string preference for '"+key+"', due to "+ne.toString());
			}
		}

		for(String editorName: PreferenceConstants.booleanEditorNames){
			try{
				key = editorName+PreferenceConstants.SUFFIX_DEFAULT;
				store.setDefault(editorName, Boolean.parseBoolean(Activator.getResource(key)));
			}catch(Exception ne){
				Activator.warn("PreferenceInitializer Failed to load default boolean preference for '"+key+"', due to "+ne.toString());
			}
		}

		for(String editorName: PreferenceConstants.intEditorNames){
			try{
				key = editorName+PreferenceConstants.SUFFIX_DEFAULT;
				store.setDefault(editorName, Integer.parseInt(Activator.getResource(key)));
			}catch(Exception ne){
				Activator.warn("PreferenceInitializer Failed to load default int preference for '"+key+"', due to "+ne.toString());
			}
		}

		for(String editorName: PreferenceConstants.comboEditorNames){
			try{
				key = editorName+PreferenceConstants.SUFFIX_DEFAULT;
				//Set default value for combobox
				String value = Activator.getResource(key);
				Activator.warn("PreferenceInitializer set '"+value+"' to key '"+editorName+"'.");
				store.setDefault(editorName, value);
			}catch(Exception ne){
				Activator.warn("PreferenceInitializer Failed to load default int preference for '"+key+"', due to "+ne.toString());
			}
		}
	}

}

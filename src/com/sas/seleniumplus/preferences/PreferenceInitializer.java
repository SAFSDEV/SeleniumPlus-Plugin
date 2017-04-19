package com.sas.seleniumplus.preferences;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sas.seleniumplus.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String key = null;

		for(String editorName: PreferenceConstants.stringEditorNames){
			try{
				key = editorName+PreferenceConstants.SUFFIX_DEFAULT;
				store.setDefault(editorName, Activator.getPreference(key));
			}catch(Exception ne){
				Activator.warn("PreferenceInitializer Failed to load default string preference for '"+key+"', due to "+ne.toString());
			}
		}

		for(String editorName: PreferenceConstants.booleanEditorNames){
			try{
				key = editorName+PreferenceConstants.SUFFIX_DEFAULT;
				store.setDefault(editorName, Boolean.parseBoolean(Activator.getPreference(key)));
			}catch(Exception ne){
				Activator.warn("PreferenceInitializer Failed to load default boolean preference for '"+key+"', due to "+ne.toString());
			}
		}

		for(String editorName: PreferenceConstants.intEditorNames){
			try{
				key = editorName+PreferenceConstants.SUFFIX_DEFAULT;
				store.setDefault(editorName, Integer.parseInt(Activator.getPreference(key)));
			}catch(Exception ne){
				Activator.warn("PreferenceInitializer Failed to load default int preference for '"+key+"', due to "+ne.toString());
			}
		}
	}

}

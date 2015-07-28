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
		store.setDefault(PreferenceConstants.UPDATESITE_LIB_URL, PreferenceConstants.DOWNLOAD_URl_LIB);
		store.setDefault(PreferenceConstants.UPDATESITE_PLUGIN_URL, PreferenceConstants.DOWNLOAD_URL_PLUGIN);
		store.setDefault(PreferenceConstants.BOOLEAN_VALUE_LIB,true);
		store.setDefault(PreferenceConstants.BOOLEAN_VALUE_PLUGIN,false);
		store.setDefault(PreferenceConstants.TIME_OUT,PreferenceConstants.TIME_OUT_VALUE);
		store.setDefault(PreferenceConstants.SERVER_TIMEOUT,PreferenceConstants.SERVER_TIMEOUT_VALUE);
		store.setDefault(PreferenceConstants.BROWSER_TIMEOUT,PreferenceConstants.BROWSER_TIMEOUT_VALUE);
	}

}

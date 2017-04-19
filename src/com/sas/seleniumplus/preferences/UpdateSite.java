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

public class UpdateSite
	extends FieldEditorPreferencePageDefault
	implements IWorkbenchPreferencePage {

	private static Map<String, String> boolEditorToStringEditorMaps = new HashMap<String, String>();
	private Map<String, FieldEditor> boolKeyToStringFields = new HashMap<String, FieldEditor>();

	static{
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_LIB, PreferenceConstants.UPDATESITE_LIB_URL);
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_PLUGIN, PreferenceConstants.UPDATESITE_PLUGIN_URL);
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_SOURCE_CORE, PreferenceConstants.UPDATESITE_SOURCECORE_URL);
		boolEditorToStringEditorMaps.put(PreferenceConstants.BOOLEAN_VALUE_JAVADOC, PreferenceConstants.UPDATESITE_JAVADOC_URL);
	}

	public UpdateSite() {
		super(GRID);
	}

	private static final String[] integerFieldEditorNames = {
		PreferenceConstants.TIME_OUT
	};

//	private static final String[] stringFieldEditorNames = {
//		PreferenceConstants.UPDATESITE_LIB_URL,
//		PreferenceConstants.UPDATESITE_PLUGIN_URL,
//		PreferenceConstants.UPDATESITE_SOURCECORE_URL,
//		PreferenceConstants.UPDATESITE_JAVADOC_URL
//	};

	private static final String[] booleanFieldEditorNames = {
		PreferenceConstants.BOOLEAN_VALUE_LIB,
		PreferenceConstants.BOOLEAN_VALUE_PLUGIN,
		PreferenceConstants.BOOLEAN_VALUE_SOURCE_CORE,
		PreferenceConstants.BOOLEAN_VALUE_JAVADOC
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
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Activator.getPreference("update.site.preference.page.desc"));
	}

	@Override
	protected void performDefaults() {
		//According to the default preference value to enable/disable the StringFieldEditors
		enableStringFields(true, true);
		super.performDefaults();
	}

	/**
	 * @param fromPreference boolean, if true then use the preference values; else use the BooleanFieldEditor's value.
	 * @param useDefault boolean (only valid if fromPreference is true), use the default value or user set value.
	 */
	protected void enableStringFields(boolean fromPreference, boolean useDefault){
		String boolEditorName = null;
		if(fromPreference){
			boolean enabled = false;
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();

			for(FieldEditor booleanEditor: booleanFieldEditors){
				try{
					boolEditorName = booleanEditor.getPreferenceName();
					enabled = useDefault? store.getDefaultBoolean(boolEditorName): store.getBoolean(boolEditorName);
					boolKeyToStringFields.get(boolEditorName).setEnabled(enabled, getFieldEditorParent());
				}catch(Exception e){
					Activator.warn("Failed to enable StringFieldEditors, due to "+e.toString());
				}
			}
		}else{//From the boolean fields
			for(FieldEditor booleanEditor: booleanFieldEditors){
				try{
					boolEditorName = booleanEditor.getPreferenceName();
					boolKeyToStringFields.get(boolEditorName).setEnabled(((BooleanFieldEditor)booleanEditor).getBooleanValue(), getFieldEditorParent());
				}catch(Exception e){
					Activator.warn("Failed to enable StringFieldEditors, due to "+e.toString());
				}
			}
		}
	}

}

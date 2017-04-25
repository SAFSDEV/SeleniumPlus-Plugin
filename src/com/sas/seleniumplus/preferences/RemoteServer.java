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

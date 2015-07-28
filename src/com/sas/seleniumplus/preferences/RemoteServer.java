package com.sas.seleniumplus.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import com.sas.seleniumplus.Activator;

public class RemoteServer extends FieldEditorPreferencePage 
						implements IWorkbenchPreferencePage {
	
	StringFieldEditor str_editor_servertimeout;
	StringFieldEditor str_editor_browsertimeout;
		
	
	public RemoteServer(){
		super(GRID);		
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	    setDescription("Selenium Remote Server configuration:"); 	
		
	}

	@Override
	protected void createFieldEditors() {
			    
		
		str_editor_servertimeout = new StringFieldEditor(PreferenceConstants.SERVER_TIMEOUT,
		        "&Server timeout (seconds):",10, getFieldEditorParent());
	
		addField(str_editor_servertimeout);
				
		str_editor_browsertimeout = new StringFieldEditor(PreferenceConstants.BROWSER_TIMEOUT, 
				"Browser timeout (seconds):",10,getFieldEditorParent());
		
		addField(str_editor_browsertimeout);	
		
	}
	
	protected void performDefaults() {
		str_editor_servertimeout.loadDefault();
		str_editor_browsertimeout.loadDefault();
		super.performDefaults();		
		
	} 
}

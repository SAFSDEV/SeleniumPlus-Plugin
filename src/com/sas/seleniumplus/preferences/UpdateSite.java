package com.sas.seleniumplus.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sas.seleniumplus.Activator;

public class UpdateSite 
	extends FieldEditorPreferencePage 
	implements IWorkbenchPreferencePage {
	
	StringFieldEditor str_editor_lib;
	StringFieldEditor str_editor_plugin;
	BooleanFieldEditor bol_editor_lib;
	BooleanFieldEditor bol_editor_plugin;
	StringFieldEditor str_editor_timeout;
	
	public UpdateSite() {
		super(GRID);		
	}
	
	@Override
	protected void createFieldEditors() {
			    
		
		bol_editor_lib = new BooleanFieldEditor(PreferenceConstants.BOOLEAN_VALUE_LIB,
		        "&Enable or Disable Lib Update", getFieldEditorParent());
	
		addField(bol_editor_lib);
				
		str_editor_lib = new StringFieldEditor(PreferenceConstants.UPDATESITE_LIB_URL, 
				"Update Lib URL:",getFieldEditorParent());
		
		addField(str_editor_lib);
		
				
		bol_editor_plugin = new BooleanFieldEditor(PreferenceConstants.BOOLEAN_VALUE_PLUGIN,
		        "&Enable or Disable PlugIn Update", getFieldEditorParent());
		
		addField(bol_editor_plugin);
		str_editor_plugin = new StringFieldEditor(PreferenceConstants.UPDATESITE_PLUGIN_URL, 
				"Update Plugin URL:",getFieldEditorParent());
		
		addField(str_editor_plugin);
		
		str_editor_timeout = new StringFieldEditor(PreferenceConstants.TIME_OUT, 
				"Time Out (Minutes):",10,getFieldEditorParent());
		
		addField(str_editor_timeout);
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		boolean lib = store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_LIB);
		boolean plugin = store.getBoolean(PreferenceConstants.BOOLEAN_VALUE_PLUGIN);
		str_editor_plugin.setEnabled(plugin, getFieldEditorParent());
		str_editor_lib.setEnabled(lib, getFieldEditorParent());
	}
	
		
	@Override
	public void propertyChange(PropertyChangeEvent event) {
	        // TODO Auto-generated method stub
	        if (event.getSource() == bol_editor_lib) {
	            String consentResponse = event.getNewValue().toString();
	            if (consentResponse == "true") {
	            	str_editor_lib.setEnabled(true, getFieldEditorParent());	            	
	            } else {
	            	str_editor_lib.setEnabled(false, getFieldEditorParent());	            	
	            }
	        }
	        if (event.getSource() == bol_editor_plugin) {
	            String consentResponse = event.getNewValue().toString();
	            if (consentResponse == "true") {
	            	str_editor_plugin.setEnabled(true, getFieldEditorParent());	            	
	            } else {
	            	str_editor_plugin.setEnabled(false, getFieldEditorParent());	            	
	            }
	        }
	        super.propertyChange(event);
	    }		
	

	@Override
	public void init(IWorkbench arg0) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	    setDescription("Enter URL to update SeleniumPlus assets");	    
	}
	
	protected void performDefaults() {
		bol_editor_lib.loadDefault();
		bol_editor_plugin.loadDefault();
		str_editor_lib.loadDefault(); 
		str_editor_timeout.loadDefault(); 
		str_editor_plugin.loadDefault();
		str_editor_plugin.setEnabled(false, getFieldEditorParent());
		str_editor_lib.setEnabled(true, getFieldEditorParent());
		super.performDefaults();		
		
	} 

}

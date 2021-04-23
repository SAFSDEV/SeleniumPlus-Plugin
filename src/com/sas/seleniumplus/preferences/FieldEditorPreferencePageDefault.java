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
 * 2017-04-19    (LeiWang) Initial release.
 * 2017-04-20    (LeiWang) Dynamically load properties from resource bundle at run-time.
 * 2017-07-28    (LeiWang) Modified createFieldEditors(): If the field's label cannot be found, this field will still be created.
 * 2018-06-15    (LeiWang) Added comboFieldEditors
 */
package com.sas.seleniumplus.preferences;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import com.sas.seleniumplus.Activator;

/**
 * @author LeiWang
 *
 */
public class FieldEditorPreferencePageDefault extends FieldEditorPreferencePage{

	/** a list of IntegerFieldEditors contained in this Page */
	protected List<FieldEditor> intFieldEditors = null;
	/** a list of BooleanFieldEditors contained in this Page */
	protected List<FieldEditor> booleanFieldEditors = null;
	/** a list of StringFieldEditors contained in this Page */
	protected List<FieldEditor> stringFieldEditors = null;
	/** a list of ComboFieldEditors contained in this Page */
	protected List<FieldEditor> comboFieldEditors = null;

	/**
	 * A map holding pairs of (Class, List&lt;FieldEditor&gt;). It simplifies the code of method {@link #addField(FieldEditor)}.
	 * @see #addField(FieldEditor)
	 */
	protected Map<Class<?>, List<FieldEditor>> clazzToFieldEditorList = null;

	public FieldEditorPreferencePageDefault(){
		super();
		init();
	}
	/**
	 * @param style
	 */
	public FieldEditorPreferencePageDefault(int style) {
		super(style);
		init();
	}

	private final void init(){
		intFieldEditors = new ArrayList<FieldEditor>();
		booleanFieldEditors = new ArrayList<FieldEditor>();
		stringFieldEditors = new ArrayList<FieldEditor>();
		comboFieldEditors = new ArrayList<FieldEditor>();

		clazzToFieldEditorList = new HashMap<Class<?>, List<FieldEditor>>();
		clazzToFieldEditorList.put(IntegerFieldEditor.class, intFieldEditors);
		clazzToFieldEditorList.put(BooleanFieldEditor.class, booleanFieldEditors);
		clazzToFieldEditorList.put(StringFieldEditor.class, stringFieldEditors);
		clazzToFieldEditorList.put(ComboFieldEditor.class, comboFieldEditors);
	}

	/**
	 * This is the method to be overridden when adding fields for a certain FieldEditorPreferencePage.
	 * @return Map of editors to add, they <b>MUST</b> be put <b>IN ORDER</b>.
	 * @see #createFieldEditors()
	 */
	protected LinkedHashMap<String/*editorName*/, Class<?>/*FieldEditor's class*/> getFieldEditorsToAdd(){
		return new LinkedHashMap<String, Class<?>>();
	}

	public List<FieldEditor> getFieldEditors(){
		List<FieldEditor> fields = new ArrayList<FieldEditor>();

		fields.addAll(intFieldEditors);
		fields.addAll(booleanFieldEditors);
		fields.addAll(stringFieldEditors);
		fields.addAll(comboFieldEditors);

		return fields;
	}

	@Override
	protected void createFieldEditors() {
		Map<String/*editorName*/, Class<?>/*editor's class*/> fieldEditorsToAdd = getFieldEditorsToAdd();
		//key in the resource bundle, pointing to the label for this editor.
		String key = null;
		Class<?> editorClass = null;
		String label = null;
		String[][] options = {{}};

		for(String name: fieldEditorsToAdd.keySet()){
			key = name+PreferenceConstants.SUFFIX_LABEL;
			editorClass = fieldEditorsToAdd.get(name);

			try{
				label = Activator.getResource(key);
			}catch(MissingResourceException e){
				Activator.warn(getClass().getName()+": Failed to get value for key '"+key+"' from configuration file, use '"+key+"' instead.");
				label = key;
			}

			//Get options for ComboFieldEditor
			if(editorClass.equals(ComboFieldEditor.class)){
				key = name+PreferenceConstants.SUFFIX_OPTIONS;
				try{
					//combo options are stored in the preferences.properties file in format of JSON, such as
					//[["Test Case", "TESTCASE"], ["Test Step", "TESTSTEP"]]
					String stringOptions = Activator.getResource(key);
					options = org.safs.Utils.fromJsonString(stringOptions, String[][].class);
				}catch(MissingResourceException e){
					Activator.warn(getClass().getName()+": Failed to get value for key '"+key+"' from configuration file..");
				}
			}

			try {
				if(editorClass.equals(ComboFieldEditor.class)){
					//For ComboFieldEditor
					Constructor<?> c =  editorClass.getConstructor(String.class/*name*/, String.class/*label*/, String[][].class /*entryNamesAndValues*/,Composite.class/*editor's parent*/);
					addField( (FieldEditor)c.newInstance(name, label, options, getFieldEditorParent()) );
				}else{
					//For IntegerFieldEditor, BooleanFieldEditor and StringFieldEditor
					Constructor<?> c =  editorClass.getConstructor(String.class/*name*/, String.class/*label*/, Composite.class/*editor's parent*/);
					addField( (FieldEditor)c.newInstance(name, label, getFieldEditorParent()) );
				}

			} catch (Exception e) {
				Activator.warn(getClass().getName()+": Failed to add editor '"+name+"' of type '"+editorClass.getSimpleName()+"', due to "+e.getClass().getSimpleName()+":"+e.getMessage());
			}
		}

	}

	@Override
	protected void addField(FieldEditor editor) {
		//Add FieldEditor to map cache according to their class
		List<FieldEditor> editorList = null;
		for(Class<?> clazz:clazzToFieldEditorList.keySet()){
			if(clazz.isInstance(editor)){
				editorList = clazzToFieldEditorList.get(clazz);
				editorList.add(editor);
				break;
			}
		}
		super.addField(editor);
	}

	@Override
	protected void performDefaults() {
		//Load the default from external resource bundle,
		//if we can get the 'resource bundle' reload by itself, then we don't need to call Activator.initResourceBundle() here.
		Activator.initResourceBundle();
		PreferenceInitializer.loadDefaultFromResourceBundle();
		super.performDefaults();
	}
}

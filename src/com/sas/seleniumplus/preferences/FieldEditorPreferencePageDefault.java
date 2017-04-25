/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年4月19日    (Lei Wang) Initial release.
 * 2017年4月20日    (Lei Wang) Dynamically load properties from resource bundle at run-time.
 */
package com.sas.seleniumplus.preferences;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import com.sas.seleniumplus.Activator;

/**
 * @author Lei Wang
 *
 */
public class FieldEditorPreferencePageDefault extends FieldEditorPreferencePage{

	/** a list of IntegerFieldEditors contained in this Page */
	protected List<FieldEditor> intFieldEditors = null;
	/** a list of BooleanFieldEditors contained in this Page */
	protected List<FieldEditor> booleanFieldEditors = null;
	/** a list of StringFieldEditors contained in this Page */
	protected List<FieldEditor> stringFieldEditors = null;

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

		clazzToFieldEditorList = new HashMap<Class<?>, List<FieldEditor>>();
		clazzToFieldEditorList.put(IntegerFieldEditor.class, intFieldEditors);
		clazzToFieldEditorList.put(BooleanFieldEditor.class, booleanFieldEditors);
		clazzToFieldEditorList.put(StringFieldEditor.class, stringFieldEditors);
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

		return fields;
	}

	@Override
	protected void createFieldEditors() {
		Map<String/*editorName*/, Class<?>/*editor's class*/> fieldEditorsToAdd = getFieldEditorsToAdd();
		//key in the resource bundle, pointing to the label for this editor.
		String key = null;
		Class<?> editorClass = null;

		for(String name: fieldEditorsToAdd.keySet()){
			key = name+PreferenceConstants.SUFFIX_LABEL;
			editorClass = fieldEditorsToAdd.get(name);

			try {
				Constructor<?> c =  (Constructor<?>) editorClass.getConstructor(String.class/*name*/, String.class/*label*/, Composite.class/*editor's parent*/);
				addField( (FieldEditor)c.newInstance(name, Activator.getResource(key), getFieldEditorParent()) );
			} catch (Exception e) {
				Activator.warn(getClass().getName()+": Failed to add editor '"+name+"' of type '"+editorClass.getSimpleName()+"'");
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

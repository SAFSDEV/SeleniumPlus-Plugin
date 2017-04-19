/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年4月19日    (Lei Wang) Initial release.
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

	/** a list of IntegerFieldEditor*/
	protected List<FieldEditor> intFieldEditors = null;
	/** a list of BooleanFieldEditor*/
	protected List<FieldEditor> booleanFieldEditors = null;
	/** a list of StringFieldEditor*/
	protected List<FieldEditor> stringFieldEditors = null;

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
	 *
	 * @return Map of editors to add, they <b>MUST</b> be put <b>IN ORDER</b>.
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
		String key = null;
		Class<?> editorClass = null;

		for(String name: fieldEditorsToAdd.keySet()){
			key = name+PreferenceConstants.SUFFIX_LABEL;
			editorClass = fieldEditorsToAdd.get(name);

			try {
				Constructor<?> c =  (Constructor<?>) editorClass.getConstructor(String.class, String.class, Composite.class);
				addField( (FieldEditor)c.newInstance(name, Activator.getPreference(key), getFieldEditorParent()) );
			} catch (Exception e) {
				Activator.warn(getClass().getName()+": Failed to add editor '"+name+"' of type '"+editorClass.getSimpleName()+"'");
			}
		}

	}

	protected void addField(FieldEditor editor) {
		for(Class<?> clazz:clazzToFieldEditorList.keySet()){
			if(clazz.isInstance(editor)){
				clazzToFieldEditorList.get(clazz).add(editor);
			}
		}
		super.addField(editor);
	}
}

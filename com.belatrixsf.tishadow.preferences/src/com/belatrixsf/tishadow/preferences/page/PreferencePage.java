package com.belatrixsf.tishadow.preferences.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);

	}

	public void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceValues.TISHADOW_DIRECTORY,
				"&Tishadow Path:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceValues.TISHADOW_HOST, "Tishadow &Host:",
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceValues.TISHADOW_PORT, "Tishadow &Port:",
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Tishadow");
		String directory = PreferenceValues.getTishadowDirectory();
		new Helper().ValidateTishadowPath(directory);
	}
}

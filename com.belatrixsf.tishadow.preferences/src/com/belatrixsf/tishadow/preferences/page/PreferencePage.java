package com.belatrixsf.tishadow.preferences.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	private Helper helper;
	
	/**Constructor */
	public PreferencePage() {
		super(GRID);
		helper = new Helper();
	}

	/**Create fields to preference page */
	public void createFieldEditors() {
		addTishadowDirectoryField();		
		addField(new StringFieldEditor(PreferenceValues.TISHADOW_HOST, "Tishadow &Host:",
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceValues.TISHADOW_PORT, "Tishadow &Port:",
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	/**Add tishadow directory field to preference page. 
	 * Validation of tishadow directory.
	 * Set of information message or error message.
	 * */
	private void addTishadowDirectoryField() {
		addField(new FileFieldEditor(PreferenceValues.TISHADOW_DIRECTORY,
				"&Tishadow Path:", getFieldEditorParent()) {
			@Override
			protected boolean doCheckState() {
				String actualValue = this.getStringValue();
				if (helper.tiShadowPathIsValid(actualValue)) {
					setMessage("TiShadow Version: " + helper.getTiShadowVersion().trim(), 1);
					return true;
				} else {
					setErrorMessage("Could not find TiShadow Directory");
					return false;
				}
			}
		});
	}
}

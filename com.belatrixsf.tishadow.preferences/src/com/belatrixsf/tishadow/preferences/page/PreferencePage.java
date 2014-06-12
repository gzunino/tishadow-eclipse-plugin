package com.belatrixsf.tishadow.preferences.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	//private String tishadowVersion;
	
	public PreferencePage() {
		super(GRID);

	}

	public void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceValues.TISHADOW_DIRECTORY,
				"&Tishadow Path:", getFieldEditorParent()) {
			/*
			@Override
			protected boolean doCheckState() {
				String directory = PreferenceValues.getTishadowDirectory();
				try{
					Helper helper = new Helper().ValidateTishadowPath(directory);
					if (helper.getTishadowVersion() != null){
						tishadowVersion = helper.getTishadowVersion();
						return true;
					} 
					else
						setErrorMessage("Could not find TiShadow."); 
						return false;
				}
				catch(Exception ex){
					setErrorMessage("Could not find TiShadow.");
					return false;
				}
			}*/
		});
		
		addField(new StringFieldEditor(PreferenceValues.TISHADOW_HOST, "Tishadow &Host:",
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceValues.TISHADOW_PORT, "Tishadow &Port:",
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		//setDescription("Tishadow");
	}
}

package com.belatrixsf.tishadow.preferences.page;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceValues.TISHADOW_DIRECTORY, PreferenceValues.TISHADOW_DEFAULT_DIRECTORY);
		store.setDefault(PreferenceValues.TISHADOW_HOST, PreferenceValues.TISHADOW_DEFAULT_HOST);
		store.setDefault(PreferenceValues.TISHADOW_PORT, PreferenceValues.TISHADOW_DEFAULT_PORT);
	}

}

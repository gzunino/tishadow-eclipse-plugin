package com.belatrixsf.tishadow.preferences.page;

public class PreferenceValues {

	public static final String TISHADOW_HOST = "tishadowHost";
	public static final String TISHADOW_PORT = "tishadowPort";
	public static final String TISHADOW_DIRECTORY = "tishadowDirectory";
	public static final int TISHADOW_DEFAULT_PORT = 3000;
	public static final String TISHADOW_DEFAULT_DIRECTORY = "/usr/local/bin/tishadow";
	public static final String TISHADOW_DEFAULT_HOST = "localhost";

	public static String getTishadowHost() {
		return Activator.getDefault().getPreferenceStore()
				.getString(TISHADOW_HOST);
	}
	
	public static int getTishadowPort() {
		return Activator.getDefault().getPreferenceStore()
				.getInt(TISHADOW_PORT);
	}

	public static String getTishadowDirectory() {
		return Activator.getDefault().getPreferenceStore()
				.getString(TISHADOW_DIRECTORY);
	}
}

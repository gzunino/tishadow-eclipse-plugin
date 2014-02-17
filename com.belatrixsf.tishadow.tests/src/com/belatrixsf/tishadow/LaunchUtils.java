package com.belatrixsf.tishadow;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;

public class LaunchUtils {
	public static Map<String, String> getEnvVars() {
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}
	
	public static void handleError(String msg, Exception e) {
		MessageDialog.openError(null, msg, e.toString() + "\n" + e.getLocalizedMessage().toString());
		e.printStackTrace();
	}
}

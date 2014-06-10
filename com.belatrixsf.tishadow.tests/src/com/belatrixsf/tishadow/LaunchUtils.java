package com.belatrixsf.tishadow;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;

import com.belatrixsf.tishadow.preferences.page.PreferenceValues;

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

	public static IProject getProject(final String projectLoc) {
		try {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

			for (IProject project : projects) {
				if(projectLoc.equals(project.getLocation().toString())) {
					return project;
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean serverLaunched() {
		try {
	        Socket sock = new Socket(PreferenceValues.getTishadowHost(), PreferenceValues.getTishadowPort());
	        sock.close();
	        return false;
	    } catch (Exception e) {         
	        return true;
	    }
	}
}

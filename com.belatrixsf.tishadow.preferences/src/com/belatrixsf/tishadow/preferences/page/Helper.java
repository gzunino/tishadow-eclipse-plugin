package com.belatrixsf.tishadow.preferences.page;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

public class Helper implements IRunnerCallback{
	
	
	public boolean ValidateTishadowPath(String path){
		return exists(path) && canExecute(path) && getTishadowVersion(path) != null; 
	}
	
	private boolean canExecute(String path) {
		return new File(path).canExecute();
	}

	private Object getTishadowVersion(String path) {
		TiShadowRunner tishadowRunner = new TiShadowRunner("Tishadow Version");
		tishadowRunner.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, " -v")
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
				.setAttribute(Constants.TISHADOW_LOCATION, PreferenceValues.TISHADOW_DEFAULT_DIRECTORY)
				.setAttribute(Constants.TISHADOW_WORKING_DIRECTORY, "/home/")
				.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES, getEnvVars());
		
		tishadowRunner.runTiShadow(this);
		return null;
	}

	private boolean exists(String path) {
		return new File(path).exists();
	}
	
	public static Map<String, String> getEnvVars() {
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}
	
	@Override
	public void onRunnerTishadowFinish(Object response) {
		System.out.println(response);
	}
}

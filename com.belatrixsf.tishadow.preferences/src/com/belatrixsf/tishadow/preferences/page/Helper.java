package com.belatrixsf.tishadow.preferences.page;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

public class Helper implements IRunnerCallback{
	private String tishadowVersion;

	public void ValidateTishadowPath(String path) throws Exception{
		if(exists(path) && canExecute(path)){
			runTishadowVersion(path);
		}; 
	}
	
	@Override
	public void onRunnerTishadowFinish(Object response) {
		System.out.println(response);
		tishadowVersion = response.toString();
	}
	
	public String getTishadowVersion() {
		return tishadowVersion;
	}
	
	private boolean canExecute(String path) {
		return new File(path).canExecute();
	}

	private void runTishadowVersion(String path) throws Exception {
		System.out.println(path);
		TiShadowRunner tishadowRunner = new TiShadowRunner("Tishadow Version");
		tishadowRunner
				.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, " -v")
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
				.setAttribute(Constants.TISHADOW_LOCATION,
						path)
				.setAttribute(Constants.TISHADOW_WORKING_DIRECTORY,
						"/home/")
				.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES,
						getEnvVars());
		tishadowRunner.runTiShadow(this);
	}

	private boolean exists(String path) {
		return new File(path).exists();
	}
	
	private static Map<String, String> getEnvVars() {
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}
}

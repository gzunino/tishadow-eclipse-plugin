package com.belatrixsf.tishadow.preferences.page;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

public class Helper implements IRunnerCallback{
	private String tiShadowVersion;
	private boolean isExecutionFinished;
	private Object runResponse;
	private boolean tiShadowExists;
	
	/**Constructor */
	public Helper(){
		tiShadowVersion = "";
		isExecutionFinished = false;
		runResponse = null;
		tiShadowExists = false;
	}
	
	/*Validation of tishadow path.*/
	public boolean tiShadowPathIsValid(String path){
		if(exists(path) && canExecute(path)){
			try {
				runTishadowHelp(path);
				if(tiShadowExists){
					runTiShadowVersion(path);
					if(tiShadowVersion != ""){
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	/* Callback method used by TishadowRunner class */
	@Override
	public void onRunnerTishadowFinish(Object response) {
		isExecutionFinished = true;
		runResponse = response;
	}

	public String getTiShadowVersion() {
		return tiShadowVersion;
	}
	
	private void runTiShadowVersion(String path) throws Exception {
		isExecutionFinished = false;
		runTiShadowCommand("TiShadow Help", "--version", path);
		sleepUntilExecutionFinished();
		tiShadowVersion = runResponse.toString();
	}
	
	private void runTishadowHelp(String path) throws Exception,
			InterruptedException {
		isExecutionFinished = false;
		runTiShadowCommand("TiShadow Help", "--help", path);
		sleepUntilExecutionFinished();
		tiShadowExists = (runResponse != null && runResponse.toString().contains("appify"));
	}

	private void sleepUntilExecutionFinished() throws InterruptedException {
		while (!isExecutionFinished) {
			Thread.sleep(750);
			if(!isExecutionFinished) throw new InterruptedException("onRunnerTishadowFinish was not called");
		}
	}
	
	private boolean canExecute(String path) {
		return new File(path).canExecute();
	}

	private void runTiShadowCommand(String configurationName, String command, String path) throws Exception {
		TiShadowRunner tishadowRunner = new TiShadowRunner(configurationName);
		tishadowRunner
				.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, " " + command)
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, false)
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
	
	public static Map<String, String> getEnvVars() {
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}
}

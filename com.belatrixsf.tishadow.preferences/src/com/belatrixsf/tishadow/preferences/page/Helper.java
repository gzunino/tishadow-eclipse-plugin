package com.belatrixsf.tishadow.preferences.page;
import java.io.File;

import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

public class Helper implements IRunnerCallback{
	
	IRunnerCallback callback;
	
	public boolean ValidateTishadowPath(String path){
		return exists(path) && canExecute(path) && getTishadowVersion(path) != null; 
	}
	
	private boolean canExecute(String path) {
		return new File(path).canExecute();
	}

	private Object getTishadowVersion(String path) {
		TiShadowRunner tishadowRunner = new TiShadowRunner("Tishadow Help");
		tishadowRunner.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, "help")
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, false)
				.setAttribute(Constants.TISHADOW_LOCATION, "");
		/**
		 * workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION,
		 * location); workingCopy.setAttribute(IExternalToolConstants.
		 * ATTR_WORKING_DIRECTORY, projectLoc);
		 * workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
		 * envVars);
		 * */
		tishadowRunner.runTiShadow(callback);
		return null;
	}

	private boolean exists(String path) {
		return new File(path).exists();
	}

	@Override
	public void onRunnerTishadowFinish() {
		System.out.println("COMMAND EXECUTED! ");
		
	}
	
	
}

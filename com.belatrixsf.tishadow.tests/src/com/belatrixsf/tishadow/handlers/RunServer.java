package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

public class RunServer extends AbstractHandler implements IRunnerCallback {
	
	boolean isServerLaunched = false;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		isServerLaunched = LaunchUtils.isServerLaunched();
		if (isServerLaunched) {
			MessageDialog.openError(null, "Error",
					"An instance of the server is already running.");
		} else {
			try {
				TiShadowRunner tishadowRunner = new TiShadowRunner("Server");
				tishadowRunner
						.setAttribute(Constants.TISHADOW_WORKING_DIRECTORY,
								"")
						.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS,
								"server")
						.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
						.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES,
								LaunchUtils.getEnvVars())
						.setAttribute(Constants.TISHADOW_LOCATION,
								PreferenceValues.getTishadowDirectory());
				tishadowRunner.runTiShadow(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		System.out.println("LEVANTADOOO!!!");

	}

}

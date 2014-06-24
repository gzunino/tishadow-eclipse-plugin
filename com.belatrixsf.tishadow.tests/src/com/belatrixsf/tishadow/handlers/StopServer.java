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

public class StopServer extends AbstractHandler implements IRunnerCallback {
	
	boolean isServerLaunched = false;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		isServerLaunched = LaunchUtils.isServerLaunched();
		if (isServerLaunched) {
			MessageDialog.openError(null, "Stop",
					"Fuck");
		} 
		return null;
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		System.out.println("TiShadow server started.");

	}

}

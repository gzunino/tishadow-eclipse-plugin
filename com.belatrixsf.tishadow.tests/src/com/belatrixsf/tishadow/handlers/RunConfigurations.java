package com.belatrixsf.tishadow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.actions.OpenRunConfigurations;

import com.belatrixsf.tishadow.runner.IRunnerCallback;

public class RunConfigurations extends AbstractHandler implements
		IRunnerCallback {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		OpenRunConfigurations orc = new OpenRunConfigurations();
		orc.run();
		return null;
	}
	
	@Override
	public void onRunnerTishadowFinish(Object response) {
		// TODO Auto-generated method stub
		
	}

}

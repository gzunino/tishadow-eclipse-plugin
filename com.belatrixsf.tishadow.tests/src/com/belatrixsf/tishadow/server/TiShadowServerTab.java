package com.belatrixsf.tishadow.server;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Composite;

import com.belatrixsf.tishadow.common.ArgsBuilder;
import com.belatrixsf.tishadow.common.Argument;
import com.belatrixsf.tishadow.common.TiShadowTab;

public class TiShadowServerTab extends TiShadowTab {

	private ArgsBuilder argsBuilder = new ArgsBuilder();

	public TiShadowServerTab() {
		argumentsList = getTabOptions();
		argumentsString = super.argumentsToString("server", argumentsList);
	}

	protected ArrayList<Argument> getTabOptions() {
		return argsBuilder.getServerDefaults();
	}
	
	
	protected void createArgumentComponent(Composite parent) {
		super.createArgumentComponent(parent);
	}
	
	protected void updateArgument(ILaunchConfiguration configuration) {
		super.updateArgument(configuration);
	}

}
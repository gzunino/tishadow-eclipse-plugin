package com.belatrixsf.tishadow.tests;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Composite;

import com.belatrixsf.tishadow.common.ArgsBuilder;
import com.belatrixsf.tishadow.common.Argument;
import com.belatrixsf.tishadow.common.TiShadowTab;

public class TiShadowSpecTab extends TiShadowTab {
	
	private ArgsBuilder argsBuilder = new ArgsBuilder();

	public TiShadowSpecTab() {
		argumentsList = getTabOptions();
		argumentsString = super.argumentsToString("spec", argumentsList);
	}

	protected ArrayList<Argument> getTabOptions() {
		return argsBuilder.getSpecDefaults();
	}
	
	
	protected void createArgumentComponent(Composite parent) {
		super.createArgumentComponent(parent);
	}
	
	protected void updateArgument(ILaunchConfiguration configuration) {
		super.updateArgument(configuration);
	}
	
}

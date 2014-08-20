package com.belatrixsf.tishadow.run;

import java.util.ArrayList;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Composite;

import com.belatrixsf.tishadow.common.ArgsBuilder;
import com.belatrixsf.tishadow.common.Argument;
import com.belatrixsf.tishadow.common.TiShadowTab;

public class TiShadowRunTab extends TiShadowTab {
	
	private ArgsBuilder argsBuilder = new ArgsBuilder();

	public TiShadowRunTab() {
		argumentsList = getTabOptions();
		argumentsString = super.argumentsToString("run", argumentsList);
	}

	protected ArrayList<Argument> getTabOptions() {
		return argsBuilder.getRunDefaults();
	}
	
	
	protected void createArgumentComponent(Composite parent) {
		super.createArgumentComponent(parent);
	}
	
	protected void updateArgument(ILaunchConfiguration configuration) {
		super.updateArgument(configuration);
	}
	
}
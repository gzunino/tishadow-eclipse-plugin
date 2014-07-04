package com.belatrixsf.tishadow.tests.tester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.belatrixsf.tishadow.LaunchUtils;

public class ReRunTester extends PropertyTester {

	public ReRunTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if ("hasPreviousLaunch".equals(property)) {
			LaunchUtils launchUtil = new LaunchUtils();
			final ILaunchConfiguration lConfig = launchUtil.getLaunchConfiguration();
			boolean hasPrevLaunch = lConfig != null;
			return (expectedValue.equals(hasPrevLaunch));
		}
		return false;
	}
	
}

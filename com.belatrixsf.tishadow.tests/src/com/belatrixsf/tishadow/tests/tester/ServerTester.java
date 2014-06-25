package com.belatrixsf.tishadow.tests.tester;

import org.eclipse.core.expressions.PropertyTester;

import com.belatrixsf.tishadow.LaunchUtils;

public class ServerTester extends PropertyTester {

	public ServerTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (!"serverRunning".equals(property)){
			return false;
		}
		System.out.println("ServerTester >> isServerLaunched: " + LaunchUtils.isServerLaunched());
		return expectedValue.equals(LaunchUtils.isServerLaunched());
	}
	
}

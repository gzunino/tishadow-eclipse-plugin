package com.belatrixsf.tishadow;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

public class MenuPulldown extends CompoundContributionItem implements
		IWorkbenchContribution {

	protected IServiceLocator mServiceLocator;

	public static final String CONFIGURATION_PARAMETER = "com.belatrixsf.tishadow.config";
	public static final String RUN_TYPE_PARAMETER = "com.belatrixsf.tishadow.runType";

	public MenuPulldown() {
	}

	public MenuPulldown(final String id) {
		super(id);
	}

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		mServiceLocator = serviceLocator;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		// TODO Auto-generated method stub
		return null;
	}
}

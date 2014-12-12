package com.belatrixsf.tishadow.run;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.belatrixsf.tishadow.MenuPulldown;

public class RunTiShadowPulldown extends MenuPulldown{
	
	public static final String COMMAND_ID = "com.belatrixsf.tishadow.run.runDeploy";
	public static final String LAUNCH_DEPLOY_CONFIGURATION_PARAMETER = "com.belatrixsf.tishadow.run.launchTiShadowRun";

	public RunTiShadowPulldown() {
	}

	public RunTiShadowPulldown(final String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {

		ILaunchManager launchManager = DebugPlugin.getDefault()
				.getLaunchManager();
		ILaunchConfigurationType type = launchManager
				.getLaunchConfigurationType(LAUNCH_DEPLOY_CONFIGURATION_PARAMETER);
		ILaunchConfiguration[] launchs = null;

		try {
			launchs = launchManager.getLaunchConfigurations(type);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		IContributionItem[] ici = new IContributionItem[launchs.length];

		for (int i = 0; i < launchs.length; i++) {

			Map<String, String> pullDownConfig = new HashMap<String, String>();
			
			pullDownConfig.put(CONFIGURATION_PARAMETER, launchs[i].getName());
			pullDownConfig.put(RUN_TYPE_PARAMETER, "deploy");

			final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(
					mServiceLocator, null, COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			contributionParameter.label = (i + 1) + ". " + launchs[i].getName();
			contributionParameter.visibleEnabled = true;
			contributionParameter.parameters = pullDownConfig;
			ici[i] = new CommandContributionItem(contributionParameter);
		}

		return ici;
	}
}
package com.belatrixsf.tishadow.tests;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

public class RunTestPulldown extends CompoundContributionItem implements IWorkbenchContribution {  
	  
	 private IServiceLocator mServiceLocator;  
	  
	 public RunTestPulldown() {  
	 }  
	  
	 public RunTestPulldown(final String id) {  
	  super(id);  
	 }  
	  
	 @Override  
	 protected IContributionItem[] getContributionItems() {  
	  
		 
		 ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		 ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("com.belatrixsf.tishadow.tests.launchTiShadowTests");
         ILaunchConfiguration[] launchs = null;
         
         try {
			launchs = launchManager.getLaunchConfigurations(type);
		 } catch (CoreException e) {
			e.printStackTrace();
		 }
		 
		 IContributionItem[] ici = new IContributionItem[launchs.length];
		 
		 for (int i = 0 ; i < launchs.length ; i++) {
			 
			 Map<String,String> pullDownConfig = Collections.singletonMap("com.belatrixsf.tishadow.tests.config", launchs[i].getName());
			 
			 final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(mServiceLocator, null, "com.belatrixsf.tishadow.tests.runTest",  
					    CommandContributionItem.STYLE_PUSH);
			 contributionParameter.label = (i+1) + ". " + launchs[i].getName();
			 contributionParameter.visibleEnabled = true;
			 contributionParameter.parameters = pullDownConfig;
			 ici[i] = new CommandContributionItem(contributionParameter);
		 }
		  
		  return ici;  
	 }  
	  
	 @Override  
	 public void initialize(final IServiceLocator serviceLocator) {  
	  mServiceLocator = serviceLocator;  
	 }  
	  
	 @Override  
	 public boolean isDirty() {  
	  return true;  
	 }  
	}  
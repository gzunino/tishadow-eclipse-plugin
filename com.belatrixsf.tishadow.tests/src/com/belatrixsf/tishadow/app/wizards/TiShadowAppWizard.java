package com.belatrixsf.tishadow.app.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

public class TiShadowAppWizard extends BasicNewProjectResourceWizard implements
		INewWizard, IRunnerCallback {
	
	@Override
	public void addPages() {
		super.addPages();
		
		((WizardNewProjectCreationPage) getPage("basicNewProjectPage")).setInitialProjectName("tishadowapp");
	}
	
	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish() {
		boolean finished = super.performFinish();
		
		IProject project = getNewProject();
		
		if (project != null) {
			createTiProject(project);
		}
		
		return finished;
	}

	private void createTiProject(final IProject project) {
		try {
			TiShadowRunner tiShadowRunner = new TiShadowRunner("tishadow app");
			tiShadowRunner
					.setAttribute(Constants.TISHADOW_LOCATION,
							PreferenceValues.getTishadowDirectory())
					.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
					.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS,
							"app -d " + project.getLocation().toOSString())
					.setAttribute(Constants.TISHADOW_LOCATION,
							project.getParent().getLocation().toOSString())
					.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES,
							LaunchUtils.getEnvVars());
			tiShadowRunner.runTiShadow(this, "com.test.app");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void addTiNature(IProject project) {
		IProjectDescription description;
		try {
			description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 2];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = "com.appcelerator.titanium.mobile.nature";
			newNatures[natures.length + 1] = "com.aptana.projects.webnature";
			IStatus status = ResourcesPlugin.getWorkspace().validateNatureSet(newNatures);
			// check the status and decide what to do
		    if (status.getCode() == IStatus.OK) {
		    	description.setNatureIds(newNatures);
				project.setDescription(description, new NullProgressMonitor());
		    }
		} catch (CoreException e) {
			LaunchUtils.handleError("Cannot add Ti project nature", e);
		}
	}

	public static Map<String, String> getEnvVars() {
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		return envVariables;
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		try {
			addTiNature(getNewProject());
			getNewProject().refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
	}
}
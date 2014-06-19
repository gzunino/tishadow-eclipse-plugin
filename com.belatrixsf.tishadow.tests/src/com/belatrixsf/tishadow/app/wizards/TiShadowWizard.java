package com.belatrixsf.tishadow.app.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.belatrixsf.tishadow.LaunchUtils;
import com.belatrixsf.tishadow.preferences.page.Helper;
import com.belatrixsf.tishadow.preferences.page.PreferenceValues;
import com.belatrixsf.tishadow.runner.Constants;
import com.belatrixsf.tishadow.runner.IRunnerCallback;
import com.belatrixsf.tishadow.runner.TiShadowRunner;

@SuppressWarnings("restriction")
public abstract class TiShadowWizard extends
		BasicNewProjectResourceWizard implements INewWizard, IRunnerCallback {

	protected TiShadowPage wizardPage;
	private WizardNewProjectReferencePage referencePage = null;
	private IProject newProject;
	private WorkingSetGroup workingSetGroup;

	abstract String getArguments();
	
	abstract String getTiShadowCommandName();
	
	public IProject getNewProject() {
		return newProject;
	}
	
	@Override
	public void onRunnerTishadowFinish(Object response) {
		try {
			addTiNature(newProject);
			newProject.refreshLocal(IProject.DEPTH_INFINITE,
					new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a working set group for this page. This method can only be called
	 * once.
	 * 
	 * @param composite
	 *            the composite in which to create the group
	 * @param selection
	 *            the current workbench selection
	 * @param supportedWorkingSetTypes
	 *            an array of working set type IDs that will restrict what types
	 *            of working sets can be chosen in this group
	 * @return the created group. If this method has been called previously the
	 *         original group will be returned.
	 * @since 3.4
	 */
	public WorkingSetGroup createWorkingSetGroup(Composite composite,
			IStructuredSelection selection, String[] supportedWorkingSetTypes) {
		if (workingSetGroup != null)
			return workingSetGroup;
		workingSetGroup = new WorkingSetGroup(composite, selection,
				supportedWorkingSetTypes);
		return workingSetGroup;
	}

	
	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish() {
		createNewProject();
		if (newProject == null) {
			return false;
		}
		createTiShadowProject();
		return true;
	}

	private void createTiShadowProject() {
		String arguments = getArguments();
		try {
			executeTiShadowCommands(getTiShadowCommandName(), arguments);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeTiShadowCommands(String configurationName, String arguments)
			throws Exception {
		
		TiShadowRunner tishadowRunner = new TiShadowRunner(configurationName);
		tishadowRunner
				.setAttribute(Constants.TISHADOW_LOCATION,
						PreferenceValues.getTishadowDirectory())
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
				.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, arguments)
				.setAttribute(Constants.TISHADOW_WORKING_DIRECTORY, getWorkingDirectory())
				.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES,
						Helper.getEnvVars());
		tishadowRunner.runTiShadow(this, getInputForRunTiShadowCommand());
	}

	abstract String getWorkingDirectory();
	
	/**This is an optional parameter for Run TiShadow command, it can be null or any string*/
	abstract String getInputForRunTiShadowCommand();

	/**
	 * Adds the correct folder name if default location is selected
	 * 
	 * @param outputFolder
	 * @param wizardPage
	 */

	protected String getOutputFolderPath() {
		String outputFolderPath;
		outputFolderPath = wizardPage.getOutputFolder().getProjectLocation();
		if (wizardPage.getOutputFolder().isDefault()) {
			outputFolderPath += "/" + wizardPage.getProjectName();
		}
		return outputFolderPath;

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
			// check the status and decide what to do
			if (TiShadowRunner.isValidNature(newNatures)) {
				description.setNatureIds(newNatures);
				project.setDescription(description, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			LaunchUtils.handleError("Cannot add Ti project nature", e);
		}
	}

	/**
	 * Creates a new project resource with the selected name.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish
	 * on the wizard; the enablement of the Finish button implies that all
	 * controls on the pages currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this wizard caches the new project once it has been
	 * successfully created; subsequent invocations of this method will answer
	 * the same project resource without attempting to create it again.
	 * </p>
	 * 
	 * @return the created project resource, or <code>null</code> if the project
	 *         was not created
	 */
	private IProject createNewProject() {
		if (newProject != null) {
			return newProject;
		}

		// get a project handle
		final IProject newProjectHandle = ResourcesPlugin.getWorkspace()
				.getRoot().getProject(wizardPage.getProjectName());

		// get a project descriptor
		URI location = null;
		if (!wizardPage.getOutputFolder().isDefault()) {
			location = wizardPage.getOutputFolder().getProjectLocationURI();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace
				.newProjectDescription(newProjectHandle.getName());
		description.setLocationURI(location);

		// update the referenced project if provided
		if (referencePage != null) {
			IProject[] refProjects = referencePage.getReferencedProjects();
			if (refProjects.length > 0) {
				description.setReferencedProjects(refProjects);
			}
		}

		// create the new project operation
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				CreateProjectOperation op = new CreateProjectOperation(
						description, ResourceMessages.NewProject_windowTitle);
				try {
					// see bug
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219901
					// directly execute the operation so that the undo state is
					// not preserved. Making this undoable resulted in too many
					// accidental file deletions.
					op.execute(monitor,
							WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (ExecutionException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof ExecutionException
					&& t.getCause() instanceof CoreException) {
				CoreException cause = (CoreException) t.getCause();
				StatusAdapter status;
				if (cause.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					status = new StatusAdapter(
							StatusUtil.newStatus(
									IStatus.WARNING,
									NLS.bind(
											ResourceMessages.NewProject_caseVariantExistsError,
											newProjectHandle.getName()), cause));
				} else {
					status = new StatusAdapter(StatusUtil.newStatus(cause
							.getStatus().getSeverity(),
							ResourceMessages.NewProject_errorMessage, cause));
				}
				status.setProperty(StatusAdapter.TITLE_PROPERTY,
						ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status, StatusManager.BLOCK);
			} else {
				StatusAdapter status = new StatusAdapter(new Status(
						IStatus.WARNING, IDEWorkbenchPlugin.IDE_WORKBENCH, 0,
						NLS.bind(ResourceMessages.NewProject_internalError,
								t.getMessage()), t));
				status.setProperty(StatusAdapter.TITLE_PROPERTY,
						ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status,
						StatusManager.LOG | StatusManager.BLOCK);
			}
			return null;
		}

		newProject = newProjectHandle;

		return newProject;
	}

}

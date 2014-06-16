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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
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
public class TiShadowAppifyWizard extends BasicNewProjectResourceWizard
		implements INewWizard, IRunnerCallback {

	TiShadowAppifyWizardPage wizardPage = null;
	private WizardNewProjectReferencePage referencePage = null;
	// cache of newly-created project
	private IProject newProject;

	@Override
	public void addPages() {
		wizardPage = new TiShadowAppifyWizardPage("Properties Page");
		wizardPage.setTitle("Project");
		wizardPage.setDescription("Settings");

		this.addPage(wizardPage);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish() {
		boolean finished = performSuperFinish();
		if (getNewProject() != null) {
			createTiProject();
		}
		return finished;
	}

	private void createTiProject() {
		String inputFolder = wizardPage.getSelectedBaseProjectPath();
		String arguments = getArguments();
		try {
			appifyProject(arguments, inputFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getArguments() {
		String outputFolder = getOutputFolderPath();
		String host = wizardPage.getHostFieldValue();
		String port = wizardPage.getPortFieldValue();
		String room = wizardPage.getRoomFieldValue();
		/**
		 * TiShadow appify command:
		 * 
		 * tishadow appify -d <dest_directory> -o <host> -p <port> -r <room> 
		 */
		String arguments = "appify -d " + outputFolder;
		// host, port and room flags are optional. If they are empty default
		// values will be used.
		// + " -o " + host + " -p " + port;
		arguments = host.isEmpty() ? arguments : (arguments + " -o '" + host + "'");
		arguments = port.isEmpty() ? arguments : (arguments + " -p '" + port + "'");
		arguments = room.isEmpty() ? arguments : (arguments + " -r '" + room + "'");
		return arguments;
	}
	

	private void appifyProject(String arguments, String inputFolder)
			throws Exception {
		String configurationName = "TiShadow Appify";
		TiShadowRunner tishadowRunner = new TiShadowRunner(configurationName);
		tishadowRunner
				.setAttribute(Constants.TISHADOW_LOCATION,
						PreferenceValues.getTishadowDirectory())
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
				.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, arguments)
				.setAttribute(Constants.TISHADOW_WORKING_DIRECTORY, inputFolder)
				.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES,
						Helper.getEnvVars());
		tishadowRunner.runTiShadow(this);
	}

	/**
	 * Adds the correct folder name if default location is selected
	 * 
	 * @param outputFolder
	 * @param wizardPage
	 */
	
	private String getOutputFolderPath() {
		String outputFolderPath;
		outputFolderPath = wizardPage.getOutputFolderLocation();
		if (wizardPage.useDefaults()) {
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
		final IProject newProjectHandle = wizardPage.getProjectHandle();

		// get a project descriptor
		URI location = null;
		if (!wizardPage.useDefaults()) {
			location = wizardPage.getLocationURI();
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

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performSuperFinish() {
		createNewProject();

		if (newProject == null) {
			return false;
		}

		IWorkingSet[] workingSets = wizardPage.getSelectedWorkingSets();
		getWorkbench().getWorkingSetManager().addToWorkingSets(newProject,
				workingSets);

		updatePerspective();
		selectAndReveal(newProject);

		return true;
	}

	/**
	 * Returns the newly created project.
	 * 
	 * @return the created project, or <code>null</code> if project not created
	 */
	public IProject getNewProject() {
		return newProject;
	}

	@Override
	public void onRunnerTishadowFinish(Object response) {
		try {
			IProject newProject = getNewProject();
			addTiNature(newProject);
			newProject.refreshLocal(IProject.DEPTH_INFINITE,
					new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
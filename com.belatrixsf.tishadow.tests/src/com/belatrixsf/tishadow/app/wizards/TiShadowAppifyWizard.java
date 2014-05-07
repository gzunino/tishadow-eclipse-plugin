package com.belatrixsf.tishadow.app.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
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
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
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

public class TiShadowAppifyWizard extends BasicNewProjectResourceWizard
		implements INewWizard {

	TiShadowAppifyWizardPage page = null;
	private WizardNewProjectReferencePage referencePage = null;
	// cache of newly-created project
	private IProject newProject;

	@Override
	public void addPages() {
		page = new TiShadowAppifyWizardPage("Properties Page");
		page.setTitle("Project");
		page.setDescription("Settings");

		this.addPage(page);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish() {
		boolean finished = performSuperFinish();

		IProject project = getNewProject();

		if (project != null) {
			createTiProject(project);
		}

		return finished;
	}

	private void createTiProject(final IProject project) {

		String arguments;
		String inputFolder;
		String outputFolder;
		String host;
		String port;
		String room = "";
		TiShadowAppifyWizardPage propertiesPage;

		try {
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType type = DebugPlugin
					.getDefault()
					.getLaunchManager()
					.getLaunchConfigurationType(
							"org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(
					null, launchManager
							.generateLaunchConfigurationName("tishadow app"));

			inputFolder = page.getSelectedBaseProjectPath();
			outputFolder = page.getOutputFolderLocation();
			host = page.getHostFieldValue();
			port = page.getPortFieldValue();
			room = page.getRoomFieldValue();

			/**
			 * TiShadow appify command:
			 * 
			 * appify -d <dest_directory> -o <host> -p <port> -r <room> 
			 */
			arguments = "appify -d " + outputFolder;

			// host, port and room are optional. If they are empty default
			// values will be used.
			// + " -o " + host + " -p " + port;
			arguments = (host.equals("") ? arguments
					: (arguments + " -o " + host));
			arguments = (port.equals("") ? arguments
					: (arguments + " -p " + port));
			arguments = (room.equals("") ? arguments
					: (arguments + " -r " + room));

			workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION,
					"/usr/local/bin/tishadow");
			workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE,
					true);
			workingCopy.setAttribute(
					IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
			workingCopy.setAttribute(
					IExternalToolConstants.ATTR_WORKING_DIRECTORY, inputFolder);
			workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
					LaunchUtils.getEnvVars());

			ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE,
					new NullProgressMonitor());

			DebugPlugin.getDefault().addDebugEventListener(
					new IDebugEventSetListener() {
						@Override
						public void handleDebugEvents(DebugEvent[] events) {

							if (events.length > 0
									&& (events[0].getKind() == DebugEvent.TERMINATE)) {
								DebugPlugin.getDefault()
										.removeDebugEventListener(this);
								try {
									addTiNature(project);
									project.refreshLocal(
											IProject.DEPTH_INFINITE,
											new NullProgressMonitor());
								} catch (CoreException e) {
									e.printStackTrace();
								}
							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addTiNature(IProject project) {
		IProjectDescription description;
		try {
			description = project.getDescription();

			String[] natures = description.getNatureIds();

			String[] newNatures = new String[natures.length + 1];

			System.arraycopy(natures, 0, newNatures, 0, natures.length);

			newNatures[natures.length] = "com.appcelerator.titanium.mobile.nature";

			description.setNatureIds(newNatures);

			project.setDescription(description, new NullProgressMonitor());
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
		final IProject newProjectHandle = page.getProjectHandle();

		// get a project descriptor
		URI location = null;
		if (!page.useDefaults()) {
			location = page.getLocationURI();
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

		IWorkingSet[] workingSets = page.getSelectedWorkingSets();
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
}
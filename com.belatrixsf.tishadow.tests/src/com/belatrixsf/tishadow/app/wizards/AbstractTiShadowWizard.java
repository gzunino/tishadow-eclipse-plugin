package com.belatrixsf.tishadow.app.wizards;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
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
public abstract class AbstractTiShadowWizard extends
		BasicNewProjectResourceWizard implements INewWizard, IRunnerCallback {

	protected AbstractTiShadowPage wizardPage;
	private IProject newProject;
	private WorkingSetGroup workingSetGroup;
	private Job job = null;
	File tempFile = null;

	abstract String getArguments();
	
	abstract String getTiShadowCommandName();
	
	public IProject getNewProject() {
		return newProject;
	}
	
	@Override
	public void onRunnerTishadowFinish(Object response) {
		try {
			if (job != null){
				job.done(Status.OK_STATUS);
			}
			
			recoverDotFile();
			
			newProject.refreshLocal(IProject.DEPTH_INFINITE,
					new NullProgressMonitor());
			addTiNature(newProject);
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected void recoverDotFile() {
		try {
			File newDotProjectFile = new File(newProject.getLocation()+"/.project");
			
			FileInputStream fis = new FileInputStream(tempFile);
		    byte[] data = new byte[(int)tempFile.length()];
		    fis.read(data);
		    fis.close();
		    
		    String s = new String(data, "UTF-8");
		    
			BufferedWriter bw = new BufferedWriter(new FileWriter(newDotProjectFile));
			bw.write(s);
			bw.close();
		} catch (Exception e) {
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
		IProject project = createNewProject();
		
		backupDotProjectFile(project);
		
		if (newProject == null) {
			return false;
		}
		
		createTiShadowProject();
		
		return true;
	}
	
	protected void backupDotProjectFile(IProject project){
		try {
			tempFile = File.createTempFile(".project", "tmp", null);
			IFile dotProjectIFile = project.getFile(".project");
			File dotProjectFile = dotProjectIFile.getLocation().toFile();
			
			FileInputStream fis = new FileInputStream(dotProjectFile);
		    byte[] data = new byte[(int)dotProjectFile.length()];
		    fis.read(data);
		    fis.close();
		    
		    String s = new String(data, "UTF-8");
		    
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
			bw.write(s);
			bw.close();
		} catch (Exception e){
			e.printStackTrace();
		}
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
		
		final TiShadowRunner tishadowRunner = new TiShadowRunner(configurationName);
		tishadowRunner
				.setAttribute(Constants.TISHADOW_LOCATION,
						PreferenceValues.getTishadowDirectory())
				.setAttribute(Constants.TISHADOW_SHOW_CONSOLE, true)
				.setAttribute(Constants.TISHADOW_TOOL_ARGUMENTS, arguments)
				.setAttribute(Constants.TISHADOW_WORKING_DIRECTORY, getWorkingDirectory())
				.setAttribute(Constants.TISHADOW_ENVIRONMENT_VARIABLES,
						Helper.getEnvVars());
		
		final String inputForRunTiShadowCommand = getInputForRunTiShadowCommand();
		final String tishadowJob = "TiShadow App Creation";
		
		job = new Job(tishadowJob) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				this.setThread(Thread.currentThread());
				try {

					monitor.beginTask("Creating TiShadow App..", 200);
					tishadowRunner.runTiShadow(AbstractTiShadowWizard.this, inputForRunTiShadowCommand, monitor);
										
				} catch (Exception e) {
					stopJob(this, tishadowJob);
					e.printStackTrace();
				}
				return ASYNC_FINISH;
			}
		};
		job.setUser(true);
		job.schedule();

	}
	
	private void stopJob(Job currentJob, String tishadowJob) {
		currentJob.cancel();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Display display = Display.getDefault();
				Shell shell = display.getActiveShell();
				MessageDialog.openError(shell, "Error executing TiShadow Command",
						"There was a problem while trying to run TiShadow. \nPlease check your TiShadow configuration path on Window>Preferences>Tishadow");
			}
		});
		currentJob.done(null);
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
		IProject[] refProjects = new IProject[0];
		setReferencedProjects(description, refProjects);

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
				status.setProperty(IStatusAdapterConstants.TITLE_PROPERTY,
						ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status, StatusManager.BLOCK);
			} else {
				StatusAdapter status = new StatusAdapter(new Status(
						IStatus.WARNING, IDEWorkbenchPlugin.IDE_WORKBENCH, 0,
						NLS.bind(ResourceMessages.NewProject_internalError,
								t.getMessage()), t));
				status.setProperty(IStatusAdapterConstants.TITLE_PROPERTY,
						ResourceMessages.NewProject_errorMessage);
				StatusManager.getManager().handle(status,
						StatusManager.LOG | StatusManager.BLOCK);
			}
			return null;
		}

		newProject = newProjectHandle;
		/*
		final Job job = Job.getJobManager().currentJob();
		job.done(Status.OK_STATUS);*/
		
		return newProject;
	}

	protected void setReferencedProjects(final IProjectDescription description,
			IProject[] refProjects) {
		if (refProjects.length > 0) {
			description.setReferencedProjects(refProjects);
		}
	}

}

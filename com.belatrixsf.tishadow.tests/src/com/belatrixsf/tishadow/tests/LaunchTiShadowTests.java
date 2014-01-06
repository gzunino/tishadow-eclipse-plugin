package com.belatrixsf.tishadow.tests;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.internal.junit.ui.JUnitViewEditorLauncher;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class LaunchTiShadowTests implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		final SubMonitor mon = SubMonitor.convert(monitor);
		mon.beginTask("Running Tests", IProgressMonitor.UNKNOWN);
		
		final String projectLoc = configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "");
		final IProject project = getProject(projectLoc);
		if (project == null || !project.isOpen()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(null, "Error", "Project " + ((project != null) ? project.getName() : "" )+" is closed or doesn't exists");
					mon.done();
				}
			});
			return;
		}

		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType("org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
		
		final ILaunchConfigurationWorkingCopy workingCopy =
			      type.newInstance( null, "TiShadow Spec");
		
		workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, "/usr/local/bin/tishadow");
		workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "spec -x");
		workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, projectLoc);
		
		workingCopy.launch(mode, mon);
		
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				if (events.length > 0 && (events[0].getKind() == DebugEvent.TERMINATE)) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
					
					IFolder folder = getTiShadowResultFolder(projectLoc);
					
					final IPath junitXML = getXmlResults(mon, folder);
					
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								workingCopy.delete();
							} catch (CoreException e) {
								e.printStackTrace();
							}
							if (junitXML == null) {
								MessageDialog.openError(null, "Error", "Cannot find Junit XML results for TiShadow run");
								return;
							}
							JUnitViewEditorLauncher junit = new JUnitViewEditorLauncher();
							junit.open(junitXML);
							
							mon.done();
						}
					});
				}
			}

		});
		
	}
	
	private IFolder getTiShadowResultFolder(final String projectLoc) {
		IProject project = getProject(projectLoc);
		IFolder folder = project.getFolder(Path.fromOSString("build/tishadow/"));
		return folder;
	}
	
	private IProject getProject(final String projectLoc) {
		try {
			String projectName = Path.fromOSString(projectLoc).lastSegment();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			return project;
		} catch (Exception e) {
			return null;
		}
	}
	
	private IPath getXmlResults(final IProgressMonitor monitor, IFolder folder) {
		IPath jUnitResource = null;
		if (folder.exists()) {
			try {
				folder.refreshLocal(1, monitor);
				IResource[] members;
				members = folder.members();
				for (IResource iResource : members) {
					if (iResource.getFullPath().toPortableString().contains("_result.xml")) {
						jUnitResource = iResource.getLocation();
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return jUnitResource;
	}
	
	public static String getLaunchDir(IProject project) {
		return project.getLocation().toPortableString();
	}

	public static void setLaunchAttributes(ILaunchConfigurationWorkingCopy configuration, IResource context) {
		configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION,
				"/usr/local/bin/tishadow");
		String project = null;
		if (context != null) {
			project = LaunchTiShadowTests.getLaunchDir(context.getProject());
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, project);
		configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "spec -x");
		configuration.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
	}

}

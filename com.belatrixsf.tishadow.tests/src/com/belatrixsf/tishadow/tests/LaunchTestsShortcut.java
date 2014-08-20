package com.belatrixsf.tishadow.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.belatrixsf.tishadow.common.ArgsBuilder;

@SuppressWarnings("restriction")
public class LaunchTestsShortcut implements ILaunchShortcut {
    
    private ArrayList<IProject> allowedProjectsList;


    @Override
    public void launch(ISelection selection, String mode) {
    	IProject project = null;
    	if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection)selection).getFirstElement();
            if (element instanceof IResource) {
                project = ((IResource) element).getProject();
            }
        } else if (selection instanceof ITextSelection){
        	project = getCurrentProjectFromITextSelection();
        }
    	
    	if (project != null){
    		searchAndLaunch(project, mode);
    	} else {
    		MessageDialog.openError(null, "Error", "To launch TiShadow tests, you need to select a project.");
    	}
    }

    private IProject getCurrentProjectFromITextSelection() {
    	IProject project = null;
    	
    	IWorkbenchPart sourcePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
    	
          if (sourcePart instanceof IEditorPart)
          {
            IEditorPart editorPart = (IEditorPart) sourcePart;
            IResource resource = (IResource)editorPart.getEditorInput().getAdapter(IResource.class);
            if (resource != null)
            {
              project = resource.getProject();
            }
          }
    	return project;
	}
    
	private void searchAndLaunch(IProject project, final String mode) {
		final ILaunchConfiguration launch = getExistingLaunch(project);
		if (launch != null) {
			Job job = new Job("TiShadow Tests") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					this.setThread(Thread.currentThread());
					try {
						launch.launch(mode, monitor);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					return ASYNC_FINISH;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

    public ILaunchConfiguration getExistingLaunch(IProject project) {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("com.belatrixsf.tishadow.tests.launchTiShadowTests");
        ILaunchConfiguration[] launchs;
        try {
            launchs = launchManager.getLaunchConfigurations(type);
            for (ILaunchConfiguration iLaunchConfiguration : launchs) {
                try {
                    if ( isSameLaunch(project, iLaunchConfiguration)) {
                        return iLaunchConfiguration;
                    }
                } catch (CoreException e) {
                }
            }
        }
        catch (CoreException e1) {
        	e1.printStackTrace();
        }
        
	    String selectedTarget = "";
	    if (isTiModule(project)){
	    	selectedTarget = getSelectedTiShadowProject();
	    	if (selectedTarget.isEmpty()){
	    		return null;
	    	}
	    }
	    	    
	    try {
	        ILaunchConfigurationWorkingCopy launch = type.newInstance(null, launchManager.generateLaunchConfigurationName(project.getName()));
	        LaunchTiShadowTests.setLaunchAttributesWithArguments(launch, project, new ArgsBuilder().getSpecDefaultsString() + selectedTarget);
	        return launch.doSave();
	    }
	    catch (CoreException e) {
	        e.printStackTrace();
	    }
    	return null;
    }
    
    /**
     * Returns the -T command plus the project name for running specs on a TiShadow app.
     * @return selected TiShadow project name or empty String.
     */
    private String getSelectedTiShadowProject() {
    	String tiShadowProjectName;
    	ArrayList<IProject> tiShadowProjectsList;
    	tiShadowProjectsList = getTiShadowProjectsList();
    	IProject tiShadowProjectSelected = null;
    	
    	if (tiShadowProjectsList.isEmpty()){
    		MessageDialog.openError(null, "No TiShadow appified projects found", "To run tests on a Titanium module, you need a Tishadow app to run on.");
    	} else {
    		tiShadowProjectSelected = getSelectedProject(tiShadowProjectsList);
	    	if (! tiShadowProjectSelected.getName().isEmpty()){
	    		tiShadowProjectName = getAppifyedBaseProjectName(tiShadowProjectSelected);
	    		return " -T " + tiShadowProjectName;
	    	}
    	}
    	
    	return "";
    }
    
    public String getAppifyedBaseProjectName(IProject tiShadowProjectSelected) {
    	IFile appJs = tiShadowProjectSelected.getFolder("Resources").getFile("app.js");
    	if (appJs.exists()) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(appJs.getContents()));
            } catch (CoreException e) {
                e.printStackTrace();
            }
            try {
                String inputLine;
				while ((inputLine = in.readLine()) != null) {
                    if (inputLine.contains("TiShadow.Appify")){
                    	//TiShadow.Appify = "ProjectName";
                        return inputLine.substring(inputLine.indexOf("\"")+1, inputLine.lastIndexOf("\""));
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
    	}
    	return "";
    }

	/**
     * Detects if the selected project is a Titanium Module
     * @param project
     * @return
     */
    private boolean isTiModule(IProject project) {
    	IFile timodule = project.getFile("timodule.xml");
        if (timodule.exists()) {
        	return true;
    	}
        return false;
    }
    
    /**
     * Display the list of appifyed projects to select and return the project name of the selection.
     * @param projectsList
     * @return the selected project name
     */
    public IProject getSelectedProject(ArrayList<IProject> projectsList) {
    	
    	ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(Display.getCurrent().getActiveShell(), new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider() {
    		@SuppressWarnings("unchecked")
			@Override
    		public Object[] getElements(Object element) {
    			return ((ArrayList<IProject>)element).toArray();
    		}
    		@Override
    		public Object[] getChildren(Object element) {
    			return new Object[0];
    		}
    	});
    	dialog.setTitle("Project Selection");
    	dialog.setMessage("Select the TiShadow project you want to test from the list:");
    	dialog.setInput(projectsList);
    	dialog.open();
    	if(dialog.getReturnCode() == 0){ //0 is OK
    		return (IProject)dialog.getFirstResult();
    	} else {
    		return null;
    	}  
    }

    /**
     * Gets all the project names projects that contains appname:TiShadow.
     * @throws IOException 
     */
    public ArrayList<IProject> getTiShadowProjectsList(){
    	
        setAllowedProjectsList(new ArrayList<IProject>());
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        
        for (IProject project : projects) {
        	String inputLine;
        	if(project.isSynchronized(IResource.DEPTH_ONE)){
        		try {
					project.refreshLocal(IResource.DEPTH_ONE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
        	}
            IFile manifest = project.getFile("manifest");
		
            if (manifest.exists()) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(manifest.getContents()));
                    while ((inputLine = in.readLine()) != null) {
                    	if (inputLine.contains("appname:TiShadow")){
                    		getAllowedProjectsList().add(project);
                    		break;
                    	}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return getAllowedProjectsList();
    }
    

    public ArrayList<IProject> getAllowedProjectsList() {
        return allowedProjectsList;
    }

    public void setAllowedProjectsList(ArrayList<IProject> allowedProjectsList) {
        this.allowedProjectsList = allowedProjectsList;
    }
    

    private boolean isSameLaunch(IProject project, ILaunchConfiguration iLaunchConfiguration) throws CoreException {
        return iLaunchConfiguration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "").equals(LaunchTiShadowTests.getLaunchDir(project));
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        if (editor.getClass().getSimpleName().contains(("JSSourceEditor"))) {
            IEditorInput input = editor.getEditorInput();
            IProject project = ((IFileEditorInput) input).getFile().getProject();
            searchAndLaunch(project, mode);
        } 
    }
}
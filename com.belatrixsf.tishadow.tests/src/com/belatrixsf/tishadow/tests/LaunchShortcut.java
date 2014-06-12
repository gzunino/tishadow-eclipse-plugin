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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

@SuppressWarnings("restriction")
public class LaunchShortcut implements ILaunchShortcut {
    
    private ArrayList<IProject> allowedProjectsList;

    @Override
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection)selection).getFirstElement();
            if (element instanceof IResource) {
                IProject project = ((IResource) element).getProject();
                searchAndLaunch(project, mode);
            }
        } 
    }

    private void searchAndLaunch(IProject project, String mode) {
        ILaunchConfiguration launch = getExistingLaunch(project);
        if (launch != null) {
            try {
                launch.launch(mode, new NullProgressMonitor());
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    private ILaunchConfiguration getExistingLaunch(IProject project) {
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
	    	selectedTarget = getSelectedTiShadowProject(project);
	    	if (selectedTarget.isEmpty()){
	    		return null;
	    	}
	    }
	    try {
	        ILaunchConfigurationWorkingCopy launch = type.newInstance(null, launchManager.generateLaunchConfigurationName(project.getName()));
	        LaunchTiShadowTests.setLaunchAttributes(launch, project, selectedTarget);
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
    private String getSelectedTiShadowProject(IProject project) {
    	String tiShadowProjectName;
    	ArrayList<IProject> tiShadowProjectsList;
    	tiShadowProjectsList = getTiShadowProjectsList();
    	
    	if (tiShadowProjectsList.isEmpty()){
    		MessageDialog.openError(null, "No projects found", "To run specs from a Titanium module or a Native Extension its necessary to have at least one TiShadow project to run on.");
    	} else {
    		tiShadowProjectName = getSelectedProject(tiShadowProjectsList);
	    	if (! tiShadowProjectName.isEmpty()){
	    		return " -T " + tiShadowProjectName;
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
    private String getSelectedProject(ArrayList<IProject> projectsList) {
    	
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
    	dialog.setMessage("Select the appifyed project from the list:");
    	dialog.setInput(projectsList);
    	dialog.open();
    	if(dialog.getReturnCode() == 0){ //0 is OK
    		return dialog.getFirstResult().toString().substring(2);
    	} else {
    		return "";
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
            IFile manifest = project.getFile("manifest");
		
            if (manifest.exists()) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(manifest.getContents()));
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                try {
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.contains("appname:TiShadow")){
                            getAllowedProjectsList().add(project);
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
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
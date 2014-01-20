package com.belatrixsf.tishadow.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.internal.junit.ui.JUnitViewEditorLauncher;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		
		final Map<String, String> envVars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<String, String>());
		final String location = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, "");
		final boolean showConsole = configuration.getAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, false);
		final String toolArguments = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");
		
		workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, showConsole);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, toolArguments);
		workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, projectLoc);
		workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envVars);
		
		workingCopy.launch(mode, mon);
		
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				if (events.length > 0 && (events[0].getKind() == DebugEvent.TERMINATE)) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
					
					final IFolder folder = getTiShadowResultFolder(projectLoc);
					
					final ArrayList<IPath> junitXMLResources = getXmlResults(mon, folder);
					
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								workingCopy.delete();
							} catch (CoreException e) {
								e.printStackTrace();
							}
							
							if (junitXMLResources.isEmpty()) {
								MessageDialog.openError(null, "Error", "Cannot find Junit XML results for TiShadow run");
								return;
							}
							
							String tishadowDirectory = folder.getProject().getLocation().toOSString() + "/build/tishadow";
							
							JUnitViewEditorLauncher junit = new JUnitViewEditorLauncher();
							
							refreshProject(project);
							
							mergeXMLFiles(junitXMLResources, tishadowDirectory+"/fullTestSuite.xml");
							
//							for (IPath junitXMLResource : junitXMLResources) {
//								try {
//									ArrayList<String> linesToRemove = new ArrayList<String>();
//									linesToRemove.add("<testsuites>");
//									linesToRemove.add("</testsuites>");
//									
//									removeLine(junitXMLResource.toString(), linesToRemove, true);
//								} catch (IOException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//							}
							
//							FileSet fs = new FileSet();
//							fs.createInclude().setName("*_result.xml");
//							fs.setDir(new File(tishadowDirectory));
//							
//							XMLResultAggregator resultAggregator = new XMLResultAggregator();
//							
//							resultAggregator.setProject(new Project());
//							
//							resultAggregator.addFileSet(fs);
//							
//							resultAggregator.setTodir(new File(tishadowDirectory));
//							
//							resultAggregator.setTofile("fullTestSuite.xml");
//							
//							resultAggregator.execute();
							
							refreshProject(project);
							
							junit.open(new Path(tishadowDirectory+"/fullTestSuite.xml"));
							
							mon.done();
						}
					});
				}
			}

		});
	}
	
	private void refreshProject (IProject project) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	private void stringToXMLFile (String fileContent, String fileName) {
//		
//		try {
//			FileUtils.writeStringToFile(new File(fileName), fileContent);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		FileOutputStream fop = null;
//		File file;
// 
//		try {
// 
//			file = new File(fileName.replace(".xml", ".txt"));
//			fop = new FileOutputStream(file);
// 
//			// if file doesnt exists, then create it
//			if (file.exists()) {
//				file.delete();
//				file.createNewFile();
//			}
// 
//			// get the content in bytes
//			byte[] contentInBytes = fileContent.getBytes();
// 
//			fop.write(contentInBytes);
//			fop.flush();
//			fop.close();
// 
//			System.out.println("Done");
// 
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
//		try {
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//	        DocumentBuilder builder;
//		
//			builder = factory.newDocumentBuilder();
//		
//	        Document documentFiltered = builder.parse(new InputSource(new StringReader(fileContent)));
//	        
//	     // Use a Transformer for output
//	        TransformerFactory tFactory =
//	        TransformerFactory.newInstance();
//	        Transformer transformer = tFactory.newTransformer();
//	
//	        DOMSource source = new DOMSource(documentFiltered);
//	        StreamResult result = new StreamResult(fileName);
//	        transformer.transform(source, result);
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
//	private String readFile (String file) throws IOException {
//		return removeLine(file, new ArrayList<String>(), false);
//	}
	
	private String mergeXMLFiles(ArrayList<IPath> junitXMLResources, String mergedFileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
	
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Document mergedDocument = builder.newDocument();
		mergedDocument.appendChild(mergedDocument.createElement("testsuites"));
		
		for (IPath junitXMLResource : junitXMLResources) {
			try {
		        Document document = builder.parse(new File(junitXMLResource.toString()));
		        
		        Node testSuitesElement = mergedDocument.getElementsByTagName("testsuites").item(0);
		        
		        NodeList testSuiteElements = document.getElementsByTagName("testsuite");
		        
		        for(int i = 0; i < testSuiteElements.getLength(); i++) {
		        	Node importedNode = mergedDocument.importNode(testSuiteElements.item(i), true);
		        	testSuitesElement.appendChild(importedNode);	
		        }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	     // Use a Transformer for output
        TransformerFactory tFactory =
        TransformerFactory.newInstance();
        Transformer transformer;
		try {
			transformer = tFactory.newTransformer();
			
			DOMSource source = new DOMSource(mergedDocument);
	        StreamResult result = new StreamResult(mergedFileName);
	        transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
		
//	private String removeLine (String file, ArrayList<String> remove, boolean saveToFile) throws IOException {
//	    BufferedReader reader = new BufferedReader( new FileReader (file));
//	    String line = null;
//	    StringBuilder stringBuilder = new StringBuilder();
//	    String ls = System.getProperty("line.separator");
//
//	    while( ( line = reader.readLine() ) != null ) {
//	        boolean lineRemoved = false; 
//	    	
//	    	for(String lineToRemove : remove) {
//	        	if(line.contains(lineToRemove)) {
//	        		lineRemoved = true;
//	        	}
//	        }
//	    	
//	    	if (lineRemoved) {
//	    		continue;
//	    	}
//	    	
//	    	stringBuilder.append( line );
//	        stringBuilder.append( ls );
//	    }
//	    
//	    reader.close();
//	    
//	    String fileContent = stringBuilder.toString();
//	    
//	    if (saveToFile) {
//	    	stringToXMLFile(fileContent, file);
//        }
//	    
//	    return fileContent;
//	}
	
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
	
	private ArrayList<IPath> getXmlResults(final IProgressMonitor monitor, IFolder folder) {
		ArrayList<IPath> jUnitResources = new ArrayList<IPath>();
		if (folder.exists()) {
			try {
				folder.refreshLocal(1, monitor);
				IResource[] members;
				members = folder.members();
				for (IResource iResource : members) {
					if (iResource.getFullPath().toPortableString().contains("_result.xml")) {
						jUnitResources.add(iResource.getLocation());
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return jUnitResources;
	}
	
	public static String getLaunchDir(IProject project) {
		return project.getLocation().toPortableString();
	}

	public static void setLaunchAttributes(ILaunchConfigurationWorkingCopy configuration, IResource context) throws CoreException {
		configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION,
				"/usr/local/bin/tishadow");
		String project = null;
		if (context != null) {
			project = LaunchTiShadowTests.getLaunchDir(context.getProject());
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, project);
		configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "spec -x -p 8181");
		configuration.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
		
		// Get current value for PATH environment variable
		String pathVariable = System.getenv("PATH");
		pathVariable += ":/usr/local/bin";
		
		Map<String, String> envVariables = new HashMap<String, String>();
		envVariables.put("PATH", pathVariable);
		configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envVariables);
	}

}

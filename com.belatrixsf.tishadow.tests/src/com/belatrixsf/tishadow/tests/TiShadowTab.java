package com.belatrixsf.tishadow.tests;

import java.awt.Event;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsLaunchConfigurationMessages;
import org.eclipse.ui.externaltools.internal.program.launchConfigurations.ProgramMainTab;

final class TiShadowTab extends ProgramMainTab {
	
	private Map<Object,Button> checkBoxMap = new HashMap<Object, Button>();
	private Map<Object,Text> textBoxMap = new HashMap<Object, Text>();
	private Group group = null;
	private GridLayout layout = null;
	private GridData gridData = null;
	private ArgsBuilder argsBuilder = new ArgsBuilder();
	private String[] arguments = new String[17];
	private String argumentsString;
	protected boolean fInitializing = false;
	
	public TiShadowTab() {
		arguments = argsBuilder.getDefaults();
		argumentsString = argumentsToString(arguments);
	}
		
	@SuppressWarnings("restriction")
	@Override
	protected void createArgumentComponent(Composite parent) {
		
		group = new Group(parent, SWT.NONE);
		String groupName = "Options";
		group.setText(groupName); 
		layout = new GridLayout();
		layout.numColumns = 4;
		gridData = new GridData(GridData.FILL_BOTH);
		group.setLayout(layout);
		group.setLayoutData(gridData);
        group.setFont(parent.getFont());
		
        //Create a checkbox indicating: the short version of the argument, the long version,
        //and whether the argument requires to enter text or not.
        createCheckbox("-u", "update", false, "Only send recently changed files");
        createCheckbox("-l", "locale", true, "Set the locale in in the TiShadow app");
        createCheckbox("-o", "host", true, "Server host name / ip address");
        createCheckbox("-p", "port", true, "Server port");
        createCheckbox("-r", "room", true, "Server room");
        createCheckbox("-t", "type", true, "Testing library");
        createCheckbox("-c", "ticaster", false, "Connect to ticaster");
        createCheckbox("-j", "jshint", false, "Analyse code with JSHint");
        createCheckbox("-x", "junit-xml", false, "Output report as JUnit XML");
        createCheckbox("-P", "platform", true, "Target platform");
        createCheckbox("-s", "skip-alloy-compile", false, "Skip automatic alloy compilation");
        createCheckbox("-D", "include-dot-files", false, "Includes dot files in the bundle (defaults to false)");
        
		super.createArgumentComponent(parent);
		argumentVariablesButton.setVisible(false);
		
		argumentField.setEditable(false);
		argumentField.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent event) {
				if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.MODIFIER_MASK) != 0) {
					event.doit= true;
				}
			}
		});
		
	}
	
	private void handleVariablesButtonSelected(Text textField) {
		String variable = getVariable();
		if (variable != null) {
			textField.insert(variable);
		}
	}
	
	private String getVariable() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}
	
	public String getArguments(){
		return argumentsString;
	}
	
	private String argumentsToString(String[] args){
		String returnValue = "spec ";
		String[] argumentsNames = {"-u","-l","","-o","","-p","","-r","","-t","","-c","-j","-x","-P","","-s","-D"};
		
		for (int i=0; i<18; i++){
			if(args[i] == "true"){
				if(i==0 | i==11 | i==12 | i==13 | i==16 | i==17){
					returnValue = returnValue + argumentsNames[i] + " ";
				}else if(i==1 | i==3 | i==5 | i==7 | i==9 | i==14){
					returnValue = returnValue + argumentsNames[i] + args[i+1]+" ";
				}
			}
		}
		return returnValue;
	}
	
	@Override
	protected void updateArgument(ILaunchConfiguration configuration) {
		String[] argumentsNames = {"-u","-l","-o","-p","-r","-t","-c","-j","-x","-P","-s","-D"};
		super.updateArgument(configuration);
		if(!argumentField.getText().substring(argumentField.getText().length() - 1).equals(" ")){
			argumentField.setText(argumentField.getText()+" ");
		}
				
		for(int i = 0; i < 12; i++){
			if (argumentField.getText().contains(argumentsNames[i])){
				checkBoxMap.get(argumentsNames[i]).setSelection(true);
				if(textBoxMap.get(argumentsNames[i]+"_textBox") != null){
					textBoxMap.get(argumentsNames[i]+"_textBox").setEnabled(true);
					//textBoxMap.get(argumentsNames[i]+"_textBox").setText(argumentField.getText().substring(argumentField.getText().indexOf(argumentsNames[i])+3, findParameterTextIndex(argumentsNames[i], argumentField.getText())));
					String s = argumentField.getText(); //Since we are modifying the text on the parameters, and these have listeners to modify the argumentField, we save the state of the argumentField
					textBoxMap.get(argumentsNames[i]+"_textBox").setText(findParameterTextIndex(argumentsNames[i]));
					argumentField.setText(s); //We set to the argumentField the saved value.
				}
			}else{
				checkBoxMap.get(argumentsNames[i]).setSelection(false);
				if(textBoxMap.get(argumentsNames[i]+"_textBox") != null){
					textBoxMap.get(argumentsNames[i]+"_textBox").setEnabled(false);
					String s = argumentField.getText(); //Since we are modifying the text on the parameters, and these have listeners to modify the argumentField, we save the state of the argumentField
					textBoxMap.get(argumentsNames[i]+"_textBox").setText("");
					argumentField.setText(s); //We set to the argumentField the saved value.
				}
			}
		}
	}
	
	private void createCheckbox(final String parameter, String name, final boolean hasText, String toolTipText){
		final Button argCheckbox = new Button(group, SWT.CHECK);
		checkBoxMap.put(parameter, argCheckbox);
		final Text argTextBox;
		
		argCheckbox.setLayoutData(gridData);
		
		if(hasText){
			argTextBox = new Text(group, SWT.MULTI | SWT.BORDER);
			textBoxMap.put(parameter+"_textBox",argTextBox);
			argTextBox.setLayoutData(gridData);
			argCheckbox.setText(parameter +", --"+ name +" "+ "<"+name+">");
		}else{
			argTextBox = null;
			argCheckbox.setText(parameter +", --"+ name);
		}
		
		argCheckbox.setToolTipText(toolTipText);
		
		argCheckbox.addSelectionListener(
                new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent theEvent)
                    {	
                    	if(hasText){
	                    	if(argCheckbox.getSelection()){
	                    		argumentField.setText(argumentField.getText() + parameter + " " + argTextBox.getText() + " ");
	                    		argTextBox.setEnabled(true);
	                    		argumentsString =argumentField.getText();
	                    	}else{
	                    		String s = argumentField.getText();
	                    		s = s.replace(parameter + " " + argTextBox.getText() + " ", "");
	                    		argTextBox.setEnabled(false);
	                    		argumentField.setText(s);
	                    		argumentsString =argumentField.getText();
	                    	}
                    	}else{
	                    	if(argCheckbox.getSelection()){
	                    			argumentField.setText(argumentField.getText() + parameter + " ");
	                    			argumentsString =argumentField.getText();
	                    	}else{
	                    		String s = argumentField.getText();
	                    		s = s.replace(parameter + " ", "");
	                    		argumentField.setText(s);
	                    		argumentsString =argumentField.getText();
	                    	}
                    	}
                    }
                });
		
       	if(hasText){	
	        if(argCheckbox.getSelection()){
	        	argTextBox.setEnabled(true);
	    	}else{
	    		argTextBox.setEnabled(false);
	    	}
			
	        //Forbid the space character
	        argTextBox.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(final VerifyEvent event) {
					if (event.keyCode == SWT.SPACE) {
						event.doit = false; // disallow the action
					}
				}
			});
	        
	        argTextBox.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						// TODO Auto-generated method stub
						String s = argumentField.getText();
						s = s.replaceFirst(parameter+" .*? ", parameter + " " + argTextBox.getText() + " ");
						argumentField.setText(s);
						argumentsString =argumentField.getText();
					}
	            });
       	}else{
       		new Label(group, SWT.NONE);
       	}
	}
	
	private String findParameterTextIndex(String parameter){
		String s1 = argumentField.getText();
		String s2 = s1.split(parameter)[1];
		return s2.split(" ")[1];
	}
	
}
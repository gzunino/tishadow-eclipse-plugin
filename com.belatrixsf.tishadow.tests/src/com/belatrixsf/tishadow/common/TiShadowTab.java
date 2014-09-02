package com.belatrixsf.tishadow.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.externaltools.internal.program.launchConfigurations.ProgramMainTab;

@SuppressWarnings("restriction")
public abstract class TiShadowTab extends ProgramMainTab {
	
	protected ArrayList<Argument> argumentsList = new ArrayList<Argument>();
	private Map<Object,Button> checkBoxMap = new HashMap<Object, Button>();
	private Map<Object,Text> textBoxMap = new HashMap<Object, Text>();
	private Group group = null;
	private GridLayout layout = null;
	private GridData gridData = null;
	protected String argumentsString;
	protected boolean fInitializing = false;
	private static final String AUTOMATIC_UPDATES_COMMAND="@";
	
	protected abstract ArrayList<Argument> getTabOptions();
		
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
        for(int i=0; i<argumentsList.size(); i++){
        	createCheckbox(argumentsList.get(i));
        }
        
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
	
	public String getArguments(){
		return argumentsString;
	}
	
	protected String argumentsToString(String command, ArrayList<Argument> args){
		String returnValue = command+" ";
		
		for (int i=0; i<args.size(); i++){
			if (args.get(i).isDefault()){
				returnValue = returnValue + args.get(i).getArgumentShort() + " ";
				if (args.get(i).getHasText()){
					returnValue = returnValue + args.get(i).getText() + " ";
				}
			}
		}
		
		return returnValue;
	}
	
	@Override
	protected void updateArgument(ILaunchConfiguration configuration) {
		
		String [] argumentsNames = new String[argumentsList.size()];
		for(int i=0; i<argumentsList.size(); i++){
			argumentsNames[i] = argumentsList.get(i).getArgumentShort();
		}
		
		super.updateArgument(configuration);
		if(!argumentField.getText().substring(argumentField.getText().length() - 1).equals(" ")){
			argumentField.setText(argumentField.getText()+" ");
		}
				
		for(int i = 0; i < argumentsList.size(); i++){
			if (argumentField.getText().contains(" "+argumentsNames[i])){
				checkBoxMap.get(argumentsNames[i]).setSelection(true);
				if(textBoxMap.get(argumentsNames[i]+"_textBox") != null){
					textBoxMap.get(argumentsNames[i]+"_textBox").setEnabled(true);
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
	
	private void createCheckbox(Argument arg){
		
		final String parameter = arg.getArgumentShort();
		String name = arg.getArgumentName();
		String toolTipText = arg.getToolTipText();
		final boolean hasText = arg.getHasText();
		
		final Button argCheckbox = new Button(group, SWT.CHECK);
		checkBoxMap.put(parameter, argCheckbox);
		final Text argTextBox;
		
		argCheckbox.setLayoutData(gridData);
		
		if(hasText){
			argTextBox = new Text(group, SWT.SINGLE | SWT.BORDER);
			textBoxMap.put(parameter+"_textBox",argTextBox);
			argTextBox.setLayoutData(gridData);
			argCheckbox.setText(parameter +", --"+ name);
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
                    	checkboxClicked(parameter, hasText, argCheckbox,
								argTextBox);
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
					if (event.keyCode == SWT.SPACE || event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
						event.doit = false; // Disallow the space key, the main enter key and the keypad enter key.
					}
				}
			});
	        
	        argTextBox.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
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
		try{
			String s2 = s1.split(" "+parameter)[1];
			String s3 = s2.split(" ")[1];
			return s3.split(" ")[0];
		}catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	protected void checkboxClicked(final String parameter,
			final boolean hasText, final Button argCheckbox,
			final Text argTextBox) {
		if (isAutomaticUpdate(parameter)) {
			if (argCheckbox.getSelection()) {
				argumentsString = parameter + " " + argumentsString;
				argumentField
						.setText(argumentsString);
				argumentsString = argumentField.getText();
			} else {
				String s = argumentField.getText();
				s = s.replace(parameter + " ", "");
				argumentField.setText(s);
				argumentsString = argumentField.getText();
			}
		} else if (hasText) {
			if (argCheckbox.getSelection()) {
				argumentField.setText(argumentField.getText() + parameter + " "
						+ argTextBox.getText() + " ");
				argTextBox.setEnabled(true);
				argumentsString = argumentField.getText();
			} else {
				String s = argumentField.getText();
				s = s.replace(parameter + " " + argTextBox.getText() + " ", "");
				argTextBox.setEnabled(false);
				argumentField.setText(s);
				argumentsString = argumentField.getText();
			}
		} else {
			if (argCheckbox.getSelection()) {
				argumentField
						.setText(argumentField.getText() + parameter + " ");
				argumentsString = argumentField.getText();
			} else {
				String s = argumentField.getText();
				s = s.replace(parameter + " ", "");
				argumentField.setText(s);
				argumentsString = argumentField.getText();
			}
		}
	}

	private boolean isAutomaticUpdate(String parameter) {
		return parameter.equals(AUTOMATIC_UPDATES_COMMAND);
	}
	
	
	
}
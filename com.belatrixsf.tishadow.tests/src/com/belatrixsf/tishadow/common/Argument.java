package com.belatrixsf.tishadow.common;

public class Argument {

	private String argumentShort; //e.g.: "-p"
	private String argumentName; //e.g.: "port"
	private String toolTipText; //e.g.: "Server port"
	private boolean isDefault;
	private boolean hasText;
	private String text;//e.g.: "3100"
	
	public Argument(String argumentShort, String argumentName,String toolTipText, boolean isDefault, boolean hasText, String text){
		this.setArgumentShort(argumentShort);
		this.setArgumentName(argumentName);
		this.setToolTipText(toolTipText);
		this.setDefault(isDefault);
		this.setHasText(hasText);
		this.setText(text);
	}
	
	public Argument(String argumentShort, String argumentName,String toolTipText, boolean isDefault, boolean hasText){
		this.setArgumentShort(argumentShort);
		this.setArgumentName(argumentName);
		this.setToolTipText(toolTipText);
		this.setDefault(isDefault);
		this.setHasText(hasText);
		this.setText("");
	}

	public String getArgumentShort() {
		return argumentShort;
	}

	public void setArgumentShort(String argumentShort) {
		this.argumentShort = argumentShort;
	}

	public String getArgumentName() {
		return argumentName;
	}

	public void setArgumentName(String argumentName) {
		this.argumentName = argumentName;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public boolean getHasText() {
		return hasText;
	}

	public void setHasText(boolean hasText) {
		this.hasText = hasText;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}

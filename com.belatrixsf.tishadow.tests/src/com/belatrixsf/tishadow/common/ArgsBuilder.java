package com.belatrixsf.tishadow.common;

import java.util.ArrayList;

public class ArgsBuilder {

	//Server Parameters
	private Argument serverPort = new Argument("-p", "port", "Server port",false,true);
	private Argument serverLongPolling = new Argument("-l", "long-polling", "Force long polling",false,false);
	private Argument serverInternalIp = new Argument("-i", "internal-ip", "Internal IP to bind to",false,true);
	private Argument serverScreenshotPath = new Argument("-s", "screenshot-path", "Path to save screenshot (defaults to /tmp)",false,true);
	private Argument serverManageVersions = new Argument("-z", "manage-versions", "Manage and automatically update bundles",false,false);
	
	//Spec Parameters
	private Argument update = new Argument("-u", "update", "Only send recently changed files",true,false); 
	private Argument locale = new Argument("-l", "locale", "Set the locale in in the TiShadow app", false,true);
	private Argument host = new Argument("-o", "host", "Server host name / ip address", false,true);
	private Argument port = new Argument("-p", "port", "Server port", false,true);
	private Argument room = new Argument("-r", "room", "Server room", false,true);
	private Argument type = new Argument("-t", "type", "Testing library", false,true);
	private Argument ticaster = new Argument("-c", "ticaster", "Connect to ticaster", false,false);
	private Argument jshint = new Argument("-j", "jshint", "Analyse code with JSHint", false,false);
	private Argument junitXml = new Argument("-x", "junit-xml", "Output report as JUnit XML", true,false);
	private Argument platform = new Argument("-P", "platform", "Target platform", false,true);
	private Argument skipAlloyCompile = new Argument("-s", "skip-alloy-compile", "Skip automatic alloy compilation", false,false);
	private Argument includeDotFiles = new Argument("-D", "include-dot-files", "Includes dot files in the bundle (defaults to false)", false, false);
	private Argument target = new Argument("-T", "target", "target TiShadow app (defaults to name on tiapp.xml or moduleid on manifest)", false, true);
	private Argument clearSpecFiles = new Argument("-C", "clear-spec-files", "Clears only the spec files from the cache", false, false);
	private Argument coverage = new Argument("-c", "coverage", "Runs code coverage, for available report_types see https://github.com/gotwarlost/istanbul#the-report-command", false, true);
	
	
public ArrayList<Argument> getServerDefaults(){
		
		ArrayList<Argument> argumentsList = new ArrayList<Argument>();
		
		argumentsList.add(serverPort);
		argumentsList.add(serverLongPolling);
		argumentsList.add(serverInternalIp);
		argumentsList.add(serverScreenshotPath);
		argumentsList.add(serverManageVersions);
		
		return argumentsList;
	}
	
	public ArrayList<Argument> getSpecDefaults(){
		
		ArrayList<Argument> argumentsList = new ArrayList<Argument>();
		
		argumentsList.add(update);
		argumentsList.add(locale);
		argumentsList.add(host);
		argumentsList.add(port);
		argumentsList.add(room);
		argumentsList.add(type);
		argumentsList.add(ticaster);
		argumentsList.add(jshint);
		argumentsList.add(junitXml);
		argumentsList.add(platform);
		argumentsList.add(skipAlloyCompile);
		argumentsList.add(includeDotFiles);
		argumentsList.add(target);
		argumentsList.add(clearSpecFiles);
		argumentsList.add(coverage);
		
		return argumentsList;
	}
	
	public String getServerDefaultsString(){
		
		ArrayList<Argument> argumentsList = new ArrayList<Argument>();
		
		argumentsList.add(serverPort);
		argumentsList.add(serverLongPolling);
		argumentsList.add(serverInternalIp);
		argumentsList.add(serverScreenshotPath);
		argumentsList.add(serverManageVersions);
		
		return argumentsToString("server", argumentsList);
		
	}
	
	public String getSpecDefaultsString(){
		
		ArrayList<Argument> argumentsList = new ArrayList<Argument>();
		
		argumentsList.add(update);
		argumentsList.add(locale);
		argumentsList.add(host);
		argumentsList.add(port);
		argumentsList.add(room);
		argumentsList.add(type);
		argumentsList.add(ticaster);
		argumentsList.add(jshint);
		argumentsList.add(junitXml);
		argumentsList.add(platform);
		argumentsList.add(skipAlloyCompile);
		argumentsList.add(includeDotFiles);
		argumentsList.add(target);
		argumentsList.add(clearSpecFiles);
		argumentsList.add(coverage);
		
		return argumentsToString("spec", argumentsList);
	}
	
	private String argumentsToString(String command, ArrayList<Argument> args){
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
	
}

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
	private Argument specUpdate = new Argument("-u", "update", "Only send recently changed files",true,false); 
	private Argument specLocale = new Argument("-l", "locale", "Set the locale in in the TiShadow app", false,true);
	private Argument specHost = new Argument("-o", "host", "Server host name / ip address", false,true);
	private Argument specPort = new Argument("-p", "port", "Server port", false,true);
	private Argument specRoom = new Argument("-r", "room", "Server room", false,true);
	private Argument specType = new Argument("-t", "type", "Testing library", false,true);
	private Argument specTicaster = new Argument("-c", "ticaster", "Connect to ticaster", false,false);
	private Argument specJshint = new Argument("-j", "jshint", "Analyse code with JSHint", false,false);
	private Argument specJunitXml = new Argument("-x", "junit-xml", "Output report as JUnit XML", true,false);
	private Argument specPlatform = new Argument("-P", "platform", "Target platform", false,true);
	private Argument specSkipAlloyCompile = new Argument("-s", "skip-alloy-compile", "Skip automatic alloy compilation", false,false);
	private Argument specIncludeDotFiles = new Argument("-D", "include-dot-files", "Includes dot files in the bundle (defaults to false)", false, false);
	private Argument specTarget = new Argument("-T", "target", "target TiShadow app (defaults to name on tiapp.xml or moduleid on manifest)", false, true);
	private Argument specClearSpecFiles = new Argument("-C", "clear-spec-files", "Clears only the spec files from the cache", false, false);
	private Argument specCoverage = new Argument("-c", "coverage", "Runs code coverage, for available report_types see https://github.com/gotwarlost/istanbul#the-report-command", false, true);
	
	//Run Parameters
	private Argument runUpdate = new Argument("-u", "update", "Only send recently changed files",true,false);
	private Argument runPatch = new Argument("-a", "patch", "Patch updated files without causing app restart",false,false);
	private Argument runLocale = new Argument("-l", "locale", "Set the locale in in the TiShadow app", false,true);
	private Argument runJshint = new Argument("-j", "jshint", "Analyse code with JSHint", false,false);
	private Argument runTail = new Argument("-t", "tail-logs", "Tail server logs on deploy", false,false);
	private Argument runHost = new Argument("-o", "host", "Server host name / ip address", false,true);
	private Argument runPort = new Argument("-p", "port", "Server port", false,true);
	private Argument runRoom = new Argument("-r", "room", "Server room", false,true);
	private Argument runSkipAlloyCompile = new Argument("-s", "Skip-alloy-compile", "Skip automatic alloy compilation", false,true);
	private Argument runPlatform = new Argument("-P", "platform", "Target platform", false,true);
	private Argument runIncludeDotFiles = new Argument("-D", "include-dot-files", "Includes dot files in the bundle (defaults to false)", false, false);
	private Argument runTarget = new Argument("-T", "target", "target TiShadow app (defaults to name on tiapp.xml or moduleid on manifest)", false, true);
	private Argument runAutomaticUpdates = new Argument("@", "automatic updates", "when any files in the Resources directory are modified the run command will executed", false, false);	
	
	public ArrayList<Argument> getServerDefaults(){
		
		ArrayList<Argument> argumentsList = new ArrayList<Argument>();
		
		argumentsList.add(serverPort);
		argumentsList.add(serverLongPolling);
		argumentsList.add(serverInternalIp);
		argumentsList.add(serverScreenshotPath);
		argumentsList.add(serverManageVersions);
		
		return argumentsList;
	}
	
	public String getServerDefaultsString(){
		
		ArrayList<Argument> argumentsList = getServerDefaults();
		
		return argumentsToString("server", argumentsList);
		
	}
	
	public ArrayList<Argument> getSpecDefaults(){
		
		ArrayList<Argument> argumentsList = new ArrayList<Argument>();
		
		argumentsList.add(specUpdate);
		argumentsList.add(specLocale);
		argumentsList.add(specHost);
		argumentsList.add(specPort);
		argumentsList.add(specRoom);
		argumentsList.add(specType);
		argumentsList.add(specTicaster);
		argumentsList.add(specJshint);
		argumentsList.add(specJunitXml);
		argumentsList.add(specPlatform);
		argumentsList.add(specSkipAlloyCompile);
		argumentsList.add(specIncludeDotFiles);
		argumentsList.add(specTarget);
		argumentsList.add(specClearSpecFiles);
		argumentsList.add(specCoverage);
		
		return argumentsList;
	}
	
	
	public String getSpecDefaultsString(){
		
		ArrayList<Argument> argumentsList = getSpecDefaults();
		
		return argumentsToString("spec", argumentsList);
	}
	
	public ArrayList<Argument> getRunDefaults(){
		
		ArrayList<Argument> argumentsList = new ArrayList<Argument>();
		
		argumentsList.add(runUpdate);
		argumentsList.add(runAutomaticUpdates);
		argumentsList.add(runPatch);
		argumentsList.add(runLocale);
		argumentsList.add(runJshint);
		argumentsList.add(runTail);
		argumentsList.add(runHost);
		argumentsList.add(runPort);
		argumentsList.add(runRoom);
		argumentsList.add(runSkipAlloyCompile);
		argumentsList.add(runPlatform);
		argumentsList.add(runIncludeDotFiles);
		argumentsList.add(runTarget);
		
		return argumentsList;
	}
	
	public String getRunDefaultsString(){
		
		ArrayList<Argument> argumentsList = getRunDefaults();
		
		return argumentsToString("run", argumentsList);
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

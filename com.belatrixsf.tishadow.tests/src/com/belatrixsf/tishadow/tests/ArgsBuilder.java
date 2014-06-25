package com.belatrixsf.tishadow.tests;

public class ArgsBuilder {

	private String[] arguments = new String[18];
	private boolean update;
	private boolean locale;
	private String localeString;
	private boolean host;
	private String hostName;
	private boolean port;
	private String portNumber;
	private boolean room;
	private String roomString;
	private boolean type;
	private String typeString;
	private boolean ticaster;
	private boolean jshint;
	private boolean junitXml;
	private boolean platform;
	private String platformString;
	private boolean skipAlloyCompile;
	private boolean includeDotFiles;
	
	public ArgsBuilder(){
		update = true;
		locale = false;
		localeString = "";
		host = false;
		hostName = "";
		port = false;
		portNumber = "";
		room = false;
		roomString = "";
		type = false;
		typeString = "";
		ticaster = false;
		jshint = false;
		junitXml = true;
		platform = false;
		platformString = "";
		skipAlloyCompile = false;
		includeDotFiles = false;
		
		arguments[0] = String.valueOf(update);
		arguments[1] = String.valueOf(locale);
		arguments[2] = localeString;
		arguments[3] = String.valueOf(host);
		arguments[4] = hostName;
		arguments[5] = String.valueOf(port);
		arguments[6] = portNumber;
		arguments[7] = String.valueOf(room);
		arguments[8] = roomString;
		arguments[9] = String.valueOf(type);
		arguments[10] = typeString;
		arguments[11] = String.valueOf(ticaster);
		arguments[12] = String.valueOf(jshint);
		arguments[13] = String.valueOf(junitXml);
		arguments[14] = String.valueOf(platform);
		arguments[15] = platformString;
		arguments[16] = String.valueOf(skipAlloyCompile);
		arguments[17] = String.valueOf(includeDotFiles);
	}
		
	public String[] getDefaults(){
		return arguments;
	}
	
}

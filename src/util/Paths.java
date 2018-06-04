package util;

import java.io.File;

import service.Context;

public class Paths {
	 private static Paths uniqueInstance;
	  public static Paths getInstance(){
		  	if(uniqueInstance==null)
		  		uniqueInstance=new Paths();
		  	return uniqueInstance;
	  }
	  
	  private Paths(){
		  this.basePath=FileUtils.formatPath(Context.StartPath)+File.separator;
		  this.init();
	  } 
	  
	  private void init(){
		  this.logPath=this.basePath+"log"+File.separator;
		  this.configPath=this.basePath+"config"+File.separator;
		  File logDir=new File(this.logPath);
		  if(!logDir.exists())
			  logDir.mkdirs();
		  File cfgDir=new File(this.configPath);
		  if(!cfgDir.exists())
			  cfgDir.mkdirs();
	  }
	  
	  private String basePath;
	  private String logPath;
	  private String configPath;
	  
	public String getBasePath() {
		return basePath;
	}

	public String getLogPath() {
		return logPath;
	}
	
	public String getConfigPath() {
		return configPath;
	}
	  
}

package service;

import java.io.File;

import util.Paths;


public class Context {
       public static String StartPath="";
       //·þÎñÂÖÑ¯ÆµÂÊ
       public static int sleepSecond=10;
       public static boolean isRunable(){
		  	 String lockFile=Paths.getInstance().getConfigPath()+"lock.ini";
		  	 File file=new File(lockFile);
		  	 if(file.exists()){
		  		 return false;
		  	 }else{
		  		 return true;
		  	 }
       }
}

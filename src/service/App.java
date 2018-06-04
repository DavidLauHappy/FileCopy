package service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import util.DateUtil;
import util.FileUtils;
import util.FileUtils.FileOperatorType;
import util.Logger;
import util.Paths;



public class App {
	
private  static App app=null;
	
	public static void main(String[] args){
		if("start".equalsIgnoreCase(args[0])){
			App.getInstance().start();
		}else if("stop".equalsIgnoreCase(args[0])){
			App.getInstance().stop();
			System.exit(0);
		}
	}
	
	public  void start(){
		//初始化路径
		App.getInstance().setStartPath();
		//加载配置
		App.getInstance().loadConfig();
		//启动运行删除锁文件
		App.getInstance().unlock();
		//启动运行
		App.getInstance().run();
	}
	
	private void stop(){
		App.getInstance().setStartPath();
		App.getInstance().lock();
	}
	
	private void loadConfig(){
		String cfgPath=Context.StartPath+File.separator+"config"+File.separator+"start.ini";
		cfgPath=FileUtils.formatPath(cfgPath);
		List<String>  lines=FileUtils.getFileLineList(cfgPath);
		if(lines!=null&&lines.size()>0){
			String[] vars=null;
			for(String line:lines){
				if(!line.startsWith("#")){
					vars=line.split("\\=");
					if(vars!=null){
						parameters.put(vars[0], vars[1]);
					}
				}
			}
		}
	}
	
	public Map<String,String> parameters=new HashMap<String, String>();
	private void run(){
			String cfgStartTime=parameters.get("startTime");  
			String source=parameters.get("sourcePath");  
			String target=parameters.get("targetPath");  
			String currentDate=DateUtil.getCurrentDate("yyyyMMdd");
			while(Context.isRunable()){
				   String currTime=DateUtil.getCurrentDate("HH:mm:ss");
				   cfgStartTime=parameters.get("startTime");  
				   if(cfgStartTime!=null&&cfgStartTime.length()>0){
					   //判断当前时间是否大于服务启动时间
					    currTime=currTime.replace(":", "");
					    cfgStartTime=cfgStartTime.replace(":", "");
					    currentDate=DateUtil.getCurrentDate("yyyyMMdd");
						if(currTime.compareTo(cfgStartTime)>0&&
							currentDate.compareTo(workDate)==0){
							source=parameters.get("sourcePath");  
							target=parameters.get("targetPath");  
							Logger.log("开始启动文档同步服务："+source+"--->"+target);
							 if((source!=null&&source.length()>0)&&
								(target!=null&&target.length()>0)){
								 filesCopy(source,target);
								 Logger.log("文档同步服务完成！");
								 workDate=getNextDate(currentDate);
							 }
						}
				   }else{
					   try {
							TimeUnit.SECONDS.sleep(Context.sleepSecond);
						} catch (InterruptedException e) {
							 Logger.log("文档同步服务睡眠等待发生异常……");
						}
				   }
			   }
			 Logger.log("文档同步服务收到外部退出信号，自动终止服务完成……");
			
	}
	
	
	private void filesCopy(String sourceDir,String targetDir){
		List<File> files=new ArrayList<File>();
		FileUtils.getFileList(files, sourceDir);
		if(files!=null&&files.size()>0){
			 for(File file:files){
				 if(Context.isRunable()){
					 String dirPath=file.getParentFile().getAbsolutePath();
					 dirPath=dirPath.replace(sourceDir, targetDir);
					 File dir=new File(dirPath);
					 if(!dir.exists())
						 dir.mkdirs();
					 String scrPath=file.getAbsolutePath();
					 String tgtPath=scrPath.replace(sourceDir, targetDir);
					 File bFile=new File(tgtPath);
					 if(!bFile.exists()){
						 FileUtils.moveOrCopy(scrPath, dirPath, FileOperatorType.Copy);
						 Logger.log("新增的文档同步：("+scrPath+") -> ("+dirPath+")处理完成");
					 }else{
						 String bMd5=FileUtils.getMd5ByPath(tgtPath);
						 String md5=FileUtils.getMd5ByPath(scrPath);
						 if(!md5.equals(bMd5)){
							 FileUtils.moveOrCopy(scrPath, dirPath, FileOperatorType.Copy);
							 Logger.log("修改的文档同步：("+scrPath+") ->  ("+dirPath+")处理完成");
						 }else{
							 Logger.log("文档：("+scrPath+")未发生变化，跳过同步，处理完成");
						 }
					 }
				 }else{
					 break;
				 }
			 }
		}
	}
	
	
	
	private void unlock(){
		try{
			String lockFile=Paths.getInstance().getConfigPath()+"lock.ini";
		    File file=new File(lockFile);
		    if(file.exists()){
		    	file.delete();
		    }
		    Logger.log("FileCopy服务启动前解除外部退出指令完成……");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void lock(){
		try{
			String lockFile=Paths.getInstance().getConfigPath()+"lock.ini";
			String currentTime=DateUtil.getCurrentDate("yyyyMMdd-HH:mm:ss.SSS");
		   File file=new File(lockFile);
		   FileWriter fw = new FileWriter(file,true);
		   BufferedWriter bw = new BufferedWriter(fw);
		   bw.write(currentTime);
		   bw.write("\r\n");
		   bw.close();
		   fw.close();
		   Logger.log("syncDbService发起外部退出指令完成……");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String getNextDate(String date){
		DateFormat format = new SimpleDateFormat("yyyyMMdd");      
		Calendar   calendar=Calendar.getInstance();    
		Date dateObj=null;
		String nextDate=date;
		try {
			dateObj = format.parse(date);
			calendar.setTime(dateObj);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			nextDate= format.format(calendar.getTime());
		}catch (ParseException e) {
			e.printStackTrace();
		} 
		return nextDate;
	}
	
	private  void setStartPath(){
		 String path="";
		 try {
				path= System.getProperty("user.dir");//这种不支持中文路径吧
				String pathClass= URLDecoder.decode(this.getClass().getClassLoader().getResource("service/App.class").getPath(),"UTF-8");
				Context.StartPath=path;
			} catch (Exception e) {
				e.printStackTrace();
			}
	 }
	
	public static App getInstance(){
		if(app==null)
			app=new App();
		return app;
	}
	
	private App(){
		workDate=DateUtil.getCurrentDate("yyyyMMdd");
	}
	public String workDate="";
}

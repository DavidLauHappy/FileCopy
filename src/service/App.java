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
		//��ʼ��·��
		App.getInstance().setStartPath();
		//��������
		App.getInstance().loadConfig();
		//��������ɾ�����ļ�
		App.getInstance().unlock();
		//��������
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
					   //�жϵ�ǰʱ���Ƿ���ڷ�������ʱ��
					    currTime=currTime.replace(":", "");
					    cfgStartTime=cfgStartTime.replace(":", "");
					    currentDate=DateUtil.getCurrentDate("yyyyMMdd");
						if(currTime.compareTo(cfgStartTime)>0&&
							currentDate.compareTo(workDate)==0){
							source=parameters.get("sourcePath");  
							target=parameters.get("targetPath");  
							Logger.log("��ʼ�����ĵ�ͬ������"+source+"--->"+target);
							 if((source!=null&&source.length()>0)&&
								(target!=null&&target.length()>0)){
								 filesCopy(source,target);
								 Logger.log("�ĵ�ͬ��������ɣ�");
								 workDate=getNextDate(currentDate);
							 }
						}
				   }else{
					   try {
							TimeUnit.SECONDS.sleep(Context.sleepSecond);
						} catch (InterruptedException e) {
							 Logger.log("�ĵ�ͬ������˯�ߵȴ������쳣����");
						}
				   }
			   }
			 Logger.log("�ĵ�ͬ�������յ��ⲿ�˳��źţ��Զ���ֹ������ɡ���");
			
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
						 Logger.log("�������ĵ�ͬ����("+scrPath+") -> ("+dirPath+")�������");
					 }else{
						 String bMd5=FileUtils.getMd5ByPath(tgtPath);
						 String md5=FileUtils.getMd5ByPath(scrPath);
						 if(!md5.equals(bMd5)){
							 FileUtils.moveOrCopy(scrPath, dirPath, FileOperatorType.Copy);
							 Logger.log("�޸ĵ��ĵ�ͬ����("+scrPath+") ->  ("+dirPath+")�������");
						 }else{
							 Logger.log("�ĵ���("+scrPath+")δ�����仯������ͬ�����������");
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
		    Logger.log("FileCopy��������ǰ����ⲿ�˳�ָ����ɡ���");
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
		   Logger.log("syncDbService�����ⲿ�˳�ָ����ɡ���");
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
				path= System.getProperty("user.dir");//���ֲ�֧������·����
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

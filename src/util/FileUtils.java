package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;


public class FileUtils {
	
	 public synchronized static List<String> getFileLineList(String path){
		  List<String> result=new ArrayList<String>();
		  try {
				File file=new File(path);
					FileReader fr = new FileReader(file);
					BufferedReader br = new BufferedReader(fr);
					String line = "";
					while ((line = br.readLine()) != null) {
						result.add(line);
					}
					fr.close();
					br.close();
				} catch (Exception e){		
				   Logger.log("��ȡ�ļ�"+path+"�쳣:"+e.toString());
				}
		return result;
	  }
	 
	 public static String formatPath(String path){
		  if (path != null && !"".equals(path)) {
				path = path.replace('/', File.separatorChar);
				path = path.replace('\\', File.separatorChar);
				while (path.endsWith(String.valueOf(File.separatorChar))) {
					path = path.substring(0, path.length() - 1);
				}
			} else {
				return "";
			}
			return path;
		}
	 
	 
	  /*�ݹ��ȡĳ��Ŀ¼�µ������ļ�(����Ŀ¼)���嵥��֧�ֵݹ�*/
	  public static void getFileList(List<File> result,String startPath){
		  if(result!=null){
			  String path=formatPath(startPath);
			  File dir = new File(path);
			  if (!dir.isDirectory()){
				  result.add(dir);
			  } 
			  else{
				  File[] files=dir.listFiles();
				  if(files!=null){
					  for(int i=0; i<files.length; i++){
						  File currentFile=files[i];
						  if(currentFile.isDirectory()){
							  getFileList( result, currentFile.getAbsolutePath());
						  }else{
						  result.add(currentFile);
						  }
					  }
				  }
			  }
		  }
	  }
	  
	  public enum FileOperatorType {Move,Copy;}
	  /**
		 * ���������в����ƶ��ļ���ָ��Ŀ¼��
		 * ��windowsϵͳ�µ���mv��cp�����ƶ��򿽱��ļ���ָ��Ŀ¼��
		 * windowsϵͳ��Ҫ����move��copy�����ƶ��򿽱��ļ���ָ��Ŀ¼��
		 * optype Ϊ1���ƶ��ļ��� �����Ǹ����ļ�
		 */
	  public static boolean moveOrCopy(String filePath,String targetPath,FileOperatorType opType){
		  if(filePath==null){
			  filePath = "";
			}
			if(targetPath==null){
				targetPath= "";
			}
			String cmdline = "";
			String OsName = System.getProperty("os.name").toLowerCase();
			if (OsName.indexOf("win") != -1) {
				if(opType.equals(FileOperatorType.Move)){
					cmdline = "cmd.exe /c move /Y \"" + filePath + "\" \"" + targetPath + "\"";
				}else{
					cmdline = "cmd.exe /c copy /Y \"" + filePath + "\" \"" + targetPath + "\"";
				}
			}else{
				if(opType.equals(FileOperatorType.Move)){
					cmdline = "mv " + filePath + " " + targetPath;
				}else{
					cmdline = "cp " + filePath + " " + targetPath;
				}
			}
			Logger.log("ִ��shell���"+cmdline);
			return ShellUtil.command(cmdline);
	  }
	  
	  
	  public static void deleteFile(String path){
		  File file=new File(path);
		  if(file.isFile()){
			  file.delete();
		  }else{
			  File[] files=file.listFiles();
			  if(files!=null&&files.length>0){
				 for(File curFile:files){ 
					 deleteFile(curFile.getAbsolutePath());
				 }
			  }
			 file.delete();
		  }
	  }
	  
	  
	 public  static String getMd5ByPath(String filePath){
		  FileInputStream fis = null;
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				File file=new File(filePath);
					if(file!=null&&file.isFile()){
						fis = new FileInputStream(file);
						FileChannel fChannel = fis.getChannel();
						ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
						int length = -1;
						while ((length = fChannel.read(buffer)) != -1) {
							buffer.flip();
							md.update(buffer);
							buffer.compact();
						}
						byte[] b = md.digest();
						return byteToHexString(b);
				}else{
					return "";
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	  }
	 
	 
	 private static char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8','9', 'a', 'b', 'c', 'd', 'e', 'f' };
	  private static  String byteToHexString(byte[] tmp) {
			String s;// ���ֽڱ�ʾ���� 16 ���ֽ�
			char str[] = new char[16 * 2]; // ÿ���ֽ��� 16 ���Ʊ�ʾ�Ļ���ʹ�������ַ���
			// ���Ա�ʾ�� 16 ������Ҫ 32 ���ַ�
			int k = 0; // ��ʾת������ж�Ӧ���ַ�λ��
			for (int i = 0; i < 16; i++) { // �ӵ�һ���ֽڿ�ʼ���� MD5 ��ÿһ���ֽ� ת���� 16 �����ַ���ת��
				byte byte0 = tmp[i]; // ȡ�� i ���ֽ�
				str[k++] = hexdigits[byte0 >>> 4 & 0xf]; // ȡ�ֽ��и� 4 λ������ת��, >>> Ϊ�߼����ƣ�������λһ������
				str[k++] = hexdigits[byte0 & 0xf]; // ȡ�ֽ��е� 4 λ������ת��
			}
			s = new String(str); // ����Ľ��ת��Ϊ�ַ���
			return s;
		}
}

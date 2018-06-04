package util;

import java.io.InputStream;

public class ShellUtil {
	public static boolean command(String cmdline) {		
		return callCmd(cmdline);
	}

	private static boolean callCmd(String cmdline) {
		Runtime app = Runtime.getRuntime();
		Process proc = null;
		try {
			proc = app.exec(cmdline);
			new Thread(new SubTask(proc.getInputStream())).start();
			new Thread(new SubTask(proc.getErrorStream())).start();			
			proc.waitFor();
			InputStream errStream = proc.getErrorStream();
			int size = errStream.available();
			if (size == 0) {
				app.runFinalization();
				return true;
			} else {
				StringBuffer errMsg = new StringBuffer();
				byte[] bytes = new byte[size];
				while ((errStream.read(bytes, 0, bytes.length)) != -1) {
					errMsg = errMsg.append(new String(bytes));
    			}
				System.out.print("ִ������["+cmdline+"]ʱ���񵽴�������Ϣ��"+errMsg);
				Logger.log("ִ������["+cmdline+"]ʱ���񵽴�������Ϣ��"+errMsg);
			    return false;
			} 
		} catch (Exception e) {
			System.out.print("ִ������["+cmdline+"]ʱ��������"+e.getMessage());
			Logger.log("ִ������["+cmdline+"]ʱ��������"+e.getMessage());
			return false;			
		}
	}
}

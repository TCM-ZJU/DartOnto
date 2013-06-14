package mappingService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import dartgrid.QueryEngine;

public class SemregController {

	public String sayHelloTo(String str) {
		System.out.println("Hello " + str);
		File directory = new File("");//设定为当前文件夹 
		try{ 
		    System.out.println(directory.getCanonicalPath());//获取标准的路径 
		    System.out.println(directory.getAbsolutePath());//获取绝对路径 
		}catch(Exception e){} 
		return "Hi~ " + str;
	}
	public boolean newFile(String filename) {
		System.out.println("新建映射文件：" + filename);
		File f = new File(Constant.etc+"/"+filename);
		if(f.exists()){//检查File.txt是否存在 
			System.out.println("返回已有文件：" + filename);
			return true;
		}else{
			Document document;
	    try {
		    document = DocumentHelper.parseText("<tableregister></tableregister>");
				XMLWriter output = new XMLWriter(
				new FileWriter(new File(Constant.etc+"/"+filename)));
				output.write(document);
				output.close();
			  QueryEngine.reloadSemreg(Constant.etc+"/"+filename);
	    } catch (Exception e) {
	    	return false;
			}
		}
		return true;
	}

	public boolean save(String filename, String contant) {
		System.out.println("保存映射文件：" + filename + " " + contant);
		filename = Constant.etc+"/"+filename;
		Document document;
    try {
	    document = DocumentHelper.parseText(contant);
	    /*OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter output = new XMLWriter(
			new FileWriter(new File(filename)), format);
			output.write(document);
			output.close();*/
	    OutputFormat format = OutputFormat.createPrettyPrint();
	    format.setEncoding("UTF-8");
			XMLWriter output = new XMLWriter(new FileOutputStream(filename), format);
			output.write(document);
			output.close();	 
			QueryEngine.reloadSemreg(filename);
    } catch (Exception e) {
    	return false;
		}
		return true;
	}
	
	public boolean deleteFile(String filename) {
		System.out.println("删除映射文件：" + filename);
		filename = Constant.etc+"/"+filename;
		File f = new File(filename); 
		if(f.exists()){//检查File.txt是否存在 
			f.delete();//删除File.txt文件
			QueryEngine.removeSemreg(filename);
			System.out.println(filename + " 存在，已删除。"); 
		}else{
			System.out.println(filename + " 不存在");//输出目前所在的目录路径 
		}
		return true;
	}
}

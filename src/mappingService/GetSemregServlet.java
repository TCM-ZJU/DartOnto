package mappingService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class GetSemregServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6668217086863196920L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {// doPut
		String filename = req.getParameter("filename");
		String xml = getSemreg(filename);
		System.out.println("get semreg file: " + filename);
		System.out.println(xml);
		logger.info("get semreg file: " + filename);

		resp.setContentType("text/xml");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setCharacterEncoding("UTF-8");//GBK
		
		PrintWriter ou;
		try {
			ou = resp.getWriter();
			ou.write(xml);
			//ou.write(new String(xml.getBytes("ISO8859_1"),"GBK"));
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static String getSemreg(String filename) {
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			File xmlFile = new File(System.getProperty("user.dir"),Constant.etc+"/"+filename);
      document = saxReader.read(new InputStreamReader(new FileInputStream(xmlFile),"utf-8"));
			//document = saxReader.read(GetSemregServlet.class.getClassLoader().getResourceAsStream("etc/" + filename));
    } catch (DocumentException e) {
	    e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
    } catch (FileNotFoundException e) {
	    e.printStackTrace();
    }
    return document.asXML();
	}

	public static void main(String args[]) {
		long t1, t2;
		t1 = (new Date()).getTime();
		String html = getSemreg("tcmct.semreg");
		System.out.println(html);
		t2 = (new Date()).getTime();
		System.out.println("获取semreg文件内容：" + (t2 - t1) + "毫秒");
	}

	private final static Logger logger = Logger.getLogger(GetSemregServlet.class);
}

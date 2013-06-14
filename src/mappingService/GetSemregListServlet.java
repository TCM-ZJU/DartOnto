package mappingService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class GetSemregListServlet extends HttpServlet {

	/**
	 * 获取semreg文件列表
	 */
	private static final long serialVersionUID = -6503242254107775292L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		System.out.println("获取semreg文件列表 : ");
		String xml = getSemregList();
		System.out.println(xml);
		logger.info("获取semreg文件列表 : " + xml);

		resp.setContentType("text/xml");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter ou;
		try {
			ou = resp.getWriter();
			ou.write(xml);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private String getSemregList() { 
		File folder = new File(System.getProperty("user.dir"),Constant.etc);
		System.out.println(folder.toString());
		String[] all = folder.list();
		Document document = DocumentHelper.createDocument();
		Element files = document.addElement("files");
		for (int i = 0; i < all.length; i++) {
			String filename = all[i];
			File single = new File(Constant.etc + "\\" + all[i]);
			if (single.isFile() && (filename.endsWith(".semreg")))
				files.addElement("file").addText(filename);
		}
		return document.asXML();
	}

	public static void main(String args[]) {
		long t1, t2;
		t1 = (new Date()).getTime();
		String html = (new GetSemregListServlet()).getSemregList();
		System.out.println(html);
		t2 = (new Date()).getTime();
		System.out.println("获取semreg文件内容：" + (t2 - t1) + "毫秒");
	}

	private final static Logger logger = Logger
	    .getLogger(GetSemregListServlet.class);
}

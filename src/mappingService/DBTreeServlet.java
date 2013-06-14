package mappingService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class DBTreeServlet extends HttpServlet {

	/**
   * 获取数据库树，含表和字段
   */
  private static final long serialVersionUID = -2406508504462671552L;

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    String xml = DBController.getController().getDBTree();
		logger.info("获取数据库树：" + xml);
		
		resp.setContentType("text/xml"); 
		resp.setCharacterEncoding("UTF-8");
		PrintWriter ou;
		try {
			ou = resp.getWriter();
			//ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ou.write(xml);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String args[]){
    long t1,t2;   
    t1=(new Date()).getTime();
		String html = DBController.getController().getDBTree();
		System.out.println(html);
		t2=(new Date()).getTime();
    System.out.println("获得数据库的树形结构：" + (t2-t1)+"毫秒");
	}
	
	private final static Logger logger = Logger.getLogger(DBTreeServlet.class);
}

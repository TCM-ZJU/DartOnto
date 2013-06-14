package ontoService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ClassTreeServlet extends HttpServlet {

	/**
   * 获取本体类的树形结构
   */
  private static final long serialVersionUID = -4866238597232632844L;
  
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
  	boolean withProperty = new Boolean(req.getParameter("withProperty"));
    String xml = ModelController.getController().getClassTree(withProperty);
		logger.info("获取本体类树：" + xml);
  	System.out.println("[ClassTreeServlet] get Class Tree with Property:"+withProperty);
		
  	//if(!withProperty)
  	//	System.out.println(xml);
		resp.setContentType("text/xml"); 
		resp.setCharacterEncoding("UTF-8");
		resp.setDateHeader("Expires", 0);
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
		String html = ModelController.getController().getClassTree(true);
		System.out.println(html);
		t2=(new Date()).getTime();
    System.out.println("获得本体类的树形结构：" + (t2-t1)+"毫秒");
	}
	
	private final static Logger logger = Logger.getLogger(ClassTreeServlet.class);
}

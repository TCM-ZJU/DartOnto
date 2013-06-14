package instanceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConceptMapServlet extends HttpServlet {

	/**
   * Dart Query 使用的Web Service, 获得相关词，即由词获得以此词为概念词或异名的所有术语的所有域
   */
  private static final long serialVersionUID = 653822359297302766L;	

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
  	String concept = req.getParameter("concept");
  	System.out.println("get Map of " + concept );
  	try {
  		concept = new String(concept.getBytes("ISO8859_1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("cannot decode word: " + concept);
			e.printStackTrace();
		}
		//int level = new Integer(req.getParameter("level"));
    //System.out.println("get Map of " + concept + " with Level " + level);
    System.out.println("get Map of " + concept);
		String html = (new MapEngine()).getMap(concept);
		
		resp.setContentType("text/xml");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setCharacterEncoding("UTF-8");//
		PrintWriter ou;
		try {
			ou = new PrintWriter(resp.getOutputStream());
			ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ou.write(html);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
  
	public static void main(String args[]){
    long t1,t2;   
    t1=(new Date()).getTime();
    String html = (new MapEngine()).getMap("大黄");
		//String html = (new MapServlet()).getMap("大黄","http://cintcm.ac.cn/onto#formula");
		System.out.println(html);
		t2=(new Date()).getTime();
    System.out.println("查询‘大黄’的Map：" + (t2-t1)+"毫秒");
    System.out.println(html);
	}
}

package instanceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MapServlet extends HttpServlet {

	/**
   * Dart Query 使用的Web Service, 获得相关词，即由词获得以此词为概念词或异名的所有术语的所有域
   */
  private static final long serialVersionUID = 653822359297302766L;	

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
  	String concept = req.getParameter("concept");
  	try {
  		concept = new String(concept.getBytes("ISO8859_1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("cannot decode word: " + concept);
			e.printStackTrace();
		}
		String typeUri = req.getParameter("typeUri");
		if(typeUri == null)
			typeUri = "";
    System.out.println("get Map of " + concept + " with type of " + typeUri);
		String html = (new MapEngine()).getMap(concept, typeUri);
		
		resp.setContentType("text/xml");
		resp.setHeader("Cache-Control", "no-cache");
		//resp.setCharacterEncoding("UTF-8");//
		resp.setCharacterEncoding("GBK");//
		PrintWriter ou;
		try {
			ou = new PrintWriter(resp.getOutputStream());
			//ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ou.write("<?xml version=\"1.0\" encoding=\"GBK\"?>");
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
    //String html = (new MapEngine()).getMap("大黄","http://cintcm.ac.cn/onto#herb");
		//String html = (new MapServlet()).getMap("大黄","http://cintcm.ac.cn/onto#formula");
		System.out.println(html);
		t2=(new Date()).getTime();
    System.out.println("查询‘大黄’的Map：" + (t2-t1)+"毫秒");
    System.out.println(html);
    /*
		html = getCorrelative("山大黄");
		System.out.println(html);
		t1=(new Date()).getTime();
    System.out.println("查询‘山大黄’的相关词：" + (t1-t2)+"毫秒");
    System.out.println(html);*/
	}
}

package instanceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class PropertyServlet extends HttpServlet {
	/**
   * Dart Search 使用的Web Service, 获得概念词的属性注释
   * getProperty?word=大黄&property=父类&tier=3
   */
  private static final long serialVersionUID = 653822359297302788L;	

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
//  req.setCharacterEncoding("GBK");
    String word = req.getParameter("word");
  	String property = req.getParameter("property");
  	String tierStr = req.getParameter("tier");
  	int tier = 1;
  	if(tierStr != null)
  		tier = Integer.parseInt(req.getParameter("tier"));  	
    logger.info("decode word: " + word);
    try {
			word = new String(word.getBytes("ISO8859_1"), "GBK");
			property = new String(property.getBytes("ISO8859_1"), "GBK");
		} catch (UnsupportedEncodingException e2) {
			logger.debug("cannot decode word: " + word);
			e2.printStackTrace();
		}
    System.out.println("get " + property + " of " + word);
		String html = ConceptEngine.getContent(word,property,tier).asXML();
		
		resp.setHeader("Cache-Control", "no-cache");
		resp.setContentType("text/xml;charset=GBK");
		//resp.setCharacterEncoding("GBK");
		//resp.setContentType("text/xml;charset=UTF-8");
		PrintWriter ou;
		try {
			ou = new PrintWriter(resp.getOutputStream());
			ou.write("<?xml version=\"1.0\" encoding=\"GBK\"?>");
			//ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ou.write(html);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
  
	public static void main(String args[]){
		//ConceptEngine.getContent("大黄","定义").asXML();    
		//ConceptEngine.getContent("大黄","异名").asXML();  
		//ConceptEngine.getContent("大黄","父类").asXML();
		ConceptEngine.getContent("大黄","关联词").asXML();
	}

	private final static Logger logger = Logger.getLogger(PropertyServlet.class);
}

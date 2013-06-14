package ontoService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ClassObjectServlet extends HttpServlet {

	/**
   * 获取以本体类为主语的关系
   * ./getClassObject?uri=http://www.w3.org/2004/02/skos/core#Concept
   */
  private static final long serialVersionUID = 2071617825059979690L;

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
  	String uri = req.getParameter("uri");
  	if(uri == null){
  		String nameSpace = req.getParameter("nameSpace");
  		String localName = req.getParameter("localName");
  		uri = nameSpace + "#" + localName;
  	}
  	System.out.println("[ClassObjectServlet]"+req.getRemoteHost()+" get relationships of Subject Class :" + uri);
  	String xml = "";	
  	if(uri.equals(ModelController.conceptUri))
  		xml = ModelController.getController().getClassObject(uri);
  	else
  		xml = ModelController.getController().getClassObjectElement(uri,false,false).asXML();
		logger.info("获取以本体类为主语的关系：" + xml);
		
		if(!xml.equals("duplicate"))
			resp.setContentType("text/xml"); 
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Cache-Control", "no-cache");
		PrintWriter ou;
		try {
			ou = resp.getWriter();
			if(!xml.equals("duplicate"))
				ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ou.write(xml);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/***
	 * <type label="事物" uri="http://www.w3.org/2004/02/skos/core#Concept"><property label="概念词" uri="http://www.w3.org/2004/02/skos/core#prefLabel"/><property label="注释" uri="http://cintcm.ac.cn/onto#Comment"/><relation property="下位词" propertyUri="http://www.w3.org/2004/02/skos/core#narrower" object="事物" objectUri="http://www.w3.org/2004/02/skos/core#Concept"/><relation property="参考文献" propertyUri="http://cintcm.ac.cn/onto#Reference" object="中医文献" objectUri="http://cintcm.ac.cn/onto#paper"/><property label="二级分类" uri="http://cintcm.ac.cn/onto#Category2"/><property label="描述" uri="http://cintcm.ac.cn/onto#Description"/><relation property="上位词" propertyUri="http://www.w3.org/2004/02/skos/core#broader" object="事物" objectUri="http://www.w3.org/2004/02/skos/core#Concept"/><property label="异名" uri="http://www.w3.org/2004/02/skos/core#altLabel"/><relation property="出处" propertyUri="http://cintcm.ac.cn/onto#provence" object="中医文献" objectUri="http://cintcm.ac.cn/onto#paper"/><property label="一级分类" uri="http://cintcm.ac.cn/onto#Category1"/><relation property="有关联关系是" propertyUri="http://cintcm.ac.cn/onto#hasRela" object="关联关系" objectUri="http://cintcm.ac.cn/onto#Relation"/><property label="定义" uri="http://www.w3.org/2004/02/skos/core#definition"/></type>
	 * @param args
	 */
	public static void main(String args[]){
    long t1,t2;   
    t1=(new Date()).getTime();
		//String html = ModelController.getController().getClassObject("http://www.w3.org/2004/02/skos/core#Concept");
		//String html = ModelController.getController().getClassObjectElement("http://cintcm.ac.cn/onto#zhongyaozhiliao",false,false).asXML();
		String html = ModelController.getController().getClassObjectElement("http://cintcm.ac.cn/onto#herb",false,false).asXML();
		
		System.out.println(html);
		t2=(new Date()).getTime();
    System.out.println("获取以本体类为主语的关系：" + (t2-t1)+"毫秒");
	}
	
	private final static Logger logger = Logger.getLogger(ClassObjectServlet.class);
}

package instanceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ConceptServlet extends HttpServlet {

	/**
   * Dart Search 和 DartQuery 使用的Web Service, 获得同义词，即由词获得以此词为概念词的异名，或以此词为异名的概念词
   */
  private static final long serialVersionUID = -6237805889653875593L;
  
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
  	//req.setCharacterEncoding("GBK");
    String word = req.getParameter("word");
    boolean isFuzzy = new Boolean(req.getParameter("isFuzzy"));
    logger.info("decode word: " + word);
    System.out.println("decode word: " + word);
    try {
			word = new String(word.getBytes("ISO8859_1"), "GBK");
		} catch (UnsupportedEncodingException e2) {
			logger.debug("cannot decode word: " + word);
			e2.printStackTrace();
		}
				
		System.out.println("get concept of " + word);
		String html = getThesaurus(word, isFuzzy);
		System.out.println("concepts of " + word + ":" + html);
		
		resp.setHeader("Cache-Control", "no-cache");
		resp.setContentType("text/xml;charset=UTF-8"); 
		//resp.setCharacterEncoding("UTF-8");
		PrintWriter ou;
		try {
			//ou = new PrintWriter(resp.getOutputStream());
			ou = resp.getWriter();
			ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ou.write(html);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

  /**
   * 获得概念词列表，即由词获得以此词为异名的概念词，及其本身（如果它是概念词的话）
   * @param word
   * @return 同义词XML字符串
   * 例如：搜索"大黄"的同义词，返回XML：
   * 	<thesauruses>
   * 		<concept>水黄</concept>
   * 		<concept>小大黄</concept>
   * 		<concept>山大黄</concept>
   * 		<concept>大黄</concept>
   * 	</thesauruses>
   */
	private static String getThesaurus(String word,boolean isFuzzy){
	  Document doc = DocumentHelper.createDocument();
	  Element e = doc.addElement("concepts");
	  
	  List<String> thesauruses;
	  if(isFuzzy)
	  	thesauruses = ConceptEngine.getConcept(word, 20);
	  else
	  	thesauruses = ConceptEngine.getConcept(word);
	  for(int i=0; i<thesauruses.size(); i++)
	  	e.addElement("concept").setText(thesauruses.get(i));
	  
		return e.asXML();
	}
	
	public static void main(String args[]){
    long t1,t2;   
    t1=(new Date()).getTime();
		String html = getThesaurus("大黄", false);
		System.out.println(html);
		t2=(new Date()).getTime();
    System.out.println("查询‘大黄’的概念词：" + (t2-t1)+"毫秒");
		html = getThesaurus("山大黄", true);
		System.out.println(html);
		t1=(new Date()).getTime();
    System.out.println("查询‘山大黄’的概念词：" + (t1-t2)+"毫秒");
	}
	
	private final static Logger logger = Logger.getLogger(ConceptServlet.class);
}

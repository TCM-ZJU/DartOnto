package instanceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ontoService.ModelController;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cn.edu.zju.dart.query.result.QueryResult;
import dartgrid.QueryEngine;

public class TypeServlet extends HttpServlet {
	/**
   * Dart Search 使用的Web Service, 获得概念词的类型
   */
  private static final long serialVersionUID = 653822359297302788L;	

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    String prefLabel = req.getParameter("prefLabel");
    logger.info("decode prefLabel: " + prefLabel);
    try {
			prefLabel = new String(prefLabel.getBytes("ISO8859_1"), "GBK");
		} catch (UnsupportedEncodingException e2) {
			logger.debug("cannot decode prefLabel: " + prefLabel);
			e2.printStackTrace();
		}
    System.out.println("get Type of " + prefLabel);
    String html = getType(prefLabel).asXML();
    
		resp.setHeader("Cache-Control", "no-cache");
		resp.setContentType("text/xml;charset=GBK");
		PrintWriter ou;
		try {
			ou = new PrintWriter(resp.getOutputStream());
			ou.write("<?xml version=\"1.0\" encoding=\"GBK\"?>");
			ou.write(html);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
  protected static Element getType(String concept){
		long t1=(new Date()).getTime();
  	String sparql = "PREFIX skos: <" + ConceptEngine.skosPrefix + "#> " +
										"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
										"SELECT distinct ?type " +
										"WHERE {	?x skos:prefLabel '" + concept + "'. "+
										"       	?x rdf:type	?type .} ";
		Document reDoc = DocumentHelper.createDocument();
		Element root = reDoc.addElement("Results");
		QueryResult r;
		try {
			r = QueryEngine.sparql(sparql);
			while(r.next()){
				String content = r.getString(0);
				if(content.equals(ConceptEngine.skosPrefix+"#Concept"))
					continue;
				root.add(ModelController.getController().getClassDetail(content));
			}
		}catch (Exception e) {
			logger.error("Dart Grid: Query Error! --> " + concept + "(RDF.type)");
		}
		long t2=(new Date()).getTime();
		System.out.println("查询'"+concept+"'(RDF.type)"+"的时间：" + (t2-t1)+"毫秒");
		logger.info("查询'"+concept+"'(RDF.type)"+"的时间：" + (t2-t1)+"毫秒");
		System.out.println(reDoc.asXML());
		return root;
  }
	public static void main(String args[]){
		getType("山大黄");//查询'山大黄'(RDF.type)的时间：29125毫秒
		getType("大黄");//查询'大黄'(RDF.type)的时间：953毫秒
	}
	private final static Logger logger = Logger.getLogger(TypeServlet.class);
}

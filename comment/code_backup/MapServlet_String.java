package instanceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ontoService.ModelController;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class MapServlet extends HttpServlet {

	/**
   * Dart Query 使用的Web Service, 获得相关词，即由词获得以此词为概念词或异名的所有术语的所有域
   */
  private static final long serialVersionUID = 653822359297302766L;	

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
  	String uri = req.getParameter("uri");
  	try {
			uri = new String(uri.getBytes("ISO8859_1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			System.out.println("cannot decode word: " + uri);
			e.printStackTrace();
		}
		String type = req.getParameter("type");
    System.out.println("get Map of " + uri);
		String html = getMap(uri, type);
		
		resp.setContentType("text/xml");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setCharacterEncoding("GBK");
		PrintWriter ou;
		try {
			ou = new PrintWriter(resp.getOutputStream());
			ou.write(html);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
/**
 * 
 * 
		<Node id="10">
			<String builtin="elementType">壮阳药</String>
			<String builtin="color">2</String>
		</Node>
	</NodeArray>
	<EdgeArray>
		<Edge id="11">
			<String builtin="fromID">1</String>
			<String builtin="toID">2</String>
			<String builtin="eLabel">related</String>
			<String builtin="color">0xF00000</String>
			<String builtin="thick">1</String>
		</Edge>
 * @param uri
 * @param type
 * @return
 */
	private static String getMap(String uri, String typeUri){
		ModelController model = ModelController.getController();
	  Document doc = DocumentHelper.createDocument();
	  Element root = doc.addElement("motif");
	  Element nodeArray = root.addElement("NodeArray");
	  Element edgeArray = root.addElement("EdgeArray");
	  Map<String,Element> nodes = new HashMap<String,Element>();
	  
	  Element centerNode = nodeArray.addElement("Node").addAttribute("id", "1");
	  String centerLabel = model.getClassLabel(uri);
	  centerNode.addElement("String").addAttribute("builtin", "elementType").addText(centerLabel);
	  centerNode.addElement("String").addAttribute("builtin", "typeUri").addText(typeUri);
	  nodes.put(centerLabel, centerNode);
	  
	  Document objects = DocumentHelper.createDocument();
	  try {
	    objects = DocumentHelper.parseText(ModelController.getController().getClassObject(typeUri));
    } catch (DocumentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
    
    List relations = objects.getRootElement().elements("relation");
    for(int i=0, nodeID=2; i<relations.size(); i++){
    	Element relation = (Element) relations.get(i);
    	String propertyUri = relation.attributeValue("propertyUri");
    	String objectUri = relation.attributeValue("objectUri");
    	String sparql = "PREFIX skos: <" + ConceptEngine.skosPrefix + "#> " +
										"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
										"SELECT distinct ?type " +
										"WHERE {	?x skos:prefLabel '" + concept + "'. "+
										"       	?x rdf:type	?type .}";
    }
	  /* = ConceptEngine.getConcept(word);
  	for(int i=0; i<concepts.size(); i++){
	  	String concept = concepts.get(i);
	  //String concept = "大黄";
	  	Element result = root.addElement("correlative");
	  	result.addElement("概念词").addText(concept);
			for(int j=0; j<propertyList.length; j++){
				String property = propertyList[j];
				Element content = ConceptEngine.getContent(concept, property);
				content.setName(property + "s");
		  	result.add(content);
			}			  
	  }	  	*/
		return root.asXML();
	}
	
	
	public static void main(String args[]){
    long t1,t2;   
    t1=(new Date()).getTime();
		String html = getMap("大黄","");
		System.out.println(html);
		t2=(new Date()).getTime();
    System.out.println("查询‘大黄’的相关词：" + (t2-t1)+"毫秒");
    System.out.println(html);
    /*
		html = getCorrelative("山大黄");
		System.out.println(html);
		t1=(new Date()).getTime();
    System.out.println("查询‘山大黄’的相关词：" + (t1-t2)+"毫秒");
    System.out.println(html);*/
	}
}

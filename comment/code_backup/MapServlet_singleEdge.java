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

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cn.edu.zju.dart.query.result.QueryResult;
import dartgrid.QueryEngine;

public class MapServlet extends HttpServlet {

	/**
   * Dart Query 使用的Web Service, 获得相关词，即由词获得以此词为概念词或异名的所有术语的所有域
   */
  private static final long serialVersionUID = 653822359297302766L;	

  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
  	String concept = req.getParameter("concept");
  	System.out.println("1 get Map of " + concept );
  	try {
  		//concept = new String(concept.getBytes("ISO8859_1"), "GBK");
  		concept = new String(concept.getBytes("ISO8859_1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("cannot decode word: " + concept);
			e.printStackTrace();
		}
		String typeUri = req.getParameter("typeUri");
    System.out.println("get Map of " + concept + " with type of " + typeUri);
		String html = (new MapServlet()).getMap(concept, typeUri);
		
		resp.setContentType("text/xml");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setCharacterEncoding("UTF-8");//
		PrintWriter ou;
		try {
			ou = new PrintWriter(resp.getOutputStream());
			ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			//System.out.println("GBK->UTF-8: " + new String(html.getBytes("GBK"), "UTF-8"));
			//System.out.println("UTF-8->GBK: " + new String(html.getBytes("UTF-8"), "GBK"));
			//System.out.println("GBK->UTF-8: " + new String(html.getBytes("GBK"), "UTF-8"));
			//ou.write(new String(html.getBytes("UTF-8"), "GBK"));
			//ou.write(new String(html.getBytes("GBK"), "UTF-8"));
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
	private String getMap(String concept, String typeUri){
	  Document doc = DocumentHelper.createDocument();
	  Element root = doc.addElement("motif");
	  nodeArray = root.addElement("NodeArray");
	  edgeArray = root.addElement("EdgeArray");
	  

		Element centerNode = nodeArray.addElement("Node").addAttribute("id", "1");
	  centerNode.addElement("String").addAttribute("builtin", "elementType").addText(concept);
	  centerNode.addElement("String").addAttribute("builtin", "typeUri").addText(typeUri);
	  nodes.put(concept, centerNode);
	  
	  buildMap(centerNode);
	  List<?> nodeList = nodeArray.elements();
		for(int j=1; j<nodeList.size(); j++){
			Element node = (Element) nodeList.get(j);
			buildMap(node);
		}
		System.out.println(root.asXML());
		return root.asXML();
	}

	private void buildMap(Element centerNode){
		String concept="", typeUri="";
		List<?> strings = centerNode.elements();
		for(int j=0; j<strings.size(); j++){
			Element string = (Element) strings.get(j);
			if(string.attributeValue("builtin").equals("typeUri"))
				typeUri = string.getText();
			else if(string.attributeValue("builtin").equals("elementType"))
				concept = string.getText();
		}
	  Element objects;
	  if(objectMap.containsKey(typeUri))
	  	objects = objectMap.get(typeUri);
	  else{
	  	objects = model.getClassObjectElement(typeUri,false);
	  	objectMap.put(typeUri, objects);
	  }
	  addRelations(centerNode, concept, objects.elements("relation"), false);
	  Element subjects;
	  if(subjectMap.containsKey(typeUri))
	  	subjects = subjectMap.get(typeUri);
	  else{
	  	subjects = model.getClassSubjectElement(typeUri);
	  	subjectMap.put(typeUri, subjects);
	  }
    addRelations(centerNode, concept, subjects.elements("relation"), true);
	}
	private void addRelations(Element centerNode, String concept, List<?> relations, Boolean isOpposed){
		for(int i=0; i<relations.size(); i++){
    	Element relation = (Element) relations.get(i);
    	String propertyUri = relation.attributeValue("propertyUri");
    	String propertyLabel = model.getResLabel(propertyUri);
    	String propertyLocalName = propertyUri
					.replaceFirst(ConceptEngine.ontoPrefix+"#", "ns:")
					.replaceFirst(ConceptEngine.skosPrefix+"#", "skos:");
    	String objectUri = relation.attributeValue("objectUri");
    	String sparql = "PREFIX skos: <" + ConceptEngine.skosPrefix + "#> " +
    									"PREFIX ns: <" + ConceptEngine.ontoPrefix + "#> " +
											"SELECT distinct ?objLabel " +
											"WHERE {	?x skos:prefLabel '" + concept + "'. "+
					(isOpposed? "       	?obj "+propertyLocalName+" ?x."
										:	"       	?x "+propertyLocalName+" ?obj.") +
											"					?obj skos:prefLabel ?objLabel.}";    	
    	QueryResult r;
  		try {
  			r = QueryEngine.sparql(sparql);
  			while(r.next()){
  				String content = r.getString(0);
  				Element node = null;
  				Boolean isNewNode = true;
  				Boolean connected = false;
  				if(nodes.containsKey(content)){
  					Element e = (Element) nodes.get(content);
  					List<?> strings = e.elements();
  					for(int j=0; j<strings.size(); j++){
  						Element string = (Element) strings.get(j);
  						if(string.attributeValue("builtin").equals("typeUri"))
  							if(string.getText().equals(objectUri)){
  								isNewNode = false;
  								node = e;
  								//remove multiple edges
  								List<?> edges = edgeArray.elements();
  								for(int k=0; k<edges.size(); k++){
  									Element edge = (Element) edges.get(k);
  									String fromID="", toID="";
  									List<?> edgeStrings = edge.elements();
  									for(int l=0; l<edgeStrings.size(); l++){
  										Element edgeString = (Element) edgeStrings.get(l);
    									if(edgeString.attributeValue("builtin").equals("fromID"))
  											fromID = edgeString.getText();
  										else if(edgeString.attributeValue("builtin").equals("toID"))
  											toID = edgeString.getText();
  									}
  									if(	( fromID.equals(node.attributeValue("id")) && toID.equals(centerNode.attributeValue("id"))) ||
  											( toID.equals(node.attributeValue("id")) && fromID.equals(centerNode.attributeValue("id"))))
  										connected = true;
  								}
  							}
  					}
  				}
  				if(isNewNode) {
  					node = nodeArray.addElement("Node").addAttribute("id", nodeID+++"");
    			  node.addElement("String").addAttribute("builtin", "elementType").addText(content);
    			  node.addElement("String").addAttribute("builtin", "typeUri").addText(objectUri);
    			  nodes.put(content, node);
  				}
  				else if(connected)
  					continue;
  				Element edge = edgeArray.addElement("Edge").addAttribute("id", edgeID+++"");
  				edge.addElement("String").addAttribute("builtin", "eLabel").addText(propertyLabel);
  				edge.addElement("String").addAttribute("builtin", "typeUri").addText(propertyUri);
  				edge.addElement("String").addAttribute("builtin", "fromID")
  						.addText(isOpposed?node.attributeValue("id"):centerNode.attributeValue("id"));
  				edge.addElement("String").addAttribute("builtin", "toID")
  						.addText(isOpposed?centerNode.attributeValue("id"):node.attributeValue("id"));
  			}
  		}catch (Exception e) {
  			logger.error("Dart Grid: Query Error! --> " + concept + "(RDF.type)");
  			e.printStackTrace();
  		}
    }
	}
	
	private ModelController model = ModelController.getController();
  private Element nodeArray = null;
  private Element edgeArray = null;
  private Map<String,Element> nodes = new HashMap<String,Element>();
  private Map<String,Element> objectMap = new HashMap<String,Element>();
  private Map<String,Element> subjectMap = new HashMap<String,Element>();
  private int nodeID=2, edgeID=1;
  
	public static void main(String args[]){
    long t1,t2;   
    t1=(new Date()).getTime();
    String html = (new MapServlet()).getMap("大黄","http://cintcm.ac.cn/onto#herb");
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
	private final static Logger logger = Logger.getLogger(MapServlet.class);
}

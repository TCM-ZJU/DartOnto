package instanceService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ontoService.ModelController;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cn.edu.zju.dart.query.result.QueryResult;
//import cn.edu.zju.dart.util.GlobalTool;
import dartgrid.QueryEngine;

public class RelatedTermServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4735858150445932004L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		doPost(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		String word = req.getParameter("word");
		try {
			word = new String(word.getBytes("ISO8859_1"), "GBK");
		} catch (UnsupportedEncodingException e2) {
			logger.debug("cannot decode word: " + word);
			e2.printStackTrace();
		}
		System.out.println("[RelationServlet]get relationships of Instance :"
		    + word);
		String xml = getInstanceProperty(word);

		resp.setCharacterEncoding("UTF-8");
		// resp.setHeader("Cache-Control", "no-cache");
		PrintWriter ou;
		try {
			ou = resp.getWriter();
			ou.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ou.write(xml);
			ou.flush();
			ou.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/*****************************************************************************
	 * <instance prefLabel="大黄"> <type label="事物"
	 * uri="http://www.w3.org/2004/02/skos/core#Concept"> <property label="概念词"
	 * uri="http://www.w3.org/2004/02/skos/core#prefLabel"> <value>大黄</value>
	 * </property> <property label="注释" uri="http://cintcm.ac.cn/onto#Comment"/>
	 * <relation property="下位词"
	 * propertyUri="http://www.w3.org/2004/02/skos/core#narrower" object="事物"
	 * objectUri="http://www.w3.org/2004/02/skos/core#Concept"/> <relation
	 * property="参考文献" propertyUri="http://cintcm.ac.cn/onto#Reference"
	 * object="中医文献" objectUri="http://cintcm.ac.cn/onto#paper"/> <property
	 * label="二级分类" uri="http://cintcm.ac.cn/onto#Category2"/> <property
	 * label="描述" uri="http://cintcm.ac.cn/onto#Description"/> <relation
	 * property="上位词" propertyUri="http://www.w3.org/2004/02/skos/core#broader"
	 * object="事物" objectUri="http://www.w3.org/2004/02/skos/core#Concept"> <value
	 * uri="http://cintcm.ac.cn/onto?prefLabel=蓼科">蓼科</value> </relation>
	 * <property label="异名" uri="http://www.w3.org/2004/02/skos/core#altLabel"/>
	 * <relation property="出处" propertyUri="http://cintcm.ac.cn/onto#provence"
	 * object="中医文献" objectUri="http://cintcm.ac.cn/onto#paper"/> <property
	 * label="一级分类" uri="http://cintcm.ac.cn/onto#Category1"/> <relation
	 * property="有关联关系是" propertyUri="http://cintcm.ac.cn/onto#hasRela"
	 * object="关联关系" objectUri="http://cintcm.ac.cn/onto#Relation"/> <property
	 * label="定义" uri="http://www.w3.org/2004/02/skos/core#definition"/> </type>
	 * ... </instance>
	 * 
	 * @param word
	 * @return
	 */
	static String getInstanceProperty(String word) {
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("instance").addAttribute("prefLabel", word);		

		String uri = ModelController.conceptUri;
		addType(root, word, uri, true);
		
		Element types = TypeServlet.getType(word);
		List typeList = types.elements();
		for (int i = 0; i < typeList.size(); i++) {
			uri = ((Element) typeList.get(i)).attributeValue("uri");
			addType(root, word, uri, false);
		}		
		return root.asXML();
	}

	private static void addType(Element root, String word, String uri, boolean isConcept){
		Element object = ModelController.getController().getClassObjectElement(uri, isConcept, isConcept);
		root.add(object);
		addRelations(object, word, false);
		Element subject = ModelController.getController().getClassSubjectElement(uri, isConcept);
		root.add(subject);
		addRelations(subject, word, true);
	}
	private static void addRelations(Element object, String concept,
	    Boolean isOpposed) {
		List relations = object.elements("relation");
		for (int i = 0; i < relations.size(); i++) {
			Element relation = (Element) relations.get(i);
			String propertyUri = relation.attributeValue("propertyUri");
			if (propertyUri.equals(ConceptEngine.ontoPrefix + "#hasRela")) {
				buildRelaMap(concept, relation);
				continue;
			}
		  if(propertyUri.equals(ConceptEngine.ontoPrefix+"#relaConcept"))
		  	continue;
			String propertyLabel = relation.attributeValue("property");
			String sparql = ConceptEngine.getContentSparql(concept, propertyUri,
			    false, isOpposed);
			QueryResult r;
			boolean notContain = true;
			try {
				r = QueryEngine.sparql(sparql);
				while (r.next()) {
					notContain = false;
					String content = r.getString(0);
					relation.addElement("value").addText(content);
				}
			} catch (Exception e) {
				System.out.println(sparql);
				logger.error("Dart Grid: Query Error! --> " + concept + "("
				    + propertyLabel + ")");
			} finally {
				if (notContain)
					object.remove(relation);
			}
		}
		// System.out.println(object.asXML());
		List properties = object.elements("property");
		for (int i = 0; i < properties.size(); i++) {
			Element property = (Element) properties.get(i);
			object.remove(property);
		}
	}

	private static void buildRelaMap(String concept, Element relation) {
		Element relaXML = ConceptEngine.getContent(concept, "关联词");
		// <result db="中医药学语言系统" rela="被…使用">论利湿清热</result>
		List relas = relaXML.elements();
		for (int i = 0; i < relas.size(); i++) {
			Element rela = (Element) relas.get(i);
			relation.addElement("value").addAttribute("rela",
			    rela.attributeValue("rela")).addText(rela.getText());
		}
	}

	public static void main(String args[]) {
		// GlobalTool.setPrintable(true);
		long t1, t2;
		t1 = (new Date()).getTime();
		String html = getInstanceProperty("大黄");
		System.out.println(html);
		t2 = (new Date()).getTime();
		System.out.println("获取以本体类为主语的关系：" + (t2 - t1) + "毫秒");
	}

	private final static Logger logger = Logger
	    .getLogger(RelatedTermServlet.class);
}

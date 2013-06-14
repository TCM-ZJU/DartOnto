package ontoService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import mappingService.Constant;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;

public class ModelController {
	
	public static ModelController getController(){
		if(controller == null)
			controller = new ModelController();
		return controller;
	}
	
	public static void main(String args[]){
		ModelController m = new ModelController();
		System.out.println(m.getRelationTree());
		//System.out.println(m.getClassTree(true));
	}
	public ModelController(){
		buildFileModel();
		//initModel();
		//output();
	}
	private void buildFileModel(){
		model = readFile(model,"etc/newonto.owl");
		skosModel = readFile(skosModel, "etc/skos.owl");
		dcModel = readFile(dcModel, "etc/dcelements.owl");
		skosModel.add(dcModel);
		model.add(skosModel);
	}
	private OntModel readFile(OntModel m, String inputFileName){
		m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
//	use the FileManager to find the input file
		InputStream in = ModelController.class.getClassLoader().getResourceAsStream(inputFileName);
		//InputStream in = FileManager.get().open( inputFileName );
		if (in == null)
			System.out.println("File: " + inputFileName + " not found");
//	read the RDF/XML file
		m.read(in, "");
		return m;
	}
	public void output(){
		OutputStreamWriter writer;
		/*try {
	    writer = new OutputStreamWriter(System.out, "gb2312");
			model.write(writer,"RDF/XML-ABBREV");
    } catch (UnsupportedEncodingException e1) {
    	System.out.println( "Unsupported Encoding Exception");
	    e1.printStackTrace();
    }*/
		try {
      FileOutputStream os = new FileOutputStream(Constant.etc+"/newonto.owl");
      //writer = new OutputStreamWriter(os, "gb2312") ;
      writer = new OutputStreamWriter(os, "UTF-8") ;
      Model outModel = model.difference(skosModel);
      outModel.write(writer,"RDF/XML-ABBREV");//,"N-TRIPLE","RDF/XML-ABBREV"
	  } catch (IOException e) {
	   	System.out.println( "IO Exception");
	    e.printStackTrace();
	  } catch (InvalidPropertyURIException e) {
	  	System.out.println(e.getMessage());
	   	System.out.println( "Property URI Exception");
	  	e.printStackTrace();
	  }
	}

	/**
	 * 获得本体类的树形结构
	 * @return XML字符串
	 */
	public String getClassTree(boolean withProperty){
		OntClass res = model.getOntClass(conceptUri);
		Document doc = DocumentHelper.createDocument();
	  Element e = doc.addElement("type");
	  getSubClass2Tree(e, res, withProperty);
		return e.asXML();
	}
	private void getSubClass2Tree(Element e, OntClass r, boolean withProperty){
	  e.addAttribute("label", r.getLabel("zh"));
		e.addAttribute("uri", r.getURI());
		if(withProperty)
			getProperty(e, r);
		ExtendedIterator children = r.listSubClasses(true);
		ExtendedIterator instances = r.listInstances();
		children = children.andThen(instances);
		while (children.hasNext()) {
			OntClass child = (OntClass) children.next();
			Element childEle = e.addElement("type");
			if(!withProperty)
				childEle.addAttribute("parentUri", r.getURI());
			getSubClass2Tree(childEle, child, withProperty);
		}
	}
	
	/**
	 * 获得本体关联的树形结构
	 * @return XML字符串
	 */
	public String getRelationTree(){
		OntProperty pre = model.getOntProperty(relatedUri);
		Document doc = DocumentHelper.createDocument();
	  Element e = doc.addElement("rela");
	  getSubRelation2Tree(e, pre);
		return e.asXML();
	}
	private void getSubRelation2Tree(Element e, OntProperty p){
	  e.addAttribute("label", p.getLabel("zh"));
		e.addAttribute("uri", p.getURI());
		if(p.getRange() != null){
			e.addAttribute("rangeLabel", p.getRange().getLabel("zh"));
			e.addAttribute("range", p.getRange().getURI());
		}
		if(p.getDomain() != null){
			e.addAttribute("hasDomain", "true");
			e.addAttribute("domainLabel", p.getDomain().getLabel("zh"));
			e.addAttribute("domain", p.getDomain().getURI());
		} else
			e.addAttribute("hasDomain", "false");
		ExtendedIterator children = p.listSubProperties(true);
		while (children.hasNext()) {
			OntProperty child = (OntProperty) children.next();
			if(child.getLabel("zh") != null){
				Element childEle = e.addElement("rela");
				getSubRelation2Tree(childEle, child);
			}
		}
	}
	
	private void getProperty(Element e, OntClass r){
		getProperty(e, r, true);
	}
	private void getProperty(Element e, OntClass r, Boolean direct){
		getProperty(e, r, direct, true);
	}
	private void getProperty(Element e, OntClass r, Boolean direct, Boolean includeConcept){
		while(includeConcept?(r!=null):(r!=null&&!r.equals(model.getOntClass(conceptUri)))){
			StmtIterator stmts = model.listStatements(null, RDFS.domain, r);
			//ExtendedIterator properties = r.listDeclaredProperties(direct);
		  while(stmts.hasNext()){
		  	//OntProperty property = (OntProperty) properties.next();
		  	Statement stmt = stmts.nextStatement();
		  	OntProperty property = model.getOntProperty(stmt.getSubject().getURI());
		  	if(property.getLabel("zh") != null){
			  	if(property.isObjectProperty()){
			  		Element pre = e.addElement("relation")
		  			.addAttribute("property",property.getLabel("zh"))
		  			.addAttribute("propertyUri", property.getURI());
				  	OntResource object = property.getRange();
				  	while(object == null){
				  		property = property.getSuperProperty();
				  		object = property.getRange();
				  	}
				  	pre.addAttribute("object", object.getLabel("zh"))
		  			.addAttribute("objectUri", object.getURI());
			  	}
			  	else
			  		e.addElement("property")
			  		.addAttribute("label", property.getLabel("zh"))
		  			.addAttribute("uri", property.getURI());
		  	}
		  }
		  if(direct)
		  	r = null;
		  else
		  	r = r.getSuperClass();
		}
	}
	
	/**
	 * 添加本体类
	 * @param parentURI 父类的URI
	 * @return 新建类的URI
	 */
	public String addClass(String parentURI, String localName, String label){
		OntClass res = model.getOntClass(base+localName);
		if(res!=null && res.getSubClass()!=null)
			return "duplicate";
		res = model.createClass(base+localName);
		OntClass parent = model.getOntClass(parentURI);
		res.addSuperClass(parent);
		res.addLabel(label, "zh");
		Document doc = DocumentHelper.createDocument();
	  Element e = doc.addElement("type");
	  e.addAttribute("uri", res.getURI());
	  e.addAttribute("label", label);
		return e.asXML();
	}
	
	/**
	 * 删除本体类
	 * @param uri 此类的URI
	 * @return 删除成功与否
	 */
	public boolean deleteClass(String parentURI, String uri){
		OntClass res = model.getOntClass(uri);
		OntClass parent = model.getOntClass(parentURI);
		res.removeSuperClass(parent);
		return true;
	}
	
	/**
	 * 移动本体类
	 * @param uri 此类的URI
	 * @param oldParentUri 旧父类的URI
	 * @param newParentUri 新父类的URI
	 * @return 移动成功与否
	 */
	public boolean moveClass(String uri, String oldParentUri, String newParentUri, boolean isCopy){
		OntClass res = model.getOntClass(uri);
		OntClass newParent = model.getOntClass(newParentUri);
		res.addSuperClass(newParent);
		if(!isCopy){
			OntClass oldParent = model.getOntClass(oldParentUri);
			res.removeSuperClass(oldParent);			
		}
		return true;
	}
	
	/**
	 * 复制本体类
	 * @param uri 此类的URI
	 * @param oldParentUri 旧父类的URI
	 * @param newParentUri 新父类的URI
	 * @return 复制成功与否
	 */
	public boolean copyClass(String uri, String newParentUri, boolean isCopy){
		OntClass res = model.getOntClass(uri);
		OntClass newParent = model.getOntClass(newParentUri);
		res.addSuperClass(newParent);
		return true;
	}
	
	/**
	 * 根据URI获得本体类的细节
	 * @param uri 本体类资源的URI
	 * @return XML Element
	 */
	public Element getClassDetail(String uri){
		OntClass res = model.getOntClass(uri);
		return getClassDetail(res);
	}
	public Element getClassDetail(OntClass res){
		Document doc = DocumentHelper.createDocument();
	  Element e = doc.addElement("type");
	  e.addAttribute("label",res.getLabel("zh"));
	  e.addAttribute("uri",res.getURI());
	  return e;
	}
	public String getResLabel(String uri){
		OntResource res = model.getOntResource(uri);
		if(res == null)
			return uri.substring(uri.lastIndexOf('#')+1);
		return res.getLabel("zh");
	}
	/**
	 * 根据URI更新本体类的细节
	 * @param uri 本体类资源的URI
	 * @param label 资源的label
	 * @param localName 资源的新localName
	 * @return 是否成功
	 */
	public String updateClassDetail(String uri, String localName, String label, String objectUri){
		ResourceUtils.renameResource(model.getOntResource(uri), base + localName); 
		uri = base + localName;
		OntResource res = model.getOntResource(uri);
		res.setLabel(label, "zh");
		if(!objectUri.equals("null")){
			OntProperty pre = model.getOntProperty(uri);
			pre.setRange(model.getOntClass(objectUri));
		}
		return uri;
	}

	/**
	 * 根据URI获得以本体类为主语的关系
	 * @param uri 本体类资源的URI
	 * @return XML字符串
	 */
	public String getClassObject(String uri){
		return getClassObject(uri, true);
	}
	public String getClassObject(String uri, Boolean direct){
		return getClassObjectElement(uri, direct, true).asXML();
	}
	public Element getClassObjectElement(String uri, boolean direct, boolean includeConcept){
		OntClass res = model.getOntClass(uri);
		Element e = getClassDetail(res);
		getProperty(e, res, direct, includeConcept);
		return e;
	}
	/**
	 * 根据URI获得以本体类为宾语的关系
	 * @param uri 本体类资源的URI
	 * @return XML字符串
	 */
	public String getClassSubject(String uri){
		return getClassSubjectElement(uri,true).asXML();
	}
	public Element getClassSubjectElement(String uri, boolean includeConcept){
		OntClass res = model.getOntClass(uri);
		Element e = getClassDetail(res);		
		while(includeConcept?(res!=null):(!res.equals(model.getOntClass(conceptUri)))){
			StmtIterator stmts = model.listStatements(null, RDFS.range, res);
			while(stmts.hasNext()){
		  	Statement stmt = stmts.nextStatement();
		  	OntProperty property = model.getOntProperty(stmt.getSubject().getURI());
		  	if(property.getLabel("zh") != null){
			  	if(property.isObjectProperty()){
			  		Element pre = e.addElement("relation")
		  			.addAttribute("property",property.getLabel("zh"))
		  			.addAttribute("propertyUri", property.getURI());
				  	OntResource subject = property.getDomain();
				  	while(subject == null){
				  		property = property.getSuperProperty();
				  		if(property == null){
				  			subject = model.getOntResource(conceptUri);
				  			break;
				  		}
				  		subject = property.getRange();
				  	}
				  	pre.addAttribute("object", subject.getLabel("zh"))
		  			.addAttribute("objectUri", subject.getURI());
			  	}
		  	}
			}
			res = res.getSuperClass();
	  }
		return e;
	}
	/**
	 * 新建关系，并根据主语URI获得关系
	 * @param subUri 主语类资源的URI
	 * @param localName 关系的本地名
	 * @param label 关系的标签
	 * @param objUri 宾语类资源的URI
	 * @return 资源所有细节的XML字符串
	 */
	public String createRelation(String subUri, String localName, String label, String objUri, String parentUri){
		OntClass sub = model.getOntClass(subUri);
		if(objUri.equals("null")){
			DatatypeProperty pre = model.createDatatypeProperty(base + localName);
			pre.addLabel(label, "zh");
			pre.addDomain(sub);
		}
		else {
			OntClass obj = model.getOntClass(objUri);
			ObjectProperty pre = model.createObjectProperty(base + localName);
			OntProperty parent = model.getOntProperty(parentUri);
			pre.addLabel(label, "zh");
			pre.addDomain(sub);
			pre.addRange(obj);
			pre.addSuperProperty(parent);
		}		
		return getClassObject(subUri);
	}
	
	/**
	 * 添加关系，并根据主语URI获得关系
	 * @param subUri 主语类资源的URI
	 * @param uri    谓语关联的URI
	 * @param objUri 宾语类资源的URI
	 * @return 资源所有细节的XML字符串
	 */
	public String addRelation(String subUri, String uri, String objUri){
		OntClass sub = model.getOntClass(subUri);
		OntClass obj = model.getOntClass(objUri);
		OntProperty pre = model.getOntProperty(uri);
		pre.addDomain(sub);
		pre.setRange(obj);
		return getClassObject(subUri);
	}
	
	/**
	 * 移除关系，并根据主语URI获得关系
	 * @param subUri 主语类资源的URI
	 * @param uri 关系的URI
	 * @return XML字符串
	 */
	public String deleteRelation(String subUri, String uri){
		model.removeAll(model.getProperty(uri), RDFS.domain, model.getResource(subUri));
		return getClassObject(subUri);
	}
	
	/**
	 * 删除关系，并根据主语URI获得关系
	 * @param subUri 主语类资源的URI
	 * @param uri 关系的URI
	 * @return XML字符串
	 */
	public String destroyRelation(String subUri, String uri){
		OntClass sub = model.getOntClass(subUri);
		OntProperty pre = model.getOntProperty(uri);
		model.removeAll(pre, RDFS.domain, sub);
		pre.remove();
		return getClassObject(subUri);
	}
	
	public OntModel getModel(){
		return model;
	}
	
	static String base = "http://cintcm.ac.cn/onto#"; 
	public static String conceptUri	= "http://www.w3.org/2004/02/skos/core#Concept";
	static String relatedUri	= "http://www.w3.org/2004/02/skos/core#related";
	static ModelController controller = null;

	private OntModel model = null;
	private OntModel skosModel = null;
	private OntModel dcModel = null;
}

package ontoService;

public class RemoteModel {
	/************** Relation **************/
	public String createRelation(String subUri, String localName, String label, String objUri, String parentUri){
		System.out.println("create Relation: " + subUri + " " + localName + " " + label + " " + objUri + " " + parentUri);
		return ModelController.getController().createRelation(subUri, localName, label, objUri, parentUri);
	}

	public String addRelation(String subUri, String uri, String objUri){
		System.out.println("add Relation: " + subUri + " " + uri + " " + objUri);		
		return ModelController.getController().addRelation(subUri, uri, objUri);
	}
	
	public String deleteRelation(String subUri, String uri){
		System.out.println("remove Relation: " + subUri + " " + uri);		
		return ModelController.getController().deleteRelation(subUri, uri);
	}
	
	public String destroyRelation(String subUri, String uri){
		System.out.println("destroy Relation: " + subUri + " " + uri);		
		return ModelController.getController().destroyRelation(subUri, uri);
	}

	/************** Class **************/
	public String addClass(String parentURI, String localName, String label){
		System.out.println("add Class: " + parentURI + " " + localName + " " + label);
		String xml = ModelController.getController().addClass(parentURI, localName, label);
		System.out.println(xml);
		return xml;
	}
	
	public String updateDetail(String uri, String localName, String label, String objectUri){
		System.out.println("updata Ontology Resource: " + uri + " " + localName + " " + label);
		return ModelController.getController().updateClassDetail(uri, localName, label, objectUri);
	}
	
	public boolean deleteClass(String parenturi, String uri){
		System.out.println("delete Class: " + parenturi+ " " + uri);
		return ModelController.getController().deleteClass(parenturi, uri);
	}
	
	public boolean moveClass(String uri, String oldParentUri, String newParentUri, boolean isCopy){
		System.out.println("move Class: " + uri + " " + oldParentUri + " " + newParentUri + " isCopy:" + isCopy);
    return ModelController.getController().moveClass(uri, oldParentUri, newParentUri, isCopy);
	}

	public boolean copyClass(String uri, String newParentUri, boolean isCopy){
		System.out.println("copy Class: " + uri + " " + newParentUri);
    return ModelController.getController().copyClass(uri, newParentUri, isCopy);
	}
	
	public void save(){
		System.out.println("Save Ontology");
    ModelController.getController().output();
	}
	
	public static void main(String args[]){
		(new RemoteModel()).save();
	}
}

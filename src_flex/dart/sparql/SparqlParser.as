package dart.sparql
{
import mx.collections.ArrayCollection;
import mx.controls.Alert;
public class SparqlParser {
	public function SparqlParser(objectFeed:XML) {
		root = objectFeed;
		prefixs.addItem(new Prefix("tcm",/http:\/\/cintcm.ac.cn\/onto#/g,"http://cintcm.ac.cn/onto#"));
		prefixs.addItem(new Prefix("skos",/http:\/\/www.w3.org\/2004\/02\/skos\/core#/g,"http://www.w3.org/2004/02/skos/core#"));
		prefixs.addItem(new Prefix("dc",/http:\/\/purl.org\/dc\/elements\/1.1\//g,"http://purl.org/dc/elements/1.1/"));
	}
	
	private var root:XML;
	private var classCount:int = 1;
	private var propertyCount:int = 1;
	private var prefixs:ArrayCollection = new ArrayCollection();
	private var prefixStr:String = "";
	private var select:String = "SELECT";
	private var where:String = "WHERE{\n";
	private var filter:String = "";
	
	public function getSparql():String{
		var children:XMLList = root.children();
		parseChildren("?c"+classCount, children);
		for(var i:int=0; i<prefixs.length; i++){
			var prefix:Prefix = prefixs[i];
			if(prefix.match(where)){
				where = where.replace(prefix.pattern, prefix.token);
				prefixStr += prefix.preStr;
			}
		}
        var sparqlStr:String = prefixStr+select+"\n"+where+filter+"}";
        return sparqlStr;
    }
   	
	private function parseChildren(parentToken:String, children:XMLList):void {
		classCount++;
		for(var i:int=0; i<children.length(); i++){
			var child:XML = children[i];
			if(child.@state!=null && child.@state=="checked"){
				if(child.localName() == "property"){
					var p:String = "?p"+propertyCount;
					propertyCount++;
					where += "  "+parentToken+" "+child.@uri+" ";
	        		if(child.@oper==undefined || child.@oper=="select"){
	        			select += " "+p;
	        			where += p+".\n";
	        		} else if(child.@oper=="equal"){
	        			where += "'"+child.@value+"'"+".\n";	        			
	        			//where += p+".\n";
	        			//filter += "  FILTER("+p+"='" + child.@value + "').\n";
	        		} else if(child.@oper=="contain"){
	        			where += p+".\n";
	        			filter += "  FILTER(regex("+p+", '" + child.@value + "')).\n";
	        		}
	        	} else if(child.localName() == "relation"){
	        		var thisToken:String = "?c"+classCount;
	        		where += "  "+parentToken+" "+child.@propertyUri+" "+thisToken+".\n";
	        		parseChildren(thisToken, child.elements());
	        	}
        	}
        }
	}
}
}
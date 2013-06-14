package dart.mapping
{
	import mx.collections.ArrayCollection;
	import mx.utils.ObjectUtil;
	import mx.controls.Alert;

public class JoinParser
{
	private var join:XML;
	private var idArray:ArrayCollection = new ArrayCollection(new Array(10));
	private var equal:XML = new XML("<constaints/>");
	
    public function JoinParser(join:XML){
    	this.join = join;
    }
    
    public function parse():ArrayCollection {
    	analysis(join);
    	var result:ArrayCollection = new ArrayCollection();
    	var i:int;
    	var leftTable:String = 'Subject';
    	var table:Object = new Object();
    	table['uri'] = leftTable;
		result.addItem(table);
    	var leftID:int;
    	for(i=0;i<idArray.length;i++){
    		if(idArray.getItemAt(i) == leftTable)
    			break;
    	}
    	leftID = i;
    	var constraints:XMLList;
    	while(leftTable !='Object'){
	    	constraints = equal.children();
	    	for(i=0;i<constraints.length();i++){
	    		var constraint:XML = constraints[i];
	    		var columns:XMLList = constraint.column;
		    	for(var j:int=0;j<columns.length();j++){
		    		if(columns[j].@father == leftID){
		    			table['right']=columns[i].toString();
		    			
		    			table = new Object();
		    			var rightColumn:XML = columns[1-j];
		    			leftID = rightColumn.@father;
		    			leftTable = idArray.getItemAt(leftID).toString();
		    			table['uri'] = leftTable;
		    			table['left'] = rightColumn.toString();
		    			result.addItem(table);
		    			delete constraints[i];
		    			break;
		    		}
		    	}
	    	}
    	}
    	trace(mx.utils.ObjectUtil.toString(result));
    	return result;
    }
    private function analysis(joinNode:XML):void {
    	var id:int;
    	if(joinNode.left[0].@id == undefined){
    		analysis(joinNode.left[0].join[0]);
    	} else {
    		id = joinNode.left[0].@id;
    		idArray.addItemAt(joinNode.left[0].toString(),id);
    	}
    	if(joinNode.right[0].@id == undefined){
    		analysis(joinNode.right[0].join[0]);
    	} else {
    		id = joinNode.right[0].@id;
    		idArray.addItemAt(joinNode.right[0].toString(),id);
    	}
    	equal.appendChild(joinNode.constraint);
    }
    
    public static function reParse(joinArray:ArrayCollection,id:String=''):XML {
    	var join:XML = <join/>;
    	if(id != '')
    		join.@id = id;
    	var joinNode:XML = join;
    	found(joinArray,joinNode);
    	return join;
    }
    public static function found(joinArray:ArrayCollection,joinNode:XML):void {
    	var rightID:int = joinArray.length;
    	var rightTable:Object = joinArray.getItemAt(joinArray.length-1);
    	joinNode.appendChild(new XML(<right id={rightID}>{rightTable["uri"]}</right>));
    	joinArray.source.pop();
    	var leftID:int = joinArray.length;
    	var leftTable:Object = joinArray.getItemAt(joinArray.length-1);
    	if(joinArray.length>1){
	    	joinNode.appendChild(new XML(<left><join/></left>));
	    	var join:XML = joinNode.left[0].join[0];
	    	found(joinArray,join);
    	} else 
    		joinNode.appendChild(new XML(<left id={leftID}>{leftTable['uri']}</left>));
    	joinNode.appendChild(new XML(
    		<constraint>
    			<column refid="ID1" father={leftID}>{leftTable['right']}</column>
    			<column refid="ID2" father={rightID}>{rightTable['left']}</column>
    			<condition>###ID1### = ###ID2###</condition>
			</constraint>));    }
}

}
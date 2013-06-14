package dart.sparql
{
import flash.events.Event;
import flash.events.KeyboardEvent;

import mx.collections.*;
import mx.controls.*;
import mx.controls.treeClasses.*;
import mx.events.ListEvent;

public class CustomTreeItemRenderer extends TreeItemRenderer {
	public function CustomTreeItemRenderer() {
		super();
		mouseEnabled = false;
		input.width = 100;
		input.height = 20;
		//set font style
		comboBox.width = 80;
		comboBox.height = 20;
		comboBox.dataProvider = cards;
	}
	/**
	 * 表示CheckBox控件从data中所取数据的字段
	 */		
	private var _selectedField:String = "selected";
	static private var STATE_CHECKED:String = "checked";
	static private var STATE_UNCHECKED:String = "unchecked";
	
	protected var checkBox:CheckBox;
	protected var cards:ArrayCollection = new ArrayCollection(
                [ {label:"选择", data:"select"}, 
                  {label:"等于", data:"equal"}, 
                  {label:"包含", data:"contain"} ]);
	protected var input:TextInput = new TextInput();
	protected var comboBox:ComboBox = new ComboBox();
	
	/**
	 * 构建CheckBox
	 */		
	override protected function createChildren():void {
		super.createChildren();
		checkBox = new CheckBox();
		addChild( checkBox );
		checkBox.addEventListener(Event.CHANGE, checkBoxChangeHandler);
		comboBox.addEventListener(ListEvent.CHANGE, contentChangeHandler);
		input.addEventListener(KeyboardEvent.KEY_UP, contentChangeHandler);
	}
	
	override public function set data(value:Object):void {
		if(value != null) {
			super.data = value;
			setCheckState (value, value.@state);
		}
	}
	private function setCheckState (value:Object, state:String):void {
		if (state == STATE_CHECKED) checkBox.selected = true;
		else if (state == STATE_UNCHECKED) checkBox.selected = false;
		else checkBox.selected = false;
	}
	
	/**
	 * 点击checkbox时,更新dataProvider
	 * @param event
	 */		
	protected function checkBoxChangeHandler( event:Event ):void {
		if (data) {
			if(data.localName() == "property"){
				if(checkBox.selected){
					addChildAt(comboBox,4);
					addChildAt(input,5);
				} else {
					this.removeChild(comboBox);
					this.removeChild(input);
				}
			}
			var myListData:TreeListData = TreeListData(this.listData);
			var tree:Tree = Tree(myListData.owner);
			toggleParents(data, tree, checkBox.selected);
		}
	} 
	
	/**
	 * 点击combobox时,更新dataProvider
	 * @param event
	 */		
	protected function contentChangeHandler( event:Event ):void {
		if (data) {
			var content:String = comboBox.selectedItem.data;
			data.@oper = content;
			if(content != "select")
				data.@value = input.text;
		}
	} 
	
	private function toggleParents (item:Object, tree:Tree, checked:Boolean):void {
		if (item == null) {return;}
		if (checked) {
			item.@state = STATE_CHECKED;
			var parent:Object = tree.getParentItem (item);
			if (parent == null) return;
			toggleParents(parent, tree, checked);
		}
		else
			item.@state = STATE_UNCHECKED;
	}
	
	/**
	 * 重置itemRenderer的宽度
	 */		
	override protected function measure():void {
		super.measure();
		measuredWidth += checkBox.getExplicitOrMeasuredWidth();
		measuredWidth += comboBox.getExplicitOrMeasuredWidth();
		measuredWidth += input.getExplicitOrMeasuredWidth();
		measuredWidth += 20;
	}
	
	/**
	 * 重新排列位置, 将label后移
	 * @param unscaledWidth
	 * @param unscaledHeight
	 */		
	override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
		super.updateDisplayList(unscaledWidth, unscaledHeight);
		var startx:Number = data?TreeListData(listData).indent:0;
		
		if (disclosureIcon)	{
			disclosureIcon.x = startx;
			startx = disclosureIcon.x + disclosureIcon.width;			
			disclosureIcon.setActualSize(disclosureIcon.width,disclosureIcon.height);			
			disclosureIcon.visible = data?TreeListData( listData ).hasChildren:false;
		}		
		if(icon){
			icon.x = startx;
			startx = icon.x + icon.measuredWidth;
			icon.setActualSize(icon.measuredWidth, icon.measuredHeight);
		}		
		checkBox.move(startx, ( unscaledHeight - checkBox.height ) / 2 );		
		label.x = startx + checkBox.getExplicitOrMeasuredWidth();
		comboBox.x = label.x + label.getExplicitOrMeasuredWidth()+10;
		input.x = comboBox.x + comboBox.getExplicitOrMeasuredWidth()+10;
	}
}
}
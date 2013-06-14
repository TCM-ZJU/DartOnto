package dart.graph
{
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.text.TextFormat;
	
	import mx.core.UIComponent;
	
	public class Circle extends UIComponent
	{	
		public var child:Sprite = null;
		public var label:TextField = null;
		public var format:TextFormat = null;
        private var radius:Number         = 20;
        private var fillColor:uint;
        private var lineColor:uint;
        private var borderColor:uint;
        public var cx:Number;
        public var cy:Number;
		private var fillColorArr:Array = new Array(0x330099, 0x993399, 0xFF0099, 0x6600FF, 0xFF6666, 0x00CC33, 0x33CC00, 0xFFFF99, 0xCC66FF, 0xFF9900);
		private var lineColorArr:Array = new Array(0x000CCC, 0xFF33FF, 0xFF3366, 0x0000FF, 0xFF0000, 0x33FF00, 0x00FF00, 0xFFFF33, 0xFF00FF, 0xFFCC00);
		
        private function drawCircle(text:String, x:Number, y:Number,size:int = 12):void {
               
            label = new TextField();
            child = new Sprite();
            format = new TextFormat();
            label.text = text;
        		/* initialize format and label variables */
            format.color = 0x000000;
            format.size = size;
            format.align = "center";
            format.bold = true;
            format.font = "Verdana"; 
            label.defaultTextFormat = format;
            label.multiline = true;
            label.selectable = true;
            label.wordWrap = true; 
	        label.text = text;
            //label.x = x-10;
            //label.y = y-10;
            label.x = x - radius - 6;
			label.y = y - radius / 2 + radius / 8;
	        label.width = radius * 3;
	        label.height = radius * 2;
	        child.addChild(label);
	        this.addChild(child);  
            child.graphics.beginFill(fillColor);
            child.graphics.lineStyle(2,lineColor);
            child.graphics.drawCircle(x, y, radius);  
            //this.addChild(label);
            child.graphics.endFill();
        }
        
		public function getColorIndex(type:String):uint
		{
			var hashCode:Number = 0;
			var length:int = type.length;
			
			for(var i:int = 0; i < length; i++)
				hashCode += type.charCodeAt(i);
				
			return hashCode % 10;	
		}
		public function Circle(text:String = "test", type:String = "other", x:Number = 20, y:Number = 20, radius:Number = 20)
		{
			super();
			/* get color index */
			var i:int = getColorIndex(type);
			fillColor = fillColorArr[i];
			lineColor = lineColorArr[i];
			this.radius = radius;
			this.cx = x;
			this.cy = y;
			drawCircle(text, x, y);
		}

	}
}
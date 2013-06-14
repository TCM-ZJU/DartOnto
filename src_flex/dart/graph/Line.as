package dart.graph
{
	import flash.display.Sprite;
	import flash.events.MouseEvent;
	
	import mx.core.UIComponent;

	public class Line extends UIComponent
	{
		private var color:uint = 0x00CC00;
		private var colorBri:uint = 0x00FF00;
		private var fromX:Number;
		private var fromY:Number;
		private var toX:Number;
		private var toY:Number;
		private var line:Sprite = new Sprite();
		
		public function Line(fromX:Number = 0, fromY:Number = 0, toX:Number = 0, toY:Number = 0)
		{
			super();
			this.fromX = fromX;
			this.fromY = fromY;
			this.toX = toX;
			this.toY = toY;
			this.buttonMode = true;
			line.graphics.lineStyle(1.5, color);
			drawLine();
		}
		
		private function drawLine():void
		{
			line.graphics.moveTo(fromX, fromY);
			line.graphics.lineTo(toX, toY);
			this.addChild(line);
			this.addEventListener(MouseEvent.ROLL_OVER, rollOverHandler);
			this.addEventListener(MouseEvent.ROLL_OUT, rollOutHandler);
		}
		
		private function rollOverHandler(event:MouseEvent):void
		{
			this.useHandCursor = true;
			var tmp:Sprite = event.target.getChildAt(0);
			tmp.graphics.clear();
			tmp.graphics.lineStyle(1.5,colorBri);
			drawLine();
		}
		 
		private function rollOutHandler(event:MouseEvent):void
		{
			this.useHandCursor = false;
			var tmp:Sprite = event.target.getChildAt(0);
			tmp.graphics.clear();
			tmp.graphics.lineStyle(1.5,color);
			drawLine();			
		}
		
	}
}
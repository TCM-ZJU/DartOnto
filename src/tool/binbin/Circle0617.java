package tool.binbin;

public class Circle0617 {
	Circle0617(int n){
		this.n = 2*n;
	}
	public int count(){
		if(n==0) return 1;
		if(n==1) return 0;
		int count = 0;
		int way = n/2-1;
		for(int i=0; i<way; i+=2){
			Circle0617 small = new Circle0617(i/2);
			Circle0617 big = new Circle0617((n-i)/2-1);
			count += small.count()*big.count();
		}
		count *= 2;
		if(n%4==2){
			Circle0617 half = new Circle0617((n/2-1)/2);
			count += half.count() * half.count();
		}
		return count;
	}
	public static void main(String args[]){
		System.out.println("200: "+(new Circle0617(100)).count());
		System.out.println("2: "+(new Circle0617(1)).count());
		System.out.println("4: "+(new Circle0617(2)).count());
		System.out.println("6: "+(new Circle0617(3)).count());
		System.out.println("8: "+(new Circle0617(4)).count());
		System.out.println("10: "+(new Circle0617(5)).count());
	}
	private int n;
}

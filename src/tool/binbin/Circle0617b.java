package tool.binbin;

public class Circle0617b {
	Circle0617b(int n){
		this.n = n;
	}
	public int count(){
		int c[] = new int[200];
		java.util.Arrays.fill( c, 0 );

		c[0] = 1;
		for(int i=1;i<=n;i++){
			int count = 0;
			for(int j=0; j<i-1; j+=2)
				count += c[j/2]*c[i-j/2-1];
			count *= 2;
			if(i%2==1)
				count += c[(i-1)/2]*c[(i-1)/2];
			c[i] = count;
		}
		return c[n];
	}
	public static void main(String args[]){
		System.out.println(50*2+" : "+(new Circle0617b(50)).count());
		for(int i=0; i<10;i++){
			System.out.println(i*2+" : "+(new Circle0617b(i)).count());
		}
	}
	private int n;
}

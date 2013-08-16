package Test;

public class JNiTest {
	static{
		System.loadLibrary("dllname");
	}
	public native void display();
	public native double sum (double x, double y);
	public static void main(String [] args){
		
	}
}

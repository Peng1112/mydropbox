
package Test;


class A {
    int aa;
    
}

class B {
    A a;
    public B(A a) {
        this.a = a;
    }
}

public class NewClass {
    public static void main(String[] args) {
        A a = new A();
        a.aa = 3;
        B b = new B(a);
        a = new A();
        a.aa = 10;
        System.out.println(b.a.aa);
    } 
}

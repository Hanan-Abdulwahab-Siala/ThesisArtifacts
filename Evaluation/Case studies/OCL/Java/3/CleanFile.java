public class ImportsTest {
    public static void main(String[] args) {
        Random random = new Random();
    }
}
public class HelloWorld {
    public static void main(String[] args) {
        out.println("Hello World!");
        if (i == 3) doSomething();
        if (i == 2) {
            doSomething();
        } else {
            doSomethingElse();
        }
        if (i == 3) {
            doSomething();
        } else if (i == 2) {
            doSomethingElse();
        } else {
            doSomethingDifferent();
        }
        int a = 1;
        int b = 2;
        int minVal = (a < b) ? a : b;
        switch (ch) {
            case 'A':
                doSomething(); 
                break;
            case 'B':
            case 'C':
                doSomethingElse(); 
                break;
            default:
                doSomethingDifferent(); 
                break;
        }
        while (i < 10) {
            doSomething();
        }
        do {
            doSomething();
        } while (i < 10);
        for (int i = 0; i < 10; i++) {
            doSomething();
        }
     }
}
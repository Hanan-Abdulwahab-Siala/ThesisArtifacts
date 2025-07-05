public class ImportsTest {
    public static void main(String[] args) {
        /* The following line is equivalent to
         * java.util.Random random = new java.util.Random();
         * It would've been incorrect without the import declaration */
        Random random = new Random();
    }
}

public class HelloWorld {
    public static void main(String[] args) {
        /* The following line is equivalent to:
           System.out.println("Hello World!");
           and would have been incorrect without the import declaration. */
        out.println("Hello World!");
        
        // Conditional statements -------------------
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
        
        // switch
        switch (ch) {
            case 'A':
                doSomething(); // Triggered if ch == 'A'
                break;
            case 'B':
            case 'C':
                doSomethingElse(); // Triggered if ch == 'B' or ch == 'C'
                break;
            default:
                doSomethingDifferent(); // Triggered in any other case
                break;
        }
        
        // Iteration statements -------------------
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


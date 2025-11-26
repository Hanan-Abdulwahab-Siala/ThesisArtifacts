
public class HelloWorld {
    public static void main(String[] args) {
        
        // A more complex loop using two variables
        for (int i = 0, j = 9; i < 10; i++, j -= 3) {
            doSomething();
        }
        
        for (;;) {
            doSomething();
        }
        
        for (int i : intArray) {
            doSomething(i);
        }
        
        // Jump statements -------------------
        // Label
        start:
            someMethod();
        
        // break
        for (int i = 0; i < 10; i++) {
            while (true) {
                break;
            }
            // Will break to this point
        }
        
        outer:
            for (int i = 0; i < 10; i++) {
                while (true) {
                    break outer;
                }
            }
            // Will break to this point

        // continue
        int ch;
        while (ch = getChar()) {
            if (ch == ' ') {
                continue; // Skips the rest of the while-loop
            }

            // Rest of the while-loop, will not be reached if ch == ' '
            doSomething();
        }
        
        outer:
        for (String str : stringsArr) {
            char[] strChars = str.toCharArray();
            for (char ch : strChars) {
                if (ch == ' ') {
                    /* Continues the outer cycle and the next
                    string is retrieved from stringsArr */
                    continue outer;
                }
                doSomething(ch);
            }
        }
        
        // return
        // If streamClosed is true, execution is stopped
        if (streamClosed) {
            return;
        }
        readFromStream();

        int result = a + b;
        return result;        
    }
}


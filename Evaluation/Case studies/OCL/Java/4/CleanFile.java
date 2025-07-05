public class HelloWorld {
    public static void main(String[] args) {
        for (int i = 0, j = 9; i < 10; i++, j -= 3) {
            doSomething();
        }
        for (;;) {
            doSomething();
        }
        for (int i : intArray) {
            doSomething(i);
        }
        start:
            someMethod();
        for (int i = 0; i < 10; i++) {
            while (true) {
                break;
            }
        }
        outer:
            for (int i = 0; i < 10; i++) {
                while (true) {
                    break outer;
                }
            }
        int ch;
        while (ch = getChar()) {
            if (ch == ' ') {
                continue; 
            }
            doSomething();
        }
        outer:
        for (String str : stringsArr) {
            char[] strChars = str.toCharArray();
            for (char ch : strChars) {
                if (ch == ' ') {
                    continue outer;
                }
                doSomething(ch);
            }
        }
        if (streamClosed) {
            return;
        }
        readFromStream();
        int result = a + b;
        return result;        
    }
}
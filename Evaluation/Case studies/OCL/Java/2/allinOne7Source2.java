public class LexerTest { 

 static void main(String[] args) {
   
        int x, y, result;
        
        // Arithmetic operators
        result = x + y;
        result = x - y;
        result = x * y;
        result = y / x;
        result = x % 3;

        // Unary operators
        result = +x;
        result = -y;
        result = ++x;
        result = --y;
        boolean ok = false;
        boolean not_ok = !ok;

        // assignments yield a value
        (result = System.class).getName();

        // Prefix & postfix
        ++x;
        x++;
        --y;
        y--;
        LexerTest.prePost++;
        LexerTest.prePost--;
        myapplication.mylibrary.LexerTest.prePost++;
        myapplication.mylibrary.LexerTest.prePost--;
        this.prePost++;
        this.prePost--;
        super.prePost++;
        super.prePost--;
        someMethod()[0]++;
        someMethod()[0]--;

        ++LexerTest.prePost;
        --LexerTest.prePost;
        ++myapplication.mylibrary.LexerTest.prePost;
        --myapplication.mylibrary.LexerTest.prePost;
        ++this.prePost;
        --this.prePost;
        ++super.prePost;
        --super.prePost;
        ++someMethod()[0];
        --someMethod()[0];

        // Relational operators
        result = x == y;
        result = x != y;
        result = x > y;
        result = x >= y;
        result = x < y;
        result = x <= y;

        // Conditional operators
        if ((x > 8) && (y > 8)) {
        }
 
        if ((x > 10) || (y > 10)) {
        }

        result = (x > 10) ? x : y;
        
        // Ternary operator right associativity
        int f =  b1 ? b2 : b3 ? 3 : 4;

        // Bitwise and Bit shift operators
        result = ~x;
        result = x << 1;
        result = x >> 2;
        result = x >>> 3;
        result = x & 4;
        result = x ^ 5;
        result = x | 6;
        
        // Assignment operators
        result = x;
        result += x;
        result -= x;
        result *= x;
        result /= x;
        result %= x;
        result &= x;
        result ^= x;
        result |= x;
        result <<= x;
        result >>= x;
        result >>>= x;
    }

    public static void methodCalls() {
        new Object().getClass().hashCode();
        new String[] { "test" }[0].getLength();
        String[] strings;
        (strings = new String[] {"test"})[0].charAt(0);
        strings[0].length();
        Foo foo = new Foo().new Bar();
        foo.hashCode();
        Foo.class.hashCode();
        new HashMap<Object, String>(5).get(null);
    }
}

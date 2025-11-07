
// Constructors and initializers
class Foo1 {
    String str;

    Foo1() { // Constructor with no arguments
        // Initialization
    }

    Foo1(String str) { // Constructor with one argument
        this.str = str;
    }
}

class Foo2 {
    static {
        // Initialization
    }
}

class Foo3 {
    {
        // Initialization
    }
}


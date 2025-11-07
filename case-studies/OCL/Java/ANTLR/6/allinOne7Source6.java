// Classes -------------------
// Top-level class 
class Foo0 {
    // Class members
}

// Inner class
class Foo1 { // Top-level class
    class Bar { // Inner class
    }

    static void inner_class_constructor() {
        // https://docs.oracle.com/javase/specs/jls/se9/html/jls-15.html#jls-15.9
        Foo1 foo = new Foo1();
        Foo1.Bar fooBar1 = foo.new Bar();
        Foo1.Bar fooBar2 = new Foo1().new Bar();
    }
}


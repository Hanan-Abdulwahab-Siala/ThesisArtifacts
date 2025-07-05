class Foo0 {
}
class Foo1 { 
    class Bar { 
    }
    static void inner_class_constructor() {
        Foo1 foo = new Foo1();
        Foo1.Bar fooBar1 = foo.new Bar();
        Foo1.Bar fooBar2 = new Foo1().new Bar();
    }
}
class Foo { 
    static class Bar { 
    }
}
class Foo1 {
    void bar() {
        @WeakOuter
        class Foobar {
        }
    }
}
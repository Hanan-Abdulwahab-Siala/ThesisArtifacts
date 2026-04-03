// Nested class
class Foo { // Top-level class
    static class Bar { // Nested class
    }
}

// Local class
class Foo1 {
    void bar() {
        @WeakOuter
        class Foobar {// Local class within a method
        }
    }
}

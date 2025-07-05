class Foo {
    public Foo() {
        System.out.println(Foo.class.getName() + ": constructor runtime");
    }
    public Foo(int a, int b) {
        System.out.println(Foo.class.getName() + ": overloaded constructor " + this());
    }
    int bar(int a, int b) {
        return (a*2) + b;
    }
    int bar(int a) {
        return a*2;
    }
    void openStream() throws IOException, myException { 
    }
    void printReport(String header, int... numbers) { 
        System.out.println(header);
        for (int num : numbers) {
            System.out.println(num);
        }
    }
}
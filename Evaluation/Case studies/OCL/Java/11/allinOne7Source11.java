
// Overriding methods
class Operation {
    public int doSomething() {
        return 0;
    }
}

class NewOperation extends Operation {
    @Override
    public int doSomething() {
        return 1;
    }
}



class Dummy1 {
    public void dummy() {
        interface AnotherInterface extends Runnable { // local interface
            void work();
        }
    }
}

// Annotations  -------------------
@interface BlockingOperations1 {
}

@interface BlockingOperations {
    boolean fileSystemOperations();
    boolean networkOperations() default false;
}

class Dummy2 {
    @BlockingOperations(/*mandatory*/ fileSystemOperations = true,
    /*optional*/ networkOperations = true)
    void openOutputStream() { //Annotated method
    }

    @Unused // Shorthand for @Unused()
    void travelToJupiter() {
    }
}



class Dummy1 {
    public void dummy() {
        interface AnotherInterface extends Runnable { 
            void work();
        }
    }
}
@interface BlockingOperations1 {
}
@interface BlockingOperations {
    boolean fileSystemOperations();
    boolean networkOperations() default false;
}
class Dummy2 {
    @BlockingOperations( fileSystemOperations = true,
     networkOperations = true)
    void openOutputStream() { 
    }
    @Unused 
    void travelToJupiter() {
    }
}
interface Printer {
    void printDocument();
}
class Device {
    void powerOn() {
        System.out.println("The device is now powered on.");
    }
}
class LaserPrinter extends Device implements Printer {
    public void printDocument() {
        System.out.println("The printer is printing the document.");
    }
}
public class Main {
    public static void main(String[] args) {
        LaserPrinter printer = new LaserPrinter();
        printer.printDocument();
        printer.powerOn();
    }
}


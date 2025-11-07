
// Abstract classes
public class AbstractClass {
    private static final String hello;

    static {
        System.out.println(AbstractClass.class.getName() + ": static block runtime");
        hello = "hello from " + AbstractClass.class.getName();
    }

    {
        System.out.println(AbstractClass.class.getName() + ": instance block runtime");
    }

    public AbstractClass() {
        System.out.println(AbstractClass.class.getName() + ": constructor runtime");
    }

    public static void hello() {
        System.out.println(hello);
    }
}

public class CustomClass extends AbstractClass {

    static {
        System.out.println(CustomClass.class.getName() + ": static block runtime");
    }

    {
        System.out.println(CustomClass.class.getName() + ": instance block runtime");
    }

    public CustomClass() {
        super();
        System.out.println(CustomClass.class.getName() + ": constructor runtime");
    }

    public static void main(String[] args) {
        CustomClass nc = new CustomClass();
        hello();
        AbstractClass.hello();//also valid
    }
}


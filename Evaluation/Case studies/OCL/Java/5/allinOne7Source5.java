
public class HelloWorld {
    public static void main(String[] args) {
        
        try {
            // Statements that may throw exceptions
            methodThrowingExceptions();
        } catch (Exception ex) {
            // Exception caught and handled here
            reportException(ex);
        } finally {
            // Statements always executed after the try/catch blocks
            freeResources();
        }
        
        try {
            methodThrowingExceptions();
        } catch (IOException | IllegalArgumentException ex) {
            //Both IOException and IllegalArgumentException will be caught and handled here
            reportException(ex);
        }
        
        // try-with-resources statement
        try (FileOutputStream fos = new FileOutputStream("filename");
            XMLEncoder xEnc = new XMLEncoder(fos))
        {
            xEnc.writeObject(object);
        } catch (IOException ex) {
            Logger.getLogger(Serializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // throw
        if (obj == null) {
            // Throws exception of NullPointerException type
            throw new NullPointerException();
        }
        // Will not be called, if object is null
        doSomethingWithObject(obj);
        
        // Thread concurrency control -------------------
        /* Acquires lock on someObject. It must be of a reference type and must be non-null */
        synchronized (someObject) {
            // Synchronized statements
        }
        
        // assert statement
        // If n equals 0, AssertionError is thrown
        assert n != 0;
        /* If n equals 0, AssertionError will be thrown
        with the message after the colon */
        assert n != 0 : "n was equal to zero";
        
        // Reference types -------------------
        // Arrays
        int[] numbers = new int[5];
        numbers[0] = 2;
        int x = numbers[0];
        
        // Initializers -------------------
        // Long syntax
        int[] numbers = new int[] {20, 1, 42, 15, 34};
        // Short syntax
        int[] numbers2 = {20, 1, 42, 15, 34};
        
        // Multi-dimensional arrays
        int[][] numbers = new int[3][3];
        numbers[1][2] = 2;
        int[][] numbers2 = {{2, 3, 2}, {1, 2, 6}, {2, 4, 5}};
        
        int[][] numbers = new int[2][]; //Initialization of the first dimension only
        numbers[0] = new int[3];
        numbers[1] = new int[2];

        // Prefix & postfix
        numbers[0][0]++;
        numbers[0][0]--;
        ++numbers[0][0];
        --numbers[0][0];
        foo()[0]++;
        foo()[0]--;
        ++foo()[0];
        --foo()[0];
    }
}


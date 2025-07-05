public class HelloWorld {
    public static void main(String[] args) {
        try {
            methodThrowingExceptions();
        } catch (Exception ex) {
            reportException(ex);
        } finally {
            freeResources();
        }
        try {
            methodThrowingExceptions();
        } catch (IOException | IllegalArgumentException ex) {
            reportException(ex);
        }
        try (FileOutputStream fos = new FileOutputStream("filename");
            XMLEncoder xEnc = new XMLEncoder(fos))
        {
            xEnc.writeObject(object);
        } catch (IOException ex) {
            Logger.getLogger(Serializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (obj == null) {
            throw new NullPointerException();
        }
        doSomethingWithObject(obj);
        synchronized (someObject) {
        }
        assert n != 0;
        assert n != 0 : "n was equal to zero";
        int[] numbers = new int[5];
        numbers[0] = 2;
        int x = numbers[0];
        int[] numbers = new int[] {20, 1, 42, 15, 34};
        int[] numbers2 = {20, 1, 42, 15, 34};
        int[][] numbers = new int[3][3];
        numbers[1][2] = 2;
        int[][] numbers2 = {{2, 3, 2}, {1, 2, 6}, {2, 4, 5}};
        int[][] numbers = new int[2][]; 
        numbers[0] = new int[3];
        numbers[1] = new int[2];
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
public class Main {
   public static void main(String[] args) {
      double[] activations = new double[10];
      double[] checkpoints = new double[10];
      for (int i = 0; i < 10; i++) {
         activations[i] = Math.random();  
      }
      boolean useCheckpointing = true;  
      if (useCheckpointing) {
         System.arraycopy(activations, 0, checkpoints, 0, 10);  
      }
      for (int i = 0; i < 10; i++) {
         double[] source = useCheckpointing ? checkpoints : activations;
         activations[i] = source[i] - 0.1;  
      }
      System.out.println(java.util.Arrays.toString(activations));
   }
}

public class Main {
   public static void main(String[] args) {
      double activation = Math.random();  
      System.out.println("Forward pass: " + activation);
      boolean checkpointingEnabled = false;
      double checkpoint = 0.0;
      if (checkpointingEnabled) {
         checkpoint = activation;  
         activation = checkpoint - 0.2;  
      } 
      else {
         activation -= 0.2;  
      }
      System.out.println("Updated activation (without checkpointing): " + activation);
   }
}

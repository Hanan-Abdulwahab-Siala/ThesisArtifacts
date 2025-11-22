public class Main {
   public static void main(String[] args) {
      double activation = Math.random();  
      double checkpoint = 0.0;  
      System.out.println("Forward pass: " + activation);
      boolean checkpointingEnabled = true;
      if (checkpointingEnabled) {
          checkpoint = activation;  
      }
      if (checkpointingEnabled) {
         activation = checkpoint - 0.05;  
      } 
      else {
         activation -= 0.05;  
      }
      System.out.println("Updated activation: " + activation);
   }
}

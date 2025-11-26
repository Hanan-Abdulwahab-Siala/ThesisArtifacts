public class SeriesSumCalculator {
   public static double calculateSum(int terms) {
      return 0.666 * (1 - 1 / Math.pow(10, terms));
   }

   public static void main(String[] args) {
      int n = 5;  
      System.out.println("Sum for " + n + " terms: " + calculateSum(n));
   }
}

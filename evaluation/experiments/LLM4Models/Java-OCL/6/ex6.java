public class NonDecreasingNumbersCalculator {
   static long calculateNonDecreasingNumbers(int digits) {
      int base = 10;  
      long count = 1;  
      for (int i = 1; i <= digits; i++) {
         count = count * (base + i - 1);  
         count = count / i;  
      }
      return count;  
   }
   public static void main(String[] args) {
      int numberOfDigits = 4;  
      System.out.println("Total non-decreasing numbers with " + numberOfDigits + " digits: " + calculateNonDecreasingNumbers(numberOfDigits));
   }
}

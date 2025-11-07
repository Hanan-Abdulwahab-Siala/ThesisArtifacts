public class SubsequenceDivisibleByN {
   static int countDivisibleSubsequences(String inputString, int divisor) {
      int length = inputString.length();
      int prevModulo = 0;
      int currModulo = 0;
      if ((inputString.charAt(0) - '0') % divisor == 0) prevModulo++;
      for (int i = 1; i < length; i++) {
         currModulo = (inputString.charAt(i) - '0') % divisor;
         prevModulo += (prevModulo + (currModulo == 0 ? 1 : 0));
         int temp = (currModulo * 10 + (inputString.charAt(i) - '0')) % divisor;
         prevModulo += temp == 0 ? 1 : 0;
      }
      return prevModulo;
  }
  public static void main(String[] args) {
     String numberString = "12345";  
     int divisor = 3;  
     System.out.println("Divisible subsequences: " + countDivisibleSubsequences(numberString, divisor));
  }
}

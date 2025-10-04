class NumberChecker {
   int calculateDigitProduct(int num) {
      int digitSum = 0;
      while (num > 0) {
         digitSum += num % 10;
         num /= 10;
      }
      int reversedSum = 0;
      int temp = digitSum;
      while (temp > 0) {
         reversedSum = reversedSum * 10 + temp % 10;
         temp /= 10;
      }
      return digitSum * reversedSum;
   }
   public void checkNumber() {
       int number = 1729;
       int result = calculateDigitProduct(number);
       if (number == result) {
           System.out.println("Yes");
       } else {
           System.out.println("No");
       }
   }
   public static void main(String[] args) {
      NumberChecker checker = new NumberChecker();
      checker.checkNumber();
   }
}

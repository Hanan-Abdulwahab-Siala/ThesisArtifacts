import java.util.*;
import java.util.stream.*;
import java.lang.*;
import javafx.util.Pair;
public class ModuloFactorialCalculator {
   static int computeModuloFactorial(int n, int p) {
      if (n >= p) {
         System.out.println("Factorial modulo operation cannot be performed as n >= p.");
         return 0;
      }
      int result = 1;
      for (int i = 1; i <= n; i++) {
         result = (result * i) % p;
      }
      System.out.println("Computed factorial modulo p successfully.");
      return result;
   }
   public static void main(String[] args) {
      int number = 5;
      int modulo = 7;
      int result = computeModuloFactorial(number, modulo);
      System.out.println("Result: " + result);
   }
}

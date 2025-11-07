import java.util.*;
import java.util.stream.*;
import java.lang.*;
public class CompositeNumberChecker {
   static boolean isComposite(int number) {
      if (number <= 3) {
         System.out.println("False");
         return false;
      }
      if (number % 2 == 0 || number % 3 == 0) {
         System.out.println("True");
         return true;
      }
      for (int i = 5; i * i <= number; i = i + 6) {
         if (number % i == 0 || number % (i + 2) == 0) {
            System.out.println("True");
            return true;
         }
      }
      System.out.println("False");
      return false;
   }
   public static void main(String[] args) {
      int num = 10;  
      isComposite(num);
   }
}

public class AlgorithmAnalysis {
   static int findElement(int[] elements, int size, int target) {
      int index;
      for (index = 0; index < size; index++) {
         if (elements[index] == target) {
            return index;
         }
      }
      return -1;
   }
   public static void main(String[] args) {
      int[] numbers = {10, 20, 30, 40, 50};
      int target = 30;
      int result = findElement(numbers, numbers.length, target);
        
      if (result != -1) {
          System.out.println("Element found at index: " + result);
      } else {
          System.out.println("Element not found.");
      }
   }
}

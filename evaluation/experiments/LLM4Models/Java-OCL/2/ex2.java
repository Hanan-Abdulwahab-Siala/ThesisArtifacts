public class BinarySearchAlgorithm {
   static int searchElement(int[] sortedArray, int start, int end, int target) {
      if (end >= start) {
         int middle = start + (end - start) / 2;
         if (sortedArray[middle] == target)
            return middle;
         if (sortedArray[middle] > target)
            return searchElement(sortedArray, start, middle - 1, target);
         return searchElement(sortedArray, middle + 1, end, target);
     }
     return -1;
   }
   public static void main(String[] args) {
      int[] numbers = {1, 3, 5, 7, 9, 11, 13};  
      int target = 7;  
      int result = searchElement(numbers, 0, numbers.length - 1, target);
      if (result != -1)
         System.out.println("Element found at index: " + result);
      else
         System.out.println("Element not found.");
  }
}

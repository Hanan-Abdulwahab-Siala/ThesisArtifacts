class ConeVolumeCalculator {
   static float calculateVolume(float surfaceArea) {
      if (surfaceArea < 0) return -1;      
      float radius = (float)(surfaceArea * Math.sqrt(2)) / 3;
      float height = (2 * surfaceArea) / 3;
      float volume = (float)(3.14 * Math.pow(radius, 2) * height);
      return volume;
   }
   public static void main(String[] args) {
      float surfaceArea = 5;
      System.out.println(calculateVolume(surfaceArea));
   }
}

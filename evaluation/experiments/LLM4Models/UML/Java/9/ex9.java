import java.util.*;
class Engine { 
  int horsepower = 430;  
}
class Car { 
  float speed = 3.4f;    
  float fuelEfficiency = 9.6f;  
  public int calculateFuel(int distance) {
     Engine engine = new Engine();  
     return distance / engine.horsepower;  
  }
}  
public class Main {
  public static void main(String[] args) {
    Car car = new Car();
    int fuelNeeded = car.calculateFuel(100);  
    System.out.println("Fuel needed for 100 distance: " + fuelNeeded);
  }
}

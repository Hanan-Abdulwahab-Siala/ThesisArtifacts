from abc import ABC, abstractmethod
class GeometricFigure(ABC):  
   @abstractmethod
   def area(self):
      pass
class Square(GeometricFigure):  
   def __init__(self, side_length):  
      self.side_length = side_length
   def area(self):
      return self.side_length ** 2  
   def perimeter(self):  
      return 4 * self.side_length
square = Square(5)
print(f"Area: {square.area()}")
print(f"Perimeter: {square.perimeter()}")

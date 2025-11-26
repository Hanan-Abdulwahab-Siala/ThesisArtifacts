class Animal:
   def sound(self):
      print("Animals make different sounds")
class Dog(Animal):
   def sound(self):
      print("Dogs bark")
class Cat(Animal, Dog):
   pass
pet = Cat()
pet.sound()
print("This code demonstrates multiple inheritance in Python.")
print("The method resolution order determines which method is executed.")

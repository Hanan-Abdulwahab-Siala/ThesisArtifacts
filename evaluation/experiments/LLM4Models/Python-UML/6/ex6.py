class Entity:  
    def action(self):  
        pass
class Car(Entity):  
    def action(self):  
        return "Vroom! Vroom! The car is starting!"
class Airplane(Entity):  
    def action(self):  
        return "Whoosh! The airplane is flying!"
class Robot(Entity):  
    def action(self):  
        return "Beep boop! The robot is working!"
car = Car()
airplane = Airplane()
robot = Robot()
print(car.action())
print(airplane.action())
print(robot.action())

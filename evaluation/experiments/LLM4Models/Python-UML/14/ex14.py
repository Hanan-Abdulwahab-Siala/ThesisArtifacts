class Vehicle:
    def __init__(self, type_of_vehicle):
        self.type_of_vehicle = type_of_vehicle
    def operate(self):
        return f"The {self.type_of_vehicle} is operating."
    def stop(self):
        return f"The {self.type_of_vehicle} has stopped."
class Operator:
    def __init__(self, identifier):
        self.identifier = identifier
    def operate_vehicle(self, vehicle: Vehicle):
        return f"{self.identifier} operates the {vehicle.operate()}"
    def park_vehicle(self, vehicle: Vehicle):
        return f"{self.identifier} parks the {vehicle.stop()}"
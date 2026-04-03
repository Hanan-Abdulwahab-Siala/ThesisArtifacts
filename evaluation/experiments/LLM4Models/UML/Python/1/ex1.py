class Device:
    def __init__(self, brand):
        self.brand = brand
    def get_brand(self):
        return f"Brand: {self.brand}"
class Laptop(Device):
    def __init__(self, brand, ram, storage):
        super().__init__(brand)  
        self.ram = ram
        self.storage = storage
    def specs(self):
        return f"{self.get_brand()}, RAM: {self.ram}GB, Storage: {self.storage}GB"
class Smartphone(Device):
    def __init__(self, brand, battery_capacity):
        super().__init__(brand)  
        self.battery_capacity = battery_capacity
    def battery_life(self):
        return f"{self.get_brand()}, Battery: {self.battery_capacity}mAh"
laptop = Laptop("Dell", 16, 512)
print(laptop.specs())  
smartphone = Smartphone("Samsung", 5000)
print(smartphone.battery_life())  

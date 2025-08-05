class Item:  
    def __init__(self, name):
        self.name = name  
class Container:  
    def __init__(self):
        self.item_instance = Item("Example Item")  
    def get_name(self):  
        return self.item_instance.name  
container_instance = Container()
print(container_instance.get_name()) 
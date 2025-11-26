class Item:  
    def __init__(self):
        self.description = "This is an Item class"  
class Container:  
    def __init__(self):
        self.item_instance = None  
    def set_item_instance(self, item_instance):  
        self.item_instance = item_instance  
item = Item()  
container = Container()  
container.set_item_instance(item)
print(container.item_instance.description)

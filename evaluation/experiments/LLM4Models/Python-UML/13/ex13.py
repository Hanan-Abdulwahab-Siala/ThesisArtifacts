class Item:
    def __init__(self, data):
        self.data = data
class Container:
    def __init__(self, content):
        self.content = content   
    def method_1(self):
        y = Item("New Item")

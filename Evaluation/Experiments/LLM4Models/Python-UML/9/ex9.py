class Element:  
    def __init__(self, identifier):  
        self.identifier = identifier  
class Collection:  
    def __init__(self):
        self.elements = []  
    def add_element(self, element):  
        self.elements.append(element)  
collection_instance = Collection()
element1 = Element("Element1")  
element2 = Element("Element2")  
collection_instance.add_element(element1)
collection_instance.add_element(element2)


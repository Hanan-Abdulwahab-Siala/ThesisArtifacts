class Product:  
    def __init__(self, quantity):  
        self.quantity = quantity  
    def get_quantity(self):  
        return self.quantity  
class Factory:  
    def build_product(self):  
        return Product(13)  
factory = Factory()
product = factory.build_product()
print(product.get_quantity())

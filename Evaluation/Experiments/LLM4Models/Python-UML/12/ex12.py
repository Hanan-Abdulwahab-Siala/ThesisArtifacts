class Product:  
    def __init__(self):
        self.name = "Product Class"  
    def link_container(self, container_instance):  
        self.container_instance = container_instance  
class Container:  
    def __init__(self):
        self.year_established = 2024  
    def link_product(self, product_instance):  
        self.product_instance = product_instance  
product = Product()  
container = Container()  
container.link_product(product)
product.link_container(container)
print(container.product_instance.name)
print(product.container_instance.year_established)
class Operations:
    @staticmethod
    def add_numbers(a, b):  
        return a + b
    @classmethod
    def greet_class(cls):  
        return f"Hello from the class {cls.__name__}!"
    def greet_user(self, username):  
        return f"Hello, {username}, welcome to the platform!"
operation = Operations()
print(operation.add_numbers(5, 3))
print(operation.greet_class())
print(operation.greet_user("Alice"))

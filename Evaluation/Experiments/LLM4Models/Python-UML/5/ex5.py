from abc import ABC, abstractmethod
class Worker(ABC):
    def __init__(self, full_name, income):
        self.full_name = full_name
        self.income = income
    @abstractmethod
    def work(self):
        pass
    def show_name(self):
        return f"Name: {self.full_name}"
    def is_high_income(self):
        return self.income > 3000
class Developer(Worker):
    def work(self):
        return f"{self.full_name} is coding."
dev = Developer("John", 3500)
print(dev.show_name())
print(dev.work())
print(f"High income: {dev.is_high_income()}")

class Compensation:  
    def __init__(self, monthly_income):  
        self.monthly_income = monthly_income  
    def annual_income(self):  
        return self.monthly_income * 12  
class Worker:  
    def __init__(self, full_name, compensation):  
        self.full_name = full_name  
        self.compensation = compensation 
    def total_income(self):  
        return self.compensation.annual_income()  
compensation = Compensation(2000)
worker = Worker('Ali', compensation)
print(f"{worker.full_name}'s total annual income is: {worker.total_income()}")

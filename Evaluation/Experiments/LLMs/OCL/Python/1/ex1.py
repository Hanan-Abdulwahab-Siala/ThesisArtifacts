class Person:
   def __init__(self, full_name, student_id, age, score):
      self.full_name = full_name
      self.student_id = student_id
      self.age = age
      self.score = score  
   def increase_score(self, additional_score):
      self.score += additional_score
   def celebrate_birthday(self):
      self.age += 1
      return self.age
student = Person("Jorge", "123", 18, 80)  
new_age = student.celebrate_birthday()  
print(new_age)  

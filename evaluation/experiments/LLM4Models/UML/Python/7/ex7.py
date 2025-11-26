class Alpha:
   def alpha_method(self):
      print("Alpha Method")
class Beta:
   def beta_method(self):
      print("Beta Method")
class Gamma(Alpha, Beta):
   def gamma_method(self):
      print("Gamma Method")

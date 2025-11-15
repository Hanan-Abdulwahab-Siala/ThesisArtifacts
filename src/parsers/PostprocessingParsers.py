"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 2020-10-05

Description:
    This program removes extra elements from JSON files like type2, type3, and others that are listed below. 
"""
# ------------------------------------------------------------------------------
import os
import json
# ------------------------------------------------------------------------------
# Choose Java or Python
Language="Java"
#Language="Python"
# ------------------------------------------------------------------------------
def IsTypeReferencingClass(typeprint, ClassNames):
   if typeprint.startswith("String="):
      return False
   for ClassName in ClassNames:
      if ClassName in typeprint:
         return True
   return False
# ------------------------------------------------------------------------------
def PostprocessJSONFile(UML):
   ClassNames = {cls["name"] for cls in UML}
   for cls in UML:
      VarsList = cls.get("variables", []) 
      cls["variables"] = [
         var for var in VarsList
         if not IsTypeReferencingClass(var.get("typeprint", ""), ClassNames)
      ]
   return UML
# ------------------------------------------------------------------------------
def RemoveType2AndType3(data, tt):
   if isinstance(data, dict):
      if tt==1: 
         data.pop('type', None) 
         data.pop('type2', None)
         data.pop('type3', None)
         data.pop('IsNew', None)
         data.pop('IsParameterConstructor', None)
         data.pop('IsParameterMethod', None)
         data.pop('IsSetter', None)
         data.pop('IsConstructor', None)
         data.pop('AssignedInConstructor', None)
         data.pop('AssignedInMethod', None)
         data.pop('WhereNew', None)
         data.pop('IsArray', None)
         data.pop('superclass', None)
         data.pop('interfaces', None)
         data.pop('IsPass', None)
         data.pop('appendin', None)
         data.pop('functions', None)
         data.pop('selfAttributes', None)
         data.pop('has_stmt', None)
         data.pop('AllFunction', None)
         data.pop('selfAttributes', None)
         data.pop('localVariables', None)
         data.pop('returntypeprint', None)
         data.pop('returnType2', None)
         data.pop('returnType3', None)       
      else:
         data.pop('Flag', None)
      for key, value in data.items():
         RemoveType2AndType3(value, tt)
   elif isinstance(data, list):
      for item in data:
         RemoveType2AndType3(item, tt)
# ------------------------------------------------------------------------------
def ReadFiles(directory):
   for root, dirs, files in os.walk(directory):
      for file in files:
         if file.endswith(".UML"):
            program = os.path.join(root, file)
            with open(program, 'r') as infile:
               try:
                  FileContent = json.load(infile)
               except json.JSONDecodeError as e:
                  print(f"Error decoding JSON in file {program}: {e}")
                  continue

            if Language=="Java":
               items = FileContent if isinstance(FileContent, list) else [FileContent]

               for obj in items:
                  for method in obj.get("methods", []):
                     if "returntypeprint" in method:
                        method["returnType"] = method["returntypeprint"]  
                        method.pop("returntypeprint", None)               

            RemoveType2AndType3(FileContent,1)
            FileContent=PostprocessJSONFile(FileContent) 
            UMLData = json.dumps(FileContent, indent=4)
            with open(program, 'w') as outfile:
               outfile.write(UMLData)
         if file.endswith(".REL"):
            program = os.path.join(root, file)
            with open(program, 'r') as infile:
               try:
                  FileContent = json.load(infile)
               except json.JSONDecodeError as e:
                  print(f"Error decoding JSON in file {program}: {e}")
                  continue
            RemoveType2AndType3(FileContent,2)
            UMLData = json.dumps(FileContent, indent=4)
            with open(program, 'w') as outfile:
               outfile.write(UMLData)

# ------------------------------------------------------------------------------
Directory  = ####
ReadFiles(Directory)  
# ------------------------------------------------------------------------------



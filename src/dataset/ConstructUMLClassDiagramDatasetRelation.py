"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 2020-10-05

Description:
    This program constructs a training dataset to abstract UML class diagrams from both Java and Python programs.
"""
# ------------------------------------------------------------------------------
import os
import re
import json
# ------------------------------------------------------------------------------
def CleanJavaCode(JavaCode, AllCleaning):
   JavaCode=re.sub(r'\/\/.*', '', JavaCode)                            # Remove //
   JavaCode=re.sub(r'\/\*[\s\S]*?\*\/', '', JavaCode)                  # Remove multi-line comments
   CleanedLines=[]
   Lines=JavaCode.split('\n')
   for line in Lines:
      if not line.strip():
         continue
      LeadingSpaces=len(line)-len(line.lstrip())
      if AllCleaning:
         CleanedLines.append(line.strip())
      else:
         CleanedLines.append(' '*LeadingSpaces+line.strip())
   CleanedFile='\n'.join(CleanedLines)  
   return CleanedFile
# ------------------------------------------------------------------------------
def CleanPythonCode(PythonCode, AllCleaning):
    # Remove single-line comments except those containing #@@
    PythonCode = re.sub(r'^(?!.*#@@).*#.*', '', PythonCode, flags=re.MULTILINE)
    PythonCode = re.sub(r'(\'\'\'[\s\S]*?\'\'\'|\"\"\"[\s\S]*?\"\"\")', '', PythonCode)  
    CleanedLines = []
    Lines = PythonCode.split('\n')
    
    for line in Lines:
       if line.endswith('='):
          line += '\"String\"'
          CleanedLines.append(line)  
       if not line.strip():
          continue
       LeadingSpaces = len(line) - len(line.lstrip())
       if AllCleaning:
          CleanedLines.append(line.strip())
       else:
          CleanedLines.append(' ' * LeadingSpaces + line.strip())
    
    CleanedFile = '\n'.join(CleanedLines)
    return CleanedFile
# ------------------------------------------------------------------------------
def CheckFileEmpty(File):
   if os.path.getsize(File) == 0:
      return ""
   else:
      with open(File, 'r') as file:
         Content=file.read()
      return Content
# ------------------------------------------------------------------------------
def ConstructClassPairs(Directory, Language):
   Result=[]
   count=0
   for Root, Dirs, Files in os.walk(Directory):
      for File in Files:
         Directory1 = os.path.join(Root, '') 
         if (Language=="Java" and File.endswith(".java")) or (Language=="Python" and File.endswith(".py")):
            Program=Root+"\\"+File
            print(Program)
            Point=File.rfind('.')
            UML=Root+"\\"+File[:Point]+".UML"
            REL=Root+"\\"+File[:Point]+".REL"
            ClassesRelationships={"ClassesInterfaces": [], "Relationships": []}
            ProgramCode=CheckFileEmpty(Program)
            if ProgramCode != "":
               if os.path.isfile(UML):
                  UMLCode=CheckFileEmpty(UML)
                  if UMLCode != "":
                     with open(UML, 'r') as f:
                        UMLFile=f.read()
                     ClassesData=json.loads(UMLFile)
                     ClassesRelationships["ClassesInterfaces"].extend(ClassesData)
                     RELCode=CheckFileEmpty(REL)
                     if RELCode:
                        with open(REL, 'r') as f:
                           RELFile=f.read()
                        RelationshipData=json.loads(RELFile)
                        ClassesRelationships["Relationships"].extend(RelationshipData)
                     if Language=="Java":
                        ProgramCode=CleanJavaCode(ProgramCode, False)
                     if Language=="Python":
                        ProgramCode=CleanPythonCode(ProgramCode, False)
                     count+=1  
                     Result.append({
                           "program": Directory1 + File,
                           "input": ProgramCode,
                           "output": {
                               "classes": json.dumps(ClassesData),   
                               "relationships": json.dumps(RelationshipData) 
                           }
                     }) 

   print(count)                  
   return Result
# ------------------------------------------------------------------------------
Language="Java"
Language="Python"

if Language=="Java":
   Directory=####
   OutputFile=Directory+"JavaUML.json" 
else:
   Directory=####
   OutputFile=Directory+"PythonUML.json"   

Result=ConstructClassPairs(Directory, Language)

final_json = json.dumps(Result, indent=2)

with open(OutputFile, 'w') as f:
   f.write(final_json)
# ------------------------------------------------------------------------------
   
"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 17-02-2026

Description:
    This program pre-processes Java and Python programs before abstracting UML class diagrams and OCL specifications.
"""
# -----------------------------------------------------------------------------
import os
import re
# -----------------------------------------------------------------------------
# Choose Java or Python program
Language="Java"
#Language="Python"
# ------------------------------------------------------------------------------
def CleanJavaCode(JavaCode):
   JavaCode = re.sub(r'//.*', '', JavaCode)
   JavaCode = re.sub(r'/\*[\s\S]*?\*/', '', JavaCode)
   JavaCode = re.sub(r'^\s*package\s+[^\s;]+;\s*', '', JavaCode, flags=re.MULTILINE)
   JavaCode = re.sub(r'^\s*import\s+[^\s;]+;\s*', '', JavaCode, flags=re.MULTILINE)
   JavaCode = re.sub(r'""".*?"""', '""', JavaCode, flags=re.DOTALL)
   JavaCode = '\n'.join(line for line in JavaCode.splitlines() if line.strip())
   return JavaCode
# ------------------------------------------------------------------------------
def CleanPythonCode(PythonCode):
   PythonCode=re.sub(r'#.*', '', PythonCode)
   PythonCode=re.sub(r'(\'\'\'[\s\S]*?\'\'\'|\"\"\"[\s\S]*?\"\"\")', '', PythonCode)
   PythonCode = re.sub(r'^\s*from\s+[^\s;]+;\s*', '', PythonCode, flags=re.MULTILINE)
   PythonCode = re.sub(r'^\s*import\s+[^\s;]+;\s*', '', PythonCode, flags=re.MULTILINE)
   CleanedLines=[]
   Lines=PythonCode.split('\n')
   for line in Lines:
      if line.endswith('='):
         line+='\"String\"'
         CleanedLines.append(line)  
      if not line.strip():
         continue
      LeadingSpaces=len(line)-len(line.lstrip())
      CleanedLines.append(' '*LeadingSpaces+line.strip())
   CleanedFile='\n'.join(CleanedLines)
   return CleanedFile
# ------------------------------------------------------------------------------
def CheckFileEmpty(File):
   if os.path.getsize(File)==0:
      return ""
   else:
      with open(File, 'r') as file:
         Content=file.read()
      return Content
# ------------------------------------------------------------------------------
def CleaningFile(SourceFile, DestinationFile, Language):
   try: 
      with open(SourceFile, 'r') as file:
         Program=file.read()
      if Language=="Java":
         InputFile= CleanJavaCode(Program) 
      else:
         InputFile= CleanPythonCode(Program) 
      if os.path.exists(DestinationFile):
         os.remove(DestinationFile)
      with open(DestinationFile, "w", encoding="utf-8") as file:
         file.write(InputFile) 
   except FileNotFoundError:
      print(f"File not found: {SourceFile}")
    
# -----------------------------------------------------------------------------
def PutTogether(InputDirectoryProgram, SourceFile):
   JavaCodeList = []

   for root, dirs, files in os.walk(InputDirectoryProgram):
      for file in files:
         if (Language=="Java" and file.endswith(".java")) or (Language=="Python" and file.endswith(".py")):
            FilePath = os.path.join(root, file)
            with open(FilePath, "r", encoding="utf-8") as f:
               JavaCode = f.read()
               JavaCodeList.append(JavaCode)

   CombinedCode = "\n".join(JavaCodeList)  
   if os.path.exists(SourceFile):
      os.remove(SourceFile)
   with open(SourceFile, "w", encoding="utf-8") as file:
      file.write(CombinedCode)      
# -----------------------------------------------------------------------------
InputDirectory=#### 
OutputDirectory=####
if Language=="Java":  
   FileName='Temp.java'
   OutputFile='Test1.java'
else:
   FileName='Temp.py' 
   OutputFile='Test1.py' 

SourceFile=os.path.join(InputDirectory, FileName)  
DestinationFile=os.path.join(OutputDirectory, OutputFile) 

if os.path.exists(SourceFile):
   os.remove(SourceFile)

if os.path.exists(DestinationFile):
   os.remove(DestinationFile)

PutTogether(InputDirectory, SourceFile) 

CleaningFile(SourceFile, DestinationFile, Language)

if os.path.exists(SourceFile):
   os.remove(SourceFile)
# -----------------------------------------------------------------------------

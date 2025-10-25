"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 2020-10-05

Description:
    This program constructs a training dataset to abstract OCL specifications from both Java and Python programs.
"""
# ------------------------------------------------------------------------------
import os
import re
import json
# ------------------------------------------------------------------------------
def CleanKm3(OCLFile, AllCleaning):
   CleanedLines=[]
   Lines=OCLFile.split('\n')
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
   # Remove single-line comments
   PythonCode=re.sub(r'#.*', '', PythonCode)
   # Remove multi-line comments
   PythonCode=re.sub(r'(\'\'\'[\s\S]*?\'\'\'|\"\"\"[\s\S]*?\"\"\")', '', PythonCode)
   CleanedLines=[]
   Lines=PythonCode.split('\n')
   for line in Lines:
      if line.endswith('='):
         line+='\"String\"'
         CleanedLines.append(line)
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
def CheckFileEmpty(File):
   if os.path.getsize(File) == 0:
      return ""
   else:
      with open(File, 'r') as file:  # , encoding="utf-8"
         Content=file.read()
      return Content
# ------------------------------------------------------------------------------
def ConstructClassPairs(Directory, Language):
   count=0 
   Result=[]
   for Root, Dirs, Files in os.walk(Directory):
      #Directory1= Root+r'\'
      Directory1 = os.path.join(Root, '')
      for File in Files:
         if (Language=="Java" and File.endswith(".java")) or (Language=="Python" and File.endswith(".py")):
            Program=Directory1+File
            print(Program)
            ProgramCode=CheckFileEmpty(Program)
            if ProgramCode != "":
               for File1 in Files: 
                  if File1.endswith(".km3"):     
                     km3=CheckFileEmpty(Directory1+File1)
                     if km3 != "":
                        if Language=="Java":
                           Result1={
                                  "program": Directory1+File,
                                  "input": CleanJavaCode(ProgramCode, True), # True to remove spaces and newline in Java
                                  "output": CleanKm3(km3, False)         # False to keep spaces and Indent in the generated OCL
                                  }
                        else:
                           Result1={
                                  "program": Directory1+File,
                                  "input": CleanPythonCode(ProgramCode, False), # True to remove spaces and newline in Java
                                  "output": CleanKm3(km3, False)           # False to keep spaces and Indent in the generated OCL
                                  }
                        Result.append(Result1)
                        count+=1     
            #print("after for")
   print('No of samples=',count)                      
   return Result
# ------------------------------------------------------------------------------
Language="Java"
#Language="Python"

if Language=="Java":
   Directory = ####
   OutputFile=Directory+"JavaOCL.json"  
else:
   Directory=####
   OutputFile=Directory+"PythonOCL.json"   
Result=ConstructClassPairs(Directory, Language)
with open(OutputFile, "w") as f:
   json.dump(Result, f, indent=2)
   print(f"Dataset saved to {OutputFile}")
# ------------------------------------------------------------------------------

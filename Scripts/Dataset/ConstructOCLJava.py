"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 2020-10-05

Description:
    This program constructs OCL specifications from Java programs by invoking AgileUML toolset in order to construct the training dataset.
"""
# ------------------------------------------------------------------------------
import os
import shutil
import subprocess
import glob
import time
# ------------------------------------------------------------------------------
BaseDir = ####'
AgileUMLDir = r'C:\Users\PC\Desktop\AgileUML'
# ------------------------------------------------------------------------------
ProgramFile = os.path.join(AgileUMLDir, 'program.java')
OutputDir = os.path.join(AgileUMLDir, 'output')
BatchFile = os.path.join(AgileUMLDir, 'Batch_Java_OCL.bat')
# ------------------------------------------------------------------------------
StartDir = 1
EndDir = 800
# ------------------------------------------------------------------------------
def ClearOutputDirectory(OutputDirectory, retries=3, delay=1):
   for file in glob.glob(os.path.join(OutputDirectory, '*')):
      attempt = 0
      while attempt < retries:
         try:
            os.remove(file)
            break
         except PermissionError as e:
            time.sleep(delay)
            attempt += 1
      else:
         print(f"Failed to delete {file} after {retries} retries due to PermissionError.")
# ------------------------------------------------------------------------------
def ProcessDirectories(BaseDirectory, AgileUMLDirectory, ProgramFile, BatchFile, OutputDirectory, StartDir, EndDir):
   for DirName in sorted(os.listdir(BaseDirectory)):
      if DirName.isdigit():
         DirNumber = int(DirName)
         if StartDir <= DirNumber <= EndDir:
            DirPath = os.path.join(BaseDirectory, DirName)
            print(DirPath)
            if os.path.isdir(DirPath):
               JavaFiles = [f for f in os.listdir(DirPath) if f.endswith('.java')]
               if not JavaFiles:
                  print(f"No Java files found in {DirPath}")
                  continue
               JavaFilePath = os.path.join(DirPath, JavaFiles[0])
               shutil.copy(JavaFilePath, ProgramFile)
               subprocess.run([BatchFile], check=True)
               
               OldName = r"C:\Users\PC\Desktop\AgileUML\output\cgJava2UML_out.txt"
               NewName = r"C:\Users\PC\Desktop\AgileUML\output\cgJava2UML_out.km3"
               os.rename(OldName, NewName)  
               
               KM3Files = glob.glob(os.path.join(OutputDirectory, '*.km3'))
               for KM3File in KM3Files:
                  shutil.copy(KM3File, DirPath)
               ClearOutputDirectory(OutputDirectory)
# ------------------------------------------------------------------------------
ProcessDirectories(BaseDir, AgileUMLDir, ProgramFile, BatchFile, OutputDir, StartDir, EndDir)
# ------------------------------------------------------------------------------

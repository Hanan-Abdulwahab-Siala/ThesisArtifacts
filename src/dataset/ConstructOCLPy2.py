"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 17-02-2026

Description:
    This program constructs OCL specifications from Python programs by invoking AgileUML toolset in order to construct the training dataset.
"""
# ------------------------------------------------------------------------------
import os
import shutil
import subprocess
import glob
import time
# ------------------------------------------------------------------------------
BaseDir = ####

AgileUMLDir = ####
OutputAgileUML = ####
# ------------------------------------------------------------------------------
ProgramFile = os.path.join(AgileUMLDir, 'program.py')
OutputDir = os.path.join(AgileUMLDir, 'output')
BatchFile = os.path.join(AgileUMLDir, 'Batch_AST_Py.bat')
BatchFile2 = os.path.join(AgileUMLDir, 'NewPyOCL.bat')
# ------------------------------------------------------------------------------
StartDir = 1 
EndDir =700  
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
            if os.path.isdir(DirPath):
               JavaFiles = [f for f in os.listdir(DirPath) if f.endswith('.py')]
               if not JavaFiles:
                  print(f"No Python files found in {DirPath}")
                  continue
               JavaFilePath = os.path.join(DirPath, JavaFiles[0])
               print(JavaFilePath)
               shutil.copy(JavaFilePath, ProgramFile)
               subprocess.run([BatchFile], check=True)
               subprocess.run([BatchFile2], check=True)
               os.chdir(OutputAgileUML)
               TxtFiles = glob.glob('**/*.txt', recursive=True)
               for TxtFile in TxtFiles:
                  if os.path.basename(TxtFile) == 'ast.txt':
                     continue                        
                  KM3File = os.path.splitext(TxtFile)[0] + '.km3'
                  os.rename(TxtFile, KM3File)
               KM3Files = glob.glob(os.path.join(OutputDirectory, '*.km3'))
               for KM3File in KM3Files:
                  shutil.copy(KM3File, DirPath)
               ClearOutputDirectory(OutputDirectory)
# ------------------------------------------------------------------------------
ProcessDirectories(BaseDir, AgileUMLDir, ProgramFile, BatchFile, OutputDir, StartDir, EndDir)
# ------------------------------------------------------------------------------

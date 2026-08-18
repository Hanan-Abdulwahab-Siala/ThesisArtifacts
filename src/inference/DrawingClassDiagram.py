"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 17-02-2026

Description:
    This program generates UML class diagrams using Graphviz.
"""
# ------------------------------------------------------------------------------
#Language="Python"
Language="Java"
DrawDiagram=True

File="Test1"
Path= ####
# ------------------------------------------------------------------------------
import json
import pydot
import os
import subprocess
from IPython.display import Image
# ------------------------------------------------------------------------------
def CheckModifier(modifier):
   if modifier=="public":
      symbol='+'
   elif modifier=="private":
      symbol='-'
   elif modifier=="protected":
      symbol='#'
   else:
      symbol=''
   return symbol
# ------------------------------------------------------------------------------
def GenerateDotFileForClassesAndInterfaces(Path, JsonFile, JsonFile2):
   with open(Path+JsonFile, "r") as file:
      Data = json.load(file)
      if not Data or Data == []:
         DrawDiagram=False
         return
   with open(Path+JsonFile2, "r") as file2:
      Data2 = json.load(file2)  
   existing_names = {item["name"] for item in Data}

   for relation in Data2:
      for key in ["Source", "Target"]:
         name = relation[key]
         if name not in existing_names:
            if relation["Relationship"]=="Realization" and key=="Target":
               ccc="Interface"
            else:
               ccc="Class" 
            Data.append({
               "name": name,
               "Visibility": "",
               "IsStatic": "",
               "IsAbstract": "",
               "superclasses": [],
               "variables": [],
               "methods": [],         
               "ClassInterface": ccc
            })
            existing_names.add(name)
                
   DotText="""digraph G {
    edge [fontname="Helvetica",fontsize=10,labelfontname="arial",labelfontsize=7,color="red"];
    node [fontname="Helvetica",fontsize=10,shape=record,style=filled,fillcolor="white",color="red"];

    graph [ rankdir=BT ]
    node [ shape=none ]\n \n"""

   for item in Data:
      if item.get("ClassInterface") in ["Class", "Interface"]:
         variables = item.get("variables", [])
         constructors = item.get("constructors", [])
         methods = item.get("methods", [])

         item["variables"] = variables
         item["constructors"] = constructors
         item["methods"] = methods
   for item in Data:
      if item["ClassInterface"] in ["Class","Interface"]:
         Name = item["name"]
         Front='\n    ' + Name + ' [ label=<        \n        <table border="0" cellborder="1" cellspacing="0">\n            <tr><td>'
         if item["ClassInterface"] == "Interface":
            ClassesAndInterfaces[Name] = "Interface"
            Front+= '&laquo;interface&raquo;<BR/>'
         else:
            ClassesAndInterfaces[Name] = "Class"
         ClassInterface='<b>' + CheckModifier(item["Visibility"]) + '\\N</b>'
         End='</td></tr>\n'
         if item["IsAbstract"] in ["abstract", "abstractABC"]:
            ClassInterface = '<i>'+ClassInterface+'</i>'
         if item["IsStatic"]=="static":
            ClassInterface = '<u>'+ClassInterface+'</u>'
         DotText=DotText+Front+ClassInterface+End

      Attributes=''
      Operations=''
      if Detailed:
         Variables = item['variables']
         if Variables:
            First=True
            for variable in Variables:
               End='<br align="left"/></td></tr>\n'
               if First:
                  Front='           '+'<tr><td align="left">'
                  First=False
               else:
                  Front=' <br align="left"/>'
                  
               if "Visibility" not in variable or not variable.get("Visibility"):
                  vis1 = "" 
               else:
                  vis1 = variable["Visibility"]               

               if variable['typeprint'].startswith("Set"):
                  typ="Set"
               elif variable['typeprint'].startswith("Sequence"):
                  typ="Sequence"
               elif variable['typeprint'].startswith("Dict"):
                  typ="Dict"
               else:
                  typ=variable['typeprint'] 
               Temp=' '+CheckModifier(vis1) + ' ' + variable['name'] +' : ' + typ+' '

               if variable["IsAbstract"] in ["abstract", "abstractABC"]:
                  Temp= '<i>'+Temp +'</i>'
               if variable["IsStatic"]=="static":
                  Temp= '<u>'+Temp +'</u>'
               Attributes=Attributes+Front+Temp
            Attributes=Attributes+End
         else:
            Temp= '           '+'<tr><td align="left"></td></tr>\n'
            Attributes = Attributes + Temp

         IsConstruct=False
         constructors = item['constructors']
         if constructors:
            IsConstruct=True 
            First=True
            for constructor in constructors:
               End='<br align="left"/></td></tr>\n'
               if First:
                  Front='            '+'<tr><td align="left">'
                  First=False
               else:
                  Front='<br align="left"/>'
               if constructor["Visibility"] != "":
                  Temp=' '+CheckModifier(constructor["Visibility"]) + ' ' + constructor['name'] +'('
               else:
                  Temp=' '+ constructor['name'] +'('
               Pa=''
               Parameters=constructor["parameters"]
               FirstPar=True  

               for Para in Parameters:
                  if FullParameters in ['1','2']:
                     if FirstPar:
                        FirstPar=False
                     else:
                        Pa+=','
                  if FullParameters=='1':
                     Pa=Pa+Para["name"]+':'+Para["typeprint"] 
                  if FullParameters=='2':
                     Pa=Pa+Para["typeprint"] 
               Temp=Temp+Pa+') '

               Operations=Operations+Front+Temp

         methods = item['methods']
         if methods:
            if IsConstruct:
               First=False
            else:   
               First=True
            for method in methods:
               End='<br align="left"/></td></tr>\n'
               if First:
                  Front='            '+'<tr><td align="left">'
                  First=False
               else:
                  Front='<br align="left"/>'
               if method["Visibility"] != "":
                  Temp=' '+CheckModifier(method["Visibility"]) + ' ' + method['name'] +'('
               else:
                  Temp=' '+ method['name'] +'('
               Pa=''
               Parameters=method["parameters"]
               FirstPar=True  

               for Para in Parameters:
                  if FullParameters in ['1','2']:
                     if FirstPar:
                        FirstPar=False
                     else:
                        Pa+=','
                  if FullParameters=='1':
                     Pa=Pa+Para["name"]+':'+Para["typeprint"] 
                  if FullParameters=='2':
                     Pa=Pa+Para["typeprint"] 
               if FullParameters=='1':
                  if isinstance(method['returnType'], list): 
                     rr="[]"
                  else:
                     rr= method['returnType']
                  Temp=Temp+Pa+'): '+ rr+' '
               else:
                  Temp=Temp+Pa+') '

               if method["IsAbstract"] in ["abstract", "abstractABC"]:
                  Temp= '<i>'+Temp +'</i>'
               if method["IsStatic"]=="static":
                  Temp= '<u>'+Temp +'</u>'
               Operations=Operations+Front+Temp
            Operations=Operations+End
         else:
            if IsConstruct:
               Operations=Operations+End
            else:   
               Temp= '            '+'<tr><td align="left"></td></tr>\n'
               Operations = Operations + Temp
      DotText=DotText+' '+Attributes+Operations+'        </table>> ]'
   DotText=DotText+'\n\n'+ GenerateDotFileForRelationships(Path, JsonFile)
   DotText+='}'
   with open(Path+f"{JsonFile[:-4]}.dot", "w") as DotFile:
      DotFile.write(f"""{DotText}""")
   print(f"UML classes and Interfaces are saved as: {Path+f'{JsonFile[:-4]}.dot'}")
# ------------------------------------------------------------------------------
def GenerateDotFileForRelationships(Path, JsonFile):
   with open(Path+f"{JsonFile[:-4]}.REL", 'r') as file:
      RelationData = json.load(file)
   RelationText=""

   for item in RelationData:
      Temp=""
      if item["Relationship"] == "Realization": 
         Temp= 4*' '+item["Source"] + " -> " + item["Target"] + ' [headlabel="", taillabel="", label="", arrowhead="empty", arrowtail="empty", style="dashed", fontname="Helvetica", fontcolor="black", fontsize=10.0, color="red"];\n'
      if item["Relationship"] == "Inheritance": 
         Temp= 4*' '+item["Source"] + " -> " + item["Target"] + ' [headlabel="", taillabel="", label="", arrowhead="empty", arrowtail="empty", style="", fontname="Helvetica", fontcolor="black", fontsize=10.0, color="red"];\n'
      if item["Relationship"] == "Composition": 
         Temp= 4*' '+item["Source"] + " -> " + item["Target"] + ' [headlabel="", taillabel="'+item["Role2"]+4*' ' +item["Multiplicity2"]+'", label="", arrowhead="diamond", arrowtail="empty", style="", fontname="Helvetica", fontcolor="black", fontsize=10.0, color="red"];\n'
      if item["Relationship"] == "Aggregation": 
         Temp= 4*' '+item["Source"] + " -> " + item["Target"] + ' [headlabel="", taillabel="'+item["Role2"]+4*' ' +item["Multiplicity2"]+'", label="", arrowhead="odiamond", arrowtail="empty", style="", fontname="Helvetica", fontcolor="black", fontsize=10.0, color="red"];\n'
      if item["Relationship"] == "Association": 
         if item["Role1"] and item["Role2"] :
            # Bi-association 
            Temp= 4*' '+item["Source"]+ " -> " + item["Target"] + ' [headlabel="'+item["Role1"]+4*' ' +item["Multiplicity1"]+'", taillabel="'+item["Role2"]+4*' ' +item["Multiplicity2"]+'" ,label="", arrowhead="none", arrowtail="empty", style="", fontname="Helvetica", fontcolor="black", fontsize=10.0, color="red"];\n'
         else:
            # Uni-association 
            Temp= 4*' '+item["Source"]+ " -> " + item["Target"] + ' [headlabel="'+item["Role1"]+4*' ' +item["Multiplicity1"]+'", taillabel="'+item["Role2"]+4*' ' +item["Multiplicity2"]+'", label="", arrowhead="vee", arrowtail="empty", style="", fontname="Helvetica", fontcolor="black", fontsize=10.0, color="red"];\n'
      if item["Relationship"] == "Dependency": 
         Temp= 4*' '+item["Source"]+ " -> " + item["Target"] + ' [headlabel="'+item["Role1"]+4*' ' +item["Multiplicity1"]+'", taillabel="", label="", arrowhead="vee", arrowtail="empty", style="dashed", fontname="Helvetica", fontcolor="black", fontsize=10.0, color="red"];\n'
      RelationText=RelationText+Temp
   return RelationText
# ------------------------------------------------------------------------------
while True:
   print("Select your diagram:")
   print("1. Detailed Class Diagram")
   print("2. Outline Class Diagram")
   print("3: Exit")
   Choice = input("Enter your choice (1-3): ")

   if Choice == '1':
      Detailed=True
   if Choice == '2':
      Detailed=False
   if Choice in ['1','2','3']:
      break
   else:
      print("Invalid choice. Please enter a number between 1 and 3.")
# ------------------------------------------------------------------------------
if Choice in ['1','2']:
   UMLFile = "Test1.UML"
   RELFile = "Test1.REL"
   if os.path.exists(Path):
      if Choice=='1':
         print("Select How to Display Methods:")
         print("1. Methods with Parameter Names and Types")
         print("2. Methods with Parameter Types")
         print("3: Methods Only (Default)")

         ChoiceM = input("Enter your choice (1-3): ")
         if ChoiceM == '1':
            FullParameters='1'
         elif ChoiceM == '2':
            FullParameters='2'
         else:
           FullParameters='0'

      FullPath1 = os.path.join(Path, UMLFile)
      FullPath2 = os.path.join(Path, RELFile)
      if os.path.isfile(FullPath1) and os.path.isfile(FullPath2):
         ClassesAndInterfaces = {}       
         if not Path.endswith('\\') and not Path.endswith('/'):
            Path += '\\'
         GenerateDotFileForClassesAndInterfaces(Path, UMLFile, RELFile)
      else:
         print("File does not exist.")
         DrawDiagram=False
   else:
      print("Path does not exist.")
      DrawDiagram=False
# ------------------------------------------------------------------------------
if DrawDiagram:
   if not Path.endswith('\\') and not Path.endswith('/'):
      Path+= '\\'
      
   print("1: PNG, 2: PDF, 3: SVG")
   UserInput = input("Choice (Default 1): ").strip()

   ch = {"1": 1, "2": 2, "3": 3}.get(UserInput, 1)
   if ch==1:
      Command = 'dot -Tpng "{}{}.dot" -o "{}{}.png"'.format(Path, File, Path, File)
      subprocess.run(Command, shell=True)
      print(Path + File + '.png')
      Image(filename=Path + File + '.png')
   elif ch==2:    
      Command = 'dot -Tpdf "{}{}.dot" -o "{}{}.pdf"'.format(Path, File, Path, File)
      subprocess.run(Command, shell=True)
      os.startfile(Path + File + '.pdf')      
   else:
      from IPython.display import Image, SVG, display
      import subprocess
      Command = 'dot -Tsvg "{}{}.dot" -o "{}{}.svg"'.format(Path, File, Path, File)
      subprocess.run(Command, shell=True)
      display(SVG(filename=Path + File + '.svg')) 
else:
   print("No Class Diagram Found ...") 
# ------------------------------------------------------------------------------

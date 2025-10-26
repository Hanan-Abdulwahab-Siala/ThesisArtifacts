"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 2020-10-25

Description:
    Java2JSON parser/abstractor. 
"""
# ------------------------------------------------------------------------------
import os
import re
import json
import javalang
# ------------------------------------------------------------------------------
# I need to choose whether only one program or directory contains many programs.
IWantOnlyOneProgram=False
# ------------------------------------------------------------------------------
def CleanJavaCode(JavaCode, AllCleaning):
   JavaCode = re.sub(r'\/\/.*', '', JavaCode)                            # Remove //
   JavaCode = re.sub(r'\/\*[\s\S]*?\*\/', '', JavaCode)                  # Remove multi-line comments
   if AllCleaning:
      JavaCode = ' '.join(line.strip() for line in JavaCode.split('\n'))
      JavaCode = re.sub(r'\s+', ' ', JavaCode)
   return JavaCode
# ------------------------------------------------------------------------------
def IsRelationshipFound(Relationship, Source, Target, Data):
   for x in Data:
      if 'Relationship' in x and 'Source' in x and 'Target' in x:
         if x['Relationship'] == Relationship and x['Source'] == Source and x['Target'] == Target:
            return True
   return False
# ------------------------------------------------------------------------------
def IsClassOrInterface(name, Data):
   ClassInterfaceNames = [item["name"] for item in Data if item["ClassInterface"] == "Class" or item["ClassInterface"] == "Interface"]
   return name in ClassInterfaceNames
# ------------------------------------------------------------------------------
def ConstructTypePrint(names):
   Result = ""
   End=""
   Complex=False
   for Name in reversed(names):
      if Name in ["ArrayList","HashSet", "List", "HashMap"]: 
         if Name in ["ArrayList", "List"]:
            typp="Sequence" 
         elif Name=="HashSet" :
            typp="Set"  
         else:
            typp="Map"
         Complex=True
         Result=Result+typp+"("
         End=End+')'
      else:
          if Complex:
             if Result[-1] != "(":
                Result = Result+","+Name 
             else:
                Result += Name  
          else:    
             Result += Name
   Result += End
   return Result
# ------------------------------------------------------------------------------
def ExtractNames(Type3):
   Names = []
   type2=''
   typeprint=''
   Pos = 0
   Start=True
   while Pos < len(Type3):
      Index = Type3.find('name=', Pos)
      if Index == -1:
         break
      SIndex = Index+5
      EIndex = Type3.find(',', SIndex)
      if EIndex == -1:
         break
      Name = Type3[SIndex:EIndex]
      Names.append(Name)
      if Start:
         type2=Name
         Start=False
      Pos = EIndex+1
   typeprint=ConstructTypePrint(Names)
   return type2,typeprint
# ------------------------------------------------------------------------------
def CheckVisibility(x):
   if "public" in x:
      return "public"
   elif "private" in x:
      return "private"
   elif "protected" in x:
      return "protected"
   else: return ""
# ------------------------------------------------------------------------------
def GetUMLTypes(x):
   if x=="int":
      return "Integer"
   elif x=="boolean":
      return "Boolean"
   elif x=="float":
      return "Real" 
   elif x=="void":
      return "OclVoid"
   elif x=="double":
      return "Real" 
   elif x=="char":
     return "String"   
   elif x=="long":
      return "Integer" 
   else:
      return x 
# ------------------------------------------------------------------------------
def GetSimpleTypeName(ttt):
   if isinstance(ttt, str):
      return ttt
   while getattr(ttt, 'sub_type', None) is not None:
      ttt = ttt.sub_type
   return ttt.name  
# ------------------------------------------------------------------------------
# Parsering Java Files
# ------------------------------------------------------------------------------
def ParseJavaCode(JavaCode):
   tree = javalang.parse.parse(JavaCode)
   ClassInfo = []
   for path, node in tree:
      if isinstance(node, javalang.tree.ClassDeclaration):
         ClassData = {"ClassInterface": "Class", "name": node.name, "Visibility": "", "IsStatic": "", "IsAbstract": "", "superclass": None, "interfaces": [], "constructors": [], "variables": [], "methods": []}
         if node.modifiers:
            ClassData["IsStatic"] = "static" if "static" in node.modifiers else ""
            ClassData["IsAbstract"] = "abstract" if "abstract" in node.modifiers else ""
            ClassData["Visibility"] = CheckVisibility(node.modifiers)
         if node.extends:
            ClassData["superclass"] = node.extends.name
         if node.implements:
            for interface in node.implements:
               ClassData["interfaces"].append(interface.name)
         for member in node.body:
            if isinstance(member, javalang.tree.ConstructorDeclaration): 
               ConstructorData = {
                  "name": member.name,
                  "Visibility": "",
                  "parameters": [],
               }
               ConstructorData["Visibility"] = CheckVisibility(member.modifiers)    
               
               for param in member.parameters:
                  ParamData = {
                     "typeprint": GetUMLTypes(param.type.name),
                     "name": param.name
                  }
                  ConstructorData["parameters"].append(ParamData)
               ClassData["constructors"].append(ConstructorData) 
               
            if isinstance(member, javalang.tree.FieldDeclaration):
               for declarator in member.declarators:
                  ElementType=""
                  if isinstance(member.type, javalang.tree.ReferenceType) and member.type.arguments:
                     ElementType = member.type.arguments[0].type.name
                  if ElementType != "":
                     type2,typeprint= ExtractNames(str(member.type))
                  else:
                     typeprint=member.type.name
                     type2=ElementType
                  variable = {
                     "name": declarator.name,
                     "IsStatic":"",
                     "IsAbstract": "",
                     "type": member.type.name,
                     "type2": type2,        
                     "type3": str(member.type),
                     "typeprint":GetUMLTypes(typeprint) 
                     }
                  variable["IsStatic"] = "static" if "static" in member.modifiers else ""
                  variable["IsAbstract"] = "abstract" if "abstract" in member.modifiers else ""
                  variable["Visibility"] = CheckVisibility(member.modifiers)

                  ClassData["variables"].append(variable)
            if isinstance(member, javalang.tree.MethodDeclaration):
               ReturnType = ""
               if isinstance(member.return_type, javalang.tree.ReferenceType) and member.return_type.arguments:
                  ReturnType = member.return_type.arguments[0].type.name
               if ReturnType != "":
                  type2,typeprint= ExtractNames(str(member.return_type))
               else:
                  typeprint=member.return_type.name if member.return_type else "OclVoid"
                  type2=ReturnType
               MethodData = {
                  "name": member.name,
                  "Visibility": "",
                  "IsStatic": "",
                  "IsAbstract": "",
                  "returnType": member.return_type.name if member.return_type else "OclVoid",
                  "returnType2": type2,
                  "returnType3": str(member.return_type),
                  "returntypeprint":GetUMLTypes(typeprint),
                  "parameters": [],
                  "localVariables": []
               }
               
               MethodData["IsStatic"] = "static" if "static" in member.modifiers else ""
               MethodData["IsAbstract"] = "abstract" if "abstract" in member.modifiers else ""
               MethodData["Visibility"] = CheckVisibility(member.modifiers)    
               if member.parameters:
                  for parameter in member.parameters:
                     ElementType=""
                     if isinstance(parameter.type, javalang.tree.ReferenceType) and parameter.type.arguments:
                        ElementType = parameter.type.arguments[0].type.name
                     if ElementType != "":
                        type2,typeprint= ExtractNames(str(parameter.type))
                     else:
                        typeprint = GetSimpleTypeName(parameter.type)
                        type2=ElementType
                     ParameterData = {
                        "name": parameter.name,
                        "type": parameter.type.name,
                        "type2": type2, 
                        "type3": str(parameter.type),
                        "typeprint": GetUMLTypes(typeprint)
                     }
                     MethodData["parameters"].append(ParameterData)
               if member.body:
                  for path, child in node.filter(javalang.tree.LocalVariableDeclaration):
                     for declarator in child.declarators:
                        ElementType=""
                        VariableType = child.type
                        if isinstance(VariableType, javalang.tree.ReferenceType) and VariableType.arguments:
                           ElementType = VariableType.arguments[0].type.name
                        if ElementType != "":
                           type2,typeprint= ExtractNames(str(child.type))
                        else:
                           typeprint=child.type.name
                           type2=ElementType
                        LocalVariable = {
                           "name": declarator.name,
                           "type": child.type.name,
                           "type2": type2,
                           "type3": str(child.type),
                           "typeprint": GetUMLTypes(typeprint)
                        }
                        MethodData["localVariables"].append(LocalVariable)
               ClassData["methods"].append(MethodData)
         ClassInfo.append(ClassData)
      if isinstance(node, javalang.tree.InterfaceDeclaration):
         ClassData = {"ClassInterface": "Interface", "name": node.name, "Visibility": "", "IsStatic": "", "IsAbstract": "", "superclass": None, "variables": [], "methods": []}
         if node.modifiers:
            ClassData["IsStatic"] = "static" if "static" in node.modifiers else ""
            ClassData["IsAbstract"] = "abstract" if "abstract" in node.modifiers else ""
            ClassData["Visibility"] = CheckVisibility(node.modifiers)
         if node.extends:
            ClassData["superclass"] = node.extends.name
         for member in node.body:
            if isinstance(member, javalang.tree.FieldDeclaration):
               for declarator in member.declarators:
                  ElementType=""
                  if isinstance(member.type, javalang.tree.ReferenceType) and member.type.arguments:
                     ElementType = member.type.arguments[0].type.name
                  if ElementType != "":
                     type2,typeprint= ExtractNames(str(member.type))
                  else:
                     typeprint=member.type.name
                     type2=ElementType
                  variable = {
                     "name": declarator.name,
                     "IsStatic":"",
                     "IsAbstract": "",
                     "type": member.type.name,
                     "type2": type2,         
                     "type3": str(member.type),
                     "typeprint":GetUMLTypes(typeprint) 
                     }
                  variable["IsStatic"] = "static" if "static" in member.modifiers else ""
                  variable["IsAbstract"] = "abstract" if "abstract" in member.modifiers else ""
                  variable["Visibility"] = CheckVisibility(member.modifiers)
                  ClassData["variables"].append(variable)
            if isinstance(member, javalang.tree.MethodDeclaration):
               ReturnType = ""
               if isinstance(member.return_type, javalang.tree.ReferenceType) and member.return_type.arguments:
                  ReturnType = member.return_type.arguments[0].type.name
               if ReturnType != "":
                  type2,typeprint= ExtractNames(str(member.return_type))
               else:
                  typeprint=member.return_type.name if member.return_type else "OclVoid"
                  type2=ReturnType
               MethodData = {
                  "name": member.name,
                  "Visibility": "",
                  "IsStatic": "",
                  "IsAbstract": "",
                  "returnType": member.return_type.name if member.return_type else "OclVoid",
                  "returnType2": type2,
                  "returnType3": str(member.return_type),
                  "returntypeprint":GetUMLTypes(typeprint),
                  "parameters": [],
                  "localVariables": []
               }
               
               MethodData["IsStatic"] = "static" if "static" in member.modifiers else ""
               MethodData["IsAbstract"] = "abstract" if "abstract" in member.modifiers else ""
               MethodData["Visibility"] = CheckVisibility(member.modifiers)    
               if member.parameters:
                  for parameter in member.parameters:
                     ElementType=""
                     if isinstance(parameter.type, javalang.tree.ReferenceType) and parameter.type.arguments:
                        ElementType = parameter.type.arguments[0].type.name
                     if ElementType != "":
                        type2,typeprint= ExtractNames(str(parameter.type))
                     else:
                        typeprint=parameter.type.name
                        type2=ElementType
                     ParameterData = {
                        "name": parameter.name,
                        "type": parameter.type.name,
                        "type2": type2, 
                        "type3": str(parameter.type),
                        "typeprint": GetUMLTypes(typeprint)
                     }
                     MethodData["parameters"].append(ParameterData)
               if member.body:
                  for statement in member.body:
                     if isinstance(statement, javalang.tree.LocalVariableDeclaration):
                        for declarator in statement.declarators:
                           ElementType=""
                           VariableType = statement.type
                           if isinstance(VariableType, javalang.tree.ReferenceType) and VariableType.arguments:
                              ElementType = VariableType.arguments[0].type.name
                           if ElementType != "":
                              type2,typeprint= ExtractNames(str(statement.type))
                           else:
                              typeprint=statement.type.name
                              type2=ElementType
                           LocalVariable = {
                              "name": declarator.name,
                              "type": statement.type.name,
                              "type2": type2,
                              "type3": str(statement.type),
                              "typeprint": GetUMLTypes(typeprint)
                           }
                           MethodData["localVariables"].append(LocalVariable)
               ClassData["methods"].append(MethodData)
         ClassInfo.append(ClassData)
   return ClassInfo

# ------------------------------------------------------------------------------
# Constructing Relationships Between Classes and Interfaces
# ------------------------------------------------------------------------------
def ConstructRelationshipsBetweenClassesAndInterfaces(Data):
   ClassInfo = []

   for item in Data:
      if item["ClassInterface"] in ["Class","Interface"]:
         Name = item["name"]
         if item["ClassInterface"] == "Class":
            if item["superclass"] is not None:
               if not IsRelationshipFound("Inheritance", Name, item["superclass"], ClassInfo):
                  ClassData = {"Relationship": "Inheritance", "Source": Name, "Target": item["superclass"], "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"","Deleted": False,"Flag": False}
                  ClassInfo.append(ClassData)

            interfaces = item['interfaces']
            if interfaces:
               for interface in interfaces:
                  ClassData = {"Relationship": "Realization", "Source": Name, "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                  ClassData["Target"] = interface
                  if not IsRelationshipFound("Realization", Name, interface, ClassInfo):
                     ClassInfo.append(ClassData)

            variables = item['variables']
            if variables:
               for variable in variables:
                  ClassData = {"Relationship": "Association", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                  if IsClassOrInterface(variable["type"], Data): 
                     ClassData["Source"] = Name
                     ClassData["Target"] = variable["type"]
                     ClassData["Multiplicity1"] = "0..1"

                     Match = re.search(r'dimensions=(\[[^\]]*\])', variable["type3"])
                     if Match:
                        Value = Match.group(1)
                     else:
                        Value = "[]"
                     if Value != "[]":
                        ClassData["Multiplicity1"] = "0..*"

                     ClassData["Role1"] = variable["name"]
                     if not IsRelationshipFound("Association", ClassData["Source"], ClassData["Target"], ClassInfo):
                        ClassInfo.append(ClassData)
                  elif IsClassOrInterface(variable["type2"], Data): 
                     ClassData["Source"] = Name
                     ClassData["Target"] = variable["type2"]
                     ClassData["Multiplicity1"] = "0..*"
                     ClassData["Role1"] = variable["name"]
                     if not IsRelationshipFound("Association", ClassData["Source"], ClassData["Target"], ClassInfo):
                        ClassInfo.append(ClassData)

            methods = item['methods']
            if methods:
               for method in methods:
                  if IsClassOrInterface(method["returnType"], Data):
                     if not IsRelationshipFound("Dependency", Name, method["returnType"], ClassInfo):
                        ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                        ClassData["Source"] = Name
                        ClassData["Target"] = method["returnType"]
                        ClassData["Role1"] = ""
                        ClassInfo.append(ClassData)
                  if method["returnType2"] !="" and IsClassOrInterface(method["returnType2"], Data):
                     if not IsRelationshipFound("Dependency", Name, method["returnType2"], ClassInfo):
                        ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                        ClassData["Source"] = Name
                        ClassData["Target"] = method["returnType2"]
                        ClassData["Role1"] = ""
                        ClassInfo.append(ClassData)
                  if 'parameters' in method:
                     for parameter in method['parameters']:
                        if parameter["type"] and IsClassOrInterface(parameter["type"], Data):
                           if not IsRelationshipFound("Dependency", Name, parameter["type"], ClassInfo):
                              ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                              ClassData["Source"] = Name
                              ClassData["Target"] = parameter["type"]
                              ClassData["Role1"] = parameter["name"]
                              ClassInfo.append(ClassData)
                        if not parameter["type"] and parameter["type2"] and IsClassOrInterface(parameter["type2"], Data):
                           if not IsRelationshipFound("Dependency", Name, parameter["type2"], ClassInfo):
                              ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                              ClassData["Source"] = Name
                              ClassData["Target"] = parameter["type2"]
                              ClassData["Role1"] = parameter["name"]
                              ClassInfo.append(ClassData)

                  if 'localVariables' in method:
                     for localVariable in method['localVariables']:
                        if IsClassOrInterface(localVariable["type"], Data):
                           if not IsRelationshipFound("Dependency", Name, localVariable["type"], ClassInfo):
                              ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                              ClassData["Source"] = Name
                              ClassData["Target"] = localVariable["type"]
                              ClassData["Role1"] = localVariable["name"]
                              ClassInfo.append(ClassData)
                        elif IsClassOrInterface(localVariable["type2"], Data):
                           if not IsRelationshipFound("Dependency", Name, localVariable["type2"], ClassInfo):
                              ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                              ClassData["Source"] = Name
                              ClassData["Target"] = localVariable["type2"]
                              ClassData["Role1"] = localVariable["name"]
                              ClassInfo.append(ClassData)
   return ClassInfo

# ------------------------------------------------------------------------------
def EnhancingRelationship(item, Data):
   ClassInterface1 = item['Source']
   ClassInterface2 = item['Target']
# ------------------------------------------------------------------------------
   if item["Relationship"] == 'Inheritance':
      Data[:] = [
      {**x, 'Deleted': True} if (((x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) or (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1)) and (x["Relationship"] != 'Inheritance')) else x
      for x in Data
      ]
# ------------------------------------------------------------------------------
   if item["Relationship"] == 'Realization':
      Data[:] = [
      {**x, 'Deleted': True} if (((x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) or (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1)) and (x["Relationship"] != 'Realization')) else x
      for x in Data
      ]
# ------------------------------------------------------------------------------
   if item["Relationship"] == 'Aggregation':
      for x in Data:
         if (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1) and (x["Relationship"] == 'Association') and not x['Deleted']:
            if (item['Multiplicity2'] == '1..1' and x['Multiplicity1'] == '0..*'):
               item['Multiplicity2'] = '1..*'
            item['Role2'] = x['Role1']
            x['Deleted'] = True
         if (x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) and (x["Relationship"] == 'Association') and not x['Deleted']:
            item['Multiplicity1'] = x['Multiplicity1']
            item['Role1'] = x['Role1']
            x['Deleted'] = True
         if (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1) and (x["Relationship"] == 'Dependency') and not x['Deleted']:
            x['Deleted'] = True
      Data[:] = [
      {**x, **item} if (x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) and (x["Relationship"] == 'Aggregation') else x
      for x in Data
      ]
      if not item['Deleted']:
         Data[:] = [
         {**x, 'Deleted': True} if (((x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) or (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1)) and (x["Relationship"] == 'Dependency')) else x
         for x in Data
         ]
# ------------------------------------------------------------------------------
   if item["Relationship"] == 'Association':
      for x in Data:
         if (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1) and (x["Relationship"] == 'Association') and not x['Flag']:
            item['Multiplicity2'] = x['Multiplicity1']
            item['Role2'] = x['Role1']
            item['Flag'] = True
      for x in Data:
         if (x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) and (x["Relationship"] == 'Association') and not x['Deleted']:
            x=item
         if ((x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1) and (x["Relationship"] == 'Association')) and not x['Flag']:
            x['Deleted']= True
         if (((x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) or (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1)) and (x["Relationship"] == 'Dependency')):
            x['Deleted']= True
         if x['Source'] == x['Target'] : 
            item['Multiplicity2']=''
            item['Role2']=''
            item['Flag'] = False 
   
# ------------------------------------------------------------------------------
   if item["Relationship"] == 'Dependency':
      if item['Source'] == item['Target']:
        item['Deleted']= True
      else:
         for x in Data:
            if (x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) and (x["Relationship"] == 'Dependency') and not x['Deleted'] and not x['Flag']:
               x['Flag'] = True
            if ((x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1) and (x["Relationship"] == 'Dependency')) and not x['Flag']:
               x['Deleted'] = True
   return Data
# ------------------------------------------------------------------------------
def ExtractClassName(line):
   ClassDeclaration = line.split("class ")
   if len(ClassDeclaration) > 1:
      ClassName = ClassDeclaration[1].split(" ")[0].strip()
      return ClassName
   else:
      return None
# ------------------------------------------------------------------------------
def CheckingAggregation(JavaCode):
   class_stack = []
   inside_class = False
   ClassName = None
   AggregationInfo=[]
   for line in JavaCode.split('\n'):
      if "class" in line:
         ClassName = ExtractClassName(line)
         if class_stack and class_stack[-1]["open"] != class_stack[-1]["close"] and ClassName is not None and class_stack[-1]["name"] is not None:
            AggregationInfo.append({"Relationship": "Aggregation", "Source": ClassName, "Target": class_stack[-1]["name"], "Multiplicity1": "", "Role1":"", "Multiplicity2": "1..1", "Role2":"","Deleted": False, "Flag": True})
         class_stack.append({"name": ClassName, "open": 0, "close": 0})
         inside_class = True
         if "{" in line:
             class_stack[-1]["open"] += 1
         if "}" in line:
            class_stack[-1]["close"] += 1
            inside_class = False
      elif inside_class:
         for char in line:
            if char == "{":
               class_stack[-1]["open"] += 1
            elif char == "}":
               class_stack[-1]["close"] += 1
         if class_stack[-1]["open"] == class_stack[-1]["close"]:
            class_stack.pop()
            inside_class = len(class_stack) > 0

      if 'new' in line:
         pattern = r'new\s+(\w+)\['
         Matching = re.search(pattern, line)
         if Matching:
            ClassName2 = Matching.group(1)
            if ClassName2 is not None and ClassName is not None:
               AggregationInfo.append({"Relationship": "Aggregation", "Source": ClassName2, "Target": ClassName, "Multiplicity1": "", "Role1":"", "Multiplicity2": "1..1", "Role2":"","Deleted": False,"Flag": False})
   return AggregationInfo

# ------------------------------------------------------------------------------
# Processing Directories Containing Many Java Files
# ------------------------------------------------------------------------------
def ReadJavaFiles(Directory):
   ccount=0
   for Root, Dirs, Files in os.walk(Directory):
      for File in Files:
         if File.endswith(".java"):
            Program= Root+"\\"+File
            with open(Program, 'r') as file:
               JavaCode = file.read()
            JavaCode = JavaCode.replace('#//', '_HASH_') 
            ccount+=1
            print("Number of program: ", ccount)
            print("File=",File)
            UMLInfo = ParseJavaCode(CleanJavaCode(JavaCode, True))
            RELInfo=CheckingAggregation(CleanJavaCode(JavaCode, False))
            Info = ConstructRelationshipsBetweenClassesAndInterfaces(UMLInfo)
            RELResult=[]
            for item in RELInfo:  
               if IsRelationshipFound("Association", item["Target"], item["Source"], Info):
                  RELResult.append(item)
               elif item["Flag"]:
                  item["Flag"]=False
                  RELResult.append(item)

            for item in Info:  
               if item not in RELResult:
                  RELResult.append(item)
            Data=RELResult
            for item in Data:
               Data = EnhancingRelationship(item, Data)

            UMLData = json.dumps(UMLInfo, indent=4)
            with open(Root+"\\"+File[:-4]+"UML", 'w') as Outfile1:
               Outfile1.write(UMLData)
            print(f"UML data saved to {Root}/{File[:-4]}UML")
            
            FilteredData = [item for item in Data if not item.get("Deleted")]
            for item in FilteredData:
               item.pop("Deleted", None)
            RELData = json.dumps(FilteredData, indent=4)
            
            with open(Root+"\\"+File[:-4]+"REL", 'w') as Outfile2:
               Outfile2.write(RELData)
            print(f"Relation data saved to {Root}\\{File[:-4]}REL")

if not IWantOnlyOneProgram:
   JavaDir = ####
   ReadJavaFiles(JavaDir)
# ------------------------------------------------------------------------------
# Processing Only One Java Program
# ------------------------------------------------------------------------------
def ReadJavaFile(Directory, File):
   if not os.path.exists(Directory):
      print(f"Directory '{Directory}' does not exist.")
   else:
      FilePath=os.path.join(Directory, File)
      if os.path.exists(FilePath):
         Program=Directory+File

         with open(Program, 'r') as file:
            JavaCode=file.read()
         JavaCode = JavaCode.replace('#//', '_HASH_') 
         UMLInfo=ParseJavaCode(CleanJavaCode(JavaCode, True))
         RELInfo=CheckingAggregation(CleanJavaCode(JavaCode, False))
         Info=ConstructRelationshipsBetweenClassesAndInterfaces(UMLInfo)
         RELResult=[]
         for item in RELInfo:  
            if IsRelationshipFound("Association", item["Target"], item["Source"], Info):
               RELResult.append(item)
            elif item["Flag"]:
               item["Flag"]=False
               RELResult.append(item)

         for item in Info: 
            if item not in RELResult:
               RELResult.append(item)
         Data=RELResult
         for item in Data:
            Data=EnhancingRelationship(item, Data)

         UMLData=json.dumps(UMLInfo, indent=4)
         with open(Directory+File[:-4]+"UML", 'w') as Outfile1:
            Outfile1.write(UMLData)
         print(f"UML data saved to {Directory+File[:-4]+'UML'}")
         
         FilteredData = [item for item in Data if not item.get("Deleted")]
         for item in FilteredData:
            item.pop("Deleted", None)
         RELData = json.dumps(FilteredData, indent=4)
         
         with open(Directory+File[:-4]+"REL", 'w') as Outfile2:
            Outfile2.write(RELData)
         print(f"Relation data saved to {Directory+File[:-4]+'REL'}")
      else:
         print(f"File '{File}' does not exist in directory '{Directory}'.")

if IWantOnlyOneProgram:
   File="Test1.java"
   JavaDir = ####
   ReadJavaFile(JavaDir, File)
# ------------------------------------------------------------------------------

   
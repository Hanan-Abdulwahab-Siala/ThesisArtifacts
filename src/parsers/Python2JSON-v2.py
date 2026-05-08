"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 17-02-2026

Description:
    Python2JSON parser/abstractor. 
"""
# ------------------------------------------------------------------------------
import os
import re
import json
import ast
import types
from typing import List, Dict

ReturnStatementsByFunction = {}
# ------------------------------------------------------------------------------
# I need to choose whether only one program or directory contains many programs.
IWantOnlyOneProgram=False
# ------------------------------------------------------------------------------
def BuildTypeDatabase(code):
   tree = ast.parse(code)
   type_db = {}
   method_param_map = {}
   instance_to_class = {}
   for node in ast.walk(tree):
      if isinstance(node, ast.ClassDef):
         class_name = node.name
         method_param_map[class_name] = {}
         for item in node.body:
            if isinstance(item, ast.FunctionDef):
               params = [arg.arg for arg in item.args.args]
               if params and params[0] in ("self", "cls"):
                  params = params[1:]
               method_param_map[class_name][item.name] = params

   for node in ast.walk(tree):
      if isinstance(node, ast.Assign):
         if isinstance(node.value, ast.Call) and isinstance(node.value.func, ast.Name):
            class_name = node.value.func.id
            for target in node.targets:
               if isinstance(target, ast.Name):
                  instance_to_class[target.id] = class_name

   for node in ast.walk(tree):
      if isinstance(node, ast.Call) and isinstance(node.func, ast.Attribute):
         method_name = node.func.attr
         caller = node.func.value

         class_name = None

         if isinstance(caller, ast.Name):
            class_name = instance_to_class.get(caller.id)

         if isinstance(caller, ast.Name) and caller.id in method_param_map:
            class_name = caller.id

         if not class_name or method_name not in method_param_map.get(class_name, {}):
            continue

         param_names = method_param_map[class_name][method_name]
         if class_name not in type_db:
            type_db[class_name] = {}
         if method_name not in type_db[class_name]:
            type_db[class_name][method_name] = {p: set() for p in param_names}

         for i, arg in enumerate(node.args):
            if i < len(param_names):
               param = param_names[i]
               arg_type = GetArgumentTypeFromNode(arg)
               type_db[class_name][method_name][param].add(arg_type)

         for kw in node.keywords:
            if kw.arg in param_names:
               arg_type = GetArgumentTypeFromNode(kw.value)
               type_db[class_name][method_name][kw.arg].add(arg_type)

   return type_db
# ------------------------------------------------------------------------------
def GetTypeFromDatabase(type_db, class_name, method_name, param_name):
   try:
      types = type_db[class_name][method_name][param_name]
      return ", ".join(sorted(types)) if types else "unknown"
   except KeyError:
      return "unknown"
# ------------------------------------------------------------------------------
def Append2JSON(calls, UMLData, RELData):
   for call in calls:
      cls_name = call["Method"].capitalize()
      ClassData = {"ClassInterface": "Class", 
                   "name": cls_name, "Visibility": "", "IsStatic": "", "IsAbstract": "", "superclasses": [], "interfaces": [], "variables": [], "methods": []}
      if call["DataArgs"]:
         for name, typ in call["DataArgs"]:
            variable = {
                   "name": name,
                   "IsStatic":"",
                   "IsAbstract": "",
                   "type": typ,
                   "typeprint":typ,
                   "Visibility": CheckVisibility(name)
                   }
            ClassData["variables"].append(variable) 
      UMLData.append(ClassData)             
      if call["ObjectArgs"]:            
         for role, cls in call["ObjectArgs"]:
            if not IsRelationshipFound("Association", cls_name, cls, RELData):
               REL = {"Relationship": "Association", "Source": cls_name, "Target": cls, "Multiplicity1": "1", "Role1":role, "Multiplicity2": "*", "Role2":"","Deleted": False,"Flag": False}
               RELData.append(REL)
      AssClass = call["Class"]
      AssMethod= call["Method"]
      for aaa in UMLData:
         if aaa["name"] == AssClass:
            aaa["methods"] = [method for method in aaa["methods"] if method["name"] != AssMethod]
            break
   return UMLData, RELData
# ------------------------------------------------------------------------------  
def CollectMethodSignatures(tree):
   MethodSignatures = {}
   for node in ast.walk(tree):
      if isinstance(node, ast.ClassDef):
         for item in node.body:
            if isinstance(item, ast.FunctionDef):
               Params = [arg.arg for arg in item.args.args if arg.arg != "self"]
               MethodSignatures[item.name] = Params
   return MethodSignatures
# ------------------------------------------------------------------------------
def AnalyzeCalls(tree, method_signatures):
   instantiated = {}  
   processed_methods = set()  
   calls = []

   for node in ast.walk(tree):
      if isinstance(node, ast.Assign):
         if isinstance(node.value, ast.Call) and isinstance(node.value.func, ast.Name):
            class_name = node.value.func.id
            for target in node.targets:
               if isinstance(target, ast.Name):
                  instantiated[target.id] = class_name

      elif isinstance(node, ast.Expr) and isinstance(node.value, ast.Call):
         call_node = node.value
         if isinstance(call_node.func, ast.Attribute):
            method_name = call_node.func.attr
            caller = call_node.func.value
            caller_name = caller.id if isinstance(caller, ast.Name) else None
            args = call_node.args

            if method_name not in method_signatures or caller_name is None:
               continue

            params = method_signatures[method_name]
            if len(params) != len(args):
               continue
            AssClass = instantiated.get(caller_name)
                
            object_args = []
            data_args = []

            for param, arg in zip(params, args):
               if isinstance(arg, ast.Name) and arg.id in instantiated:
                  object_args.append((param, instantiated[arg.id]))
               elif isinstance(arg, ast.Constant):
                  val = arg.value
                  typ = CorrectTypesofReturnValuesConstants(val)
                  data_args.append((param, typ))
               elif isinstance(arg, (ast.List, ast.Tuple, ast.Dict)):
                  try:
                     val = ast.literal_eval(arg)
                     typ = CorrectTypesofReturnValuesConstants(val)
                  except Exception:
                     typ = "unknown"
                  data_args.append((param, typ))
               else:
                  data_args.append((param, "unknown"))

            if caller_name in instantiated:
               caller_object = (caller_name, instantiated[caller_name])
            else:
               caller_object = None

            if len(object_args) < 2:
               continue
            if method_name in processed_methods:
               continue

            processed_methods.add(method_name)
            if caller_object:
               object_args.append(caller_object)

            calls.append({
               "Method": method_name,
               "Class": AssClass,
               "ObjectArgs": object_args,
               "DataArgs": data_args
            })
   return calls
# ------------------------------------------------------------------------------
def GetArgumentTypes(args):
   TypesList = []
   for arg in args:
       try:
          value = ast.literal_eval(arg)
       except (ValueError, SyntaxError):
          value = arg
           
       if isinstance(value, (types.FunctionType, types.BuiltinFunctionType)):
          continue  
       elif isinstance(value, int):
          TypesList.append('Integer')
       elif isinstance(value, str):
          TypesList.append('String')
       elif isinstance(value, float):
          TypesList.append('Real') 
       elif isinstance(value, bool):
          TypesList.append('Boolean')
       elif isinstance(value, complex):
          TypesList.append('Real') 
       elif isinstance(value, list):
          TypesList.append('Sequence') 
       elif isinstance(value, dict):
          TypesList.append('Map') 
       elif isinstance(value, tuple):
          TypesList.append('Tuple')
       elif isinstance(value, set):
          TypesList.append('Set')
       elif isinstance(value, None):
          TypesList.append('OclAny') 
       else:    
          TypesList.append('unknown')
   return TypesList
# ------------------------------------------------------------------------------
def RenameKeyPreserveOrder(d, old_key, new_key):
   items = list(d.items())
   for i, (k, v) in enumerate(items):
      if k == old_key:
         items[i] = (new_key, v)
         break
   d.clear()
   d.update(items)
# ------------------------------------------------------------------------------
def GetSpecificAttributeType(ClassN, Nname, AttributeIndex, ClassAttributesInfo):
   if ClassN in ClassAttributesInfo:
      class_info = ClassAttributesInfo[ClassN]
      if Nname in class_info:
         return class_info[Nname] 
      keys = list(class_info.keys())
      if AttributeIndex != 999:
         expected_name = f"arg{AttributeIndex}"
         if expected_name in class_info:
            RenameKeyPreserveOrder(class_info, expected_name, Nname)
            return class_info[Nname]
         for i, key in enumerate(keys):
            if key == Nname:
               return class_info[Nname]
   return ""
# ------------------------------------------------------------------------------
def CleanPythonCode(PythonCode, AllCleaning):
   PythonCode=re.sub(r'#.*', '', PythonCode)
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
def GetFunctionName(node):
   if isinstance(node, ast.Name):
      return node.id  
   elif isinstance(node, ast.Attribute):
      return node.attr
   elif isinstance(node, ast.Call):
      return GetFunctionName(node.func)
   return None
# ------------------------------------------------------------------------------
def FindReturnStatements(node, FunctionName, localvariables):
   if isinstance(node, ast.Return):
      if isinstance(node.value, ast.Tuple):
         for elt in node.value.elts:
            ReturnType = ""
            ReturnValue = ""
            if isinstance(elt, ast.Call):
               if isinstance(elt.func, ast.Name):
                  ReturnType=GetFunctionName(elt)
                  ReturnValue="call"
            elif isinstance(elt, ast.Name):
               type=GetVariableType(elt.id)
               type=CorrectTypesofReturnValues(elt.id, type, localvariables)
               ReturnValue=elt.id 
               ReturnType =type
            elif isinstance(elt, ast.Constant):
               ReturnValue=elt.value
               ReturnType=CorrectTypesofReturnValuesConstants(elt.value)
            elif isinstance(elt, ast.Dict):
               ReturnType="Map" 
               ReturnValue=""
            elif isinstance(elt, ast.List):
               ReturnType= "Sequence" 
               ReturnValue=""            
            elif isinstance(elt, ast.UnaryOp) and isinstance(elt.op, ast.USub):
               if isinstance(elt.operand, ast.Constant):
                  value = elt.operand.value
               elif isinstance(elt.operand, ast.Num): 
                  value = elt.operand.n
               else:
                  value = None  

               if value is not None:
                  ReturnValue = -value
                  if isinstance(ReturnValue, float):
                     ReturnType = "Real"  
                  else:
                     ReturnType = "Integer"
               else:
                  ReturnValue = ReturnType = "" 
            if FunctionName not in ReturnStatementsByFunction:
               ReturnStatementsByFunction[FunctionName] = []
            if ReturnType != "":
               ReturnStatementsByFunction[FunctionName].append({
                  "ReturnType": ReturnType,
                  "ReturnValue": ReturnValue,
               })
      elif isinstance(node.value, ast.Call):
         ReturnType = GetFunctionName(node.value.func)
         ReturnValue="call"
      elif isinstance(node.value, ast.Name):
         type=GetVariableType(node.value.id)
         type=CorrectTypesofReturnValues(node.value.id, type, localvariables)
         ReturnValue=node.value.id 
         ReturnType =type
      elif isinstance(node.value, ast.Constant):
         ReturnValue=node.value.value 
         ReturnType = CorrectTypesofReturnValuesConstants(node.value.value)
      elif isinstance(node.value, ast.UnaryOp) and isinstance(node.value.op, ast.USub):
         if isinstance(node.value.operand, ast.Constant):
            value = node.value.operand.value
         elif isinstance(node.value.operand, ast.Num): 
            value = node.value.operand.n
         else:
            value = None  

         if value is not None:
            ReturnValue = -value
            if isinstance(ReturnValue, float):
               ReturnType = "Real" 
            else:
               ReturnType = "Integer"
         else:
            ReturnValue = ReturnType = "" 
      elif isinstance(node.value, ast.Dict):
         ReturnType="Map" 
         ReturnValue=""
      elif isinstance(node.value, ast.List):
         ReturnType="Sequence" 
         ReturnValue=""
      elif isinstance(node.value, ast.JoinedStr):
         ReturnType="String"
         ReturnValue=""
          
      if isinstance(node.value, (ast.Constant, ast.USub, ast.Dict, ast.List, ast.Name, ast.Call, ast.JoinedStr)):   
         if FunctionName not in ReturnStatementsByFunction:
            ReturnStatementsByFunction[FunctionName] = []
         if ReturnType != "":
            ReturnStatementsByFunction[FunctionName].append({
               "ReturnType": ReturnType,
               "ReturnValue": ReturnValue,
            })        
   if hasattr(node, 'body') and isinstance(node.body, list):
      for child in node.body:
         FindReturnStatements(child, FunctionName, localvariables)
   if isinstance(node, ast.If):
      for child in node.orelse:
         FindReturnStatements(child, FunctionName, localvariables)
# ------------------------------------------------------------------------------
def IsClassOrInterface(name, Data):
   ClassNames = [item["name"] for item in Data if item["ClassInterface"] == "Class"]
   InterfaceNames = [item["name"] for item in Data if item["ClassInterface"] == "Interface"]
   if name in ClassNames:
      return "Class"
   elif name in InterfaceNames:
      return "Interface"
   else:
      return "NoClassesInterfaces"
# ------------------------------------------------------------------------------
def IsRelationshipFound(Relationship, Source, Target, Data):
   for x in Data:
      if 'Relationship' in x and 'Source' in x and 'Target' in x:
         if x['Relationship'] == Relationship and x['Source'] == Source and x['Target'] == Target:
            return True
   return False
# ------------------------------------------------------------------------------
def ConstructRelationshipsBetweenClassesAndInterfaces(Data, CompositionInfo, AggregationInfo):
   ClassInfo = []
   for item in Data:
      if item["ClassInterface"] in ["Class","Interface"]:
         Name = item["name"]
         if item["ClassInterface"] == "Class":
            superclasses=item['superclasses']
            if superclasses:
               for superclass in superclasses:
                  if IsClassOrInterface(superclass, Data)=="Class":
                     if not IsRelationshipFound("Inheritance", Name, superclass, ClassInfo):
                        ClassData = {"Relationship": "Inheritance", "Source": Name, "Target": superclass, "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"","Deleted": False,"Flag": False}
                        ClassInfo.append(ClassData)
                  elif IsClassOrInterface(superclass, Data)=="Interface":
                     item['superclasses'].remove(superclass)
                     item['interfaces'].append(superclass)
                     if not IsRelationshipFound("Realization", Name, superclass, ClassInfo):
                        ClassData = {"Relationship": "Realization", "Source": Name, "Target": superclass, "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"","Deleted": False,"Flag": False}
                        ClassInfo.append(ClassData)

            interfaces = item['interfaces']
            if interfaces:
               for interface in interfaces:
                  if IsClassOrInterface(interface, Data)=="Interface":
                     if not IsRelationshipFound("Realization", Name, interface, ClassInfo):
                        ClassData = {"Relationship": "Realization", "Source": Name, "Target": interface, "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"","Deleted": False,"Flag": False}
                        ClassInfo.append(ClassData)
                  elif IsClassOrInterface(interface, Data)=="Class":
                    item['superclasses'].append(interface)
                    item['interfaces'].remove(interface)
                    if not IsRelationshipFound("Inheritance", Name, superclass, ClassInfo):
                       ClassData = {"Relationship": "Inheritance", "Source": Name, "Target": interface, "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"","Deleted": False,"Flag": False}
                       ClassInfo.append(ClassData)
            variables = item['variables']
            if variables:   
               for variable in variables:
                  x=variable["type"]
                  if IsClassOrInterface(x, Data)=="Class":
                     ClassData = {"Relationship": "Composition", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":variable["name"], "Deleted": False,"Flag": False }
                     ClassData["Source"] = x
                     ClassData["Target"] = Name
                     ClassInfo.append(ClassData)

            methods = item['methods']
            if methods:
               for method in methods:
                  if 'returnType' in method:
                     for returnType in method['returnType']:
                        if IsClassOrInterface(returnType["ReturnType"], Data) in ['Class', 'Interface']:
                           if not IsRelationshipFound("Dependency", Name, returnType["ReturnType"], ClassInfo):
                              ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                              ClassData["Source"] = Name
                              ClassData["Target"] = returnType["ReturnType"]
                              ClassData["Role1"] = ""
                              ClassInfo.append(ClassData)
                  if 'parameters' in method:
                     for parameter in method['parameters']:
                        if parameter["type"] and IsClassOrInterface(parameter["type"], Data) in ['Class', 'Interface']:
                           if not IsRelationshipFound("Dependency", Name, parameter["type"], ClassInfo):
                              ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                              ClassData["Source"] = Name
                              ClassData["Target"] = parameter["type"]
                              ClassData["Role1"] = parameter["name"]
                              ClassInfo.append(ClassData)
                  if 'localVariables' in method:
                     for localVariable in method['localVariables']:
                        if IsClassOrInterface(localVariable["type"], Data) in ['Class', 'Interface']:
                           if not IsRelationshipFound("Dependency", Name, localVariable["type"], ClassInfo):
                              ClassData = {"Relationship": "Dependency", "Source": "", "Target": "", "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                              ClassData["Source"] = Name
                              ClassData["Target"] = localVariable["type"]
                              ClassData["Role1"] = localVariable["name"]
                              ClassInfo.append(ClassData)
   return ClassInfo
# ------------------------------------------------------------------------------
def EnhancingRelationship(item, Data):
   ClassInterface1 = item['Source']
   ClassInterface2 = item['Target']
# ------------------------------------------------------------------------------
   if item["Relationship"] == 'Composition':
      Data[:] = [
      {**x, 'Deleted': True} if (((x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) or (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1)) and (x["Relationship"] not in ['Composition', 'Inheritance', 'Realization', 'Dependency'])) else x
      for x in Data
      ]
# ------------------------------------------------------------------------------
   if item["Relationship"] == 'Aggregation':
      for x in Data:
         if (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1) and (x["Relationship"] == 'Association') and not x['Deleted']:
            x['Deleted'] = True
         if (x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) and (x["Relationship"] == 'Association') and not x['Deleted']:
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
            item['Role2'] = x['Role1']
            item['Multiplicity2'] = x['Multiplicity1'] 
            item['Flag'] = True
      for x in Data:
         if (x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) and (x["Relationship"] == 'Association') and not x['Deleted']:
            x=item
         if ((x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1) and (x["Relationship"] == 'Association')) and not x['Flag']:
            x['Deleted']= True
         if (((x['Source'] == ClassInterface1 and x['Target'] == ClassInterface2) or (x['Source'] == ClassInterface2 and x['Target'] == ClassInterface1)) and (x["Relationship"] == 'Dependency')):
            x['Deleted']= True
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
def CheckTypeAttribute(Class, Method, i, Data):
   TypeofAttribute=""
   for item in Data:
      if item['name']==Class:
         methods=item['methods']
         for method in methods:
             
            if method['name']==Method:
               Paras=method['parameters']
               no=0
               for Para in Paras:
                  if no==i:
                     TypeofAttribute=Para['appendin']
                  no+=1
   return TypeofAttribute
# ------------------------------------------------------------------------------
def CheckingAggregationAssociation(AggregationInfo, variables, Data, OuterMethodCalls, AssociationInfo):
   RELResult=[]
   if AggregationInfo!=[]: 
      RELResult=AggregationInfo

   for item in variables:
      args=item['Arguments']
      for arg in args:
         if IsClassOrInterface(arg.get('type'), Data) in ['Class', 'Interface']:
            RelationData = {"Relationship": "Aggregation", "Source": arg.get('type'), "Target": item['type'], "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
            if not IsRelationshipFound("Aggregation", arg.get('type'), item['type'], RELResult):
               RELResult.append(RelationData)
   for item in OuterMethodCalls:
      args=item['Arguments']
      i=1
      for arg in args:
         if arg['type']!="unknown":
            TypeofAttribute=CheckTypeAttribute(item['type'],item['method'],i,Data)  
            if TypeofAttribute !="":
               if not IsRelationshipFound("Aggregation", arg['type'], item['type'], RELResult):
                  RelationData = {"Relationship": "Aggregation", "Source": arg['type'], "Target": item['type'], "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                  RELResult.append(RelationData)
            else:
               if not IsRelationshipFound("Association", item['type'], arg['type'], RELResult):
                  RelationData = {"Relationship": "Association", "Source": item['type'], "Target": arg['type'], "Multiplicity1": "", "Role1":arg['name'], "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False }
                  RELResult.append(RelationData)
         i+=1
   RELResult=RELResult+AssociationInfo         
   return RELResult
# ------------------------------------------------------------------------------
def GetSubscriptTypeStr(Para):
   Value = GetTypeStr(Para.value)
   SValue = GetTypeStr(Para.slice.value) if isinstance(Para.slice, ast.Index) else ""
   return f"{Value}[{SValue}]"
# ------------------------------------------------------------------------------
def GetTypeStr(Para):
   if isinstance(Para, ast.Name):
      return Para.id
   elif isinstance(Para, ast.Subscript):
      return GetSubscriptTypeStr(Para)
   elif isinstance(Para, ast.Attribute):
      return f"{Para.value.id}.{Para.attr}"
   else:
      return ast.dump(Para)
# ------------------------------------------------------------------------------
def GetParaType(Para):
   if Para.annotation:
      return GetTypeStr(Para.annotation)
   else:
      return "unknown"
# ------------------------------------------------------------------------------
def ConstructExpressionString(expression):
   if isinstance(expression, ast.BinOp):
      Left = ConstructExpressionString(expression.left)
      Right = ConstructExpressionString(expression.right)
      Operator = ast.dump(expression.op).split()[0]
      return f"({Left} {Operator} {Right})"
   elif isinstance(expression, ast.Name):
      return expression.id
   else:
      return str(expression)
# ------------------------------------------------------------------------------
def GetTypeOnly(type):
   if '=' in type:
      return type.split('=')[0]
   else:
      return type
# ------------------------------------------------------------------------------
def CorrectTypesofReturnValues(name, type, LocalVariables):
   if type=="unknown":
      for i in LocalVariables:
         if i['name']== name:
            return i["type"]
   return type
# ------------------------------------------------------------------------------
def CorrectTypesofReturnValuesConstants(constant):
   if isinstance(constant, str):
      return 'String'
   elif isinstance(constant, int):
      return 'Integer'
   elif isinstance(constant, float):
      return 'Real' 
   elif isinstance(constant, complex):
      return 'Real' 
   elif isinstance(constant, bool):
      return 'Boolean'
   elif isinstance(constant, list):
      return 'Sequence' 
   elif isinstance(constant, dict):
      return 'Map' 
   elif isinstance(constant, tuple):
      return 'Tuple'
   elif isinstance(constant, set):
      return 'Set'
   elif constant is None:
      return 'OclAny' 
   else:
      return 'unknown'
# ------------------------------------------------------------------------------
def ConvertPythonTypeName(type_name):
   mapping = {
        "int": "Integer",
        "float": "Real",
        "str": "String",
        "bool": "Boolean",
        "list": "Sequence",
        "dict": "Map",
        "tuple": "Tuple",
        "set": "Set",
        "None": "OclAny"
   }
   return mapping.get(type_name, type_name)  
# ------------------------------------------------------------------------------
def Check_Doal(xx):
   if xx not in {'time', 'datetime', 'random', 'collections', 'itertools', 'shutil', 'string', 'hashlib', 'heapq', 'bisect', 
 'range', 'len', 'list', 'dict', 'set', 'tuple', 'int', 'str', 'float', 'bool', 'chr', 'ord', 'abs', 'min', 'print', 'sort','join','split','sqrt','deque'
 'max', 'sum', 'zip', 'map', 'filter', 'reduce', 'sorted', 'reversed', 'enumerate', 'all', 'any', 'isinstance', 'issubclass', 'callable', 'open', 
 'round', 'pow'}:
      return True
   else:
      return False
# ------------------------------------------------------------------------------
def GetVariableType(node):
   if isinstance(node, ast.Str):
      return 'String="'+node.s+'"'
   elif isinstance(node, ast.Num):  
      return "Integer="+str(node.s) if isinstance(node.n, int) else "Real="+str(node.s) 
   
   elif isinstance(node, ast.UnaryOp) and isinstance(node.op, ast.USub) and isinstance(node.operand, (ast.Constant, ast.Num)):
      if isinstance(node.operand, ast.Num):
         value = node.operand.n
      else:  
         value = node.operand.value

      negative_value = -value
      if isinstance(negative_value, float):
         return f"Real={negative_value}" 
      else:
         return f"Integer={negative_value}"
   elif isinstance(node, ast.NameConstant):
      if node.value == True:
         return "Boolean=True"
      elif node.value == False:
         return "Boolean=false"
      else:
         return "OclAny=null"  
   elif isinstance(node, ast.List):
      return "Sequence" 
   elif isinstance(node, ast.Call) and isinstance(node.func, ast.Name) and node.func.id == "set":  
      return "Set" 
   elif isinstance(node, ast.Call) and isinstance(node.func, ast.Name) and node.func.id == "dict":  
      return "Map" 
   elif isinstance(node, ast.Call) and isinstance(node.func, ast.Name) and Check_Doal(node.func.id):
      return ast.unparse(node)  
   elif isinstance(node, ast.BinOp):
      left_type = GetVariableType(node.left)
      right_type = GetVariableType(node.right)
      if "Integer" in left_type and "Integer" in right_type :
         return "Integer"
      elif "Real" in left_type or "Real" in right_type: 
         return "Real" 
      else:
         return "unknown"
   elif isinstance(node, ast.Dict): 
      return "Map"  
   elif isinstance(node, ast.Tuple): 
      return "Tuple"       
   elif isinstance(node, ast.Call) and isinstance(node.func, ast.Attribute):
      return node.func.attr
   else:
      return "unknown"
# ------------------------------------------------------------------------------
def CheckInheritance(data):
   for item in data:
      if item['ClassInterface'] == 'Interface' or item['ClassInterface'] == 'class':
         CorrectSuperClasses = [cls for cls in item['superclasses'] if cls in [classN['name'] for classN in data]]
         item['superclasses'] = CorrectSuperClasses
   return data
# ------------------------------------------------------------------------------
def CheckVisibility(x):
   if len(x)==1 and x[0] != "_":
      return 'public'
   elif len(x) == 2:
      if x[0] == "_":
         if x[1] == "_":
           return 'private'
         else:
           return 'protected'
      else:
         return 'public'
   elif len(x) > 2:
      if x[0] == "_":
         if x[1] == "_":
            return 'private'
         else:
            return 'protected'
      else:
         return 'public'
# ------------------------------------------------------------------------------
def CheckInterfaceAndAbstractClasses(UMLInfo):
   
   for item in UMLInfo:
      if item["ClassInterface"]== 'Class':
         countall=0
         countabstract=0
         countstatic=0
         countinit=0
         countstaticattributes=0
         
         for vari in item['variables']:
            if vari["IsStatic"]=="static" :
               countstaticattributes+=1
         if countstaticattributes>0:
            item['IsStatic'] = "static"
         for method in item['methods']:
            if method['IsAbstract']=="abstract":
               countabstract+=1  
            if method['IsStatic']=="static":
               countstatic+=1         
            if method["name"] != "__init__":
               countall+=1
            else:
               countinit+=1
      if countall==countabstract:
         if countinit==0:         
            if item['superclasses'] == []:
               if countstatic==0:
                  item['ClassInterface'] = 'Interface'
                  item['IsAbstract']="" 

                  for method in item['methods']:
                     method['IsAbstract']=""
               else:
                  item['IsStatic'] = "static" 
            else:
               item['IsAbstract']="abstract"
         else:
            if countall != 0: 
               item['IsAbstract']="abstract" 
      else:
         if countabstract>0: 
            item['IsAbstract']="abstract"
   return UMLInfo
# ------------------------------------------------------------------------------
def ConstructRelationships(UMLInfo, variables, AggregationInfo, OuterMethodCalls, CompositionInfo, AssociationInfo):
   UMLInfo=CheckInheritance(UMLInfo) 
   UMLInfo=CheckInterfaceAndAbstractClasses(UMLInfo) 
   Info=ConstructRelationshipsBetweenClassesAndInterfaces(UMLInfo, CompositionInfo, AggregationInfo)
   RELResult= CheckingAggregationAssociation(AggregationInfo, variables, UMLInfo, OuterMethodCalls, AssociationInfo)

   for item in Info:  
      if item not in RELResult:
         RELResult.append(item)
   Data=RELResult
   for item in Data:
      Data = EnhancingRelationship(item, Data)
   return UMLInfo,Data
# ------------------------------------------------------------------------------
def GetArgumentTypeFromNode(arg_node):
   if isinstance(arg_node, ast.UnaryOp) and isinstance(arg_node.op, ast.USub):
      operand = arg_node.operand
      if isinstance(operand, ast.Constant) and isinstance(operand.value, (int, float)):
         val_type = type(-operand.value).__name__
         if val_type == 'int':
            return "Integer"
         elif val_type == 'float':
            return "Real" 
      elif isinstance(operand, ast.Num):  
         val_type = type(-operand.n).__name__
         if val_type == 'int':
            return "Integer"
         elif val_type == 'float':
            return "Real" 

   if isinstance(arg_node, ast.Constant):
       mm = type(arg_node.value).__name__
       if mm == 'int':
          return "Integer"
       elif mm == 'float':
          return "Real" 
       elif mm == "str":
          return "String"
       elif mm == "date":
          return "Date"
       elif mm == "bool":
          return "Boolean"       
       elif mm == "NoneType":
          return "OclAny"  
       else:
          return mm
   elif isinstance(arg_node, ast.List):
      return "Sequence" 
   elif isinstance(arg_node, ast.Tuple):
      return "Tuple"
   elif isinstance(arg_node, ast.Set):
      return "Set"
   elif isinstance(arg_node, ast.Dict):
      return "Map" 
   elif isinstance(arg_node, ast.Call):
      return "Function_Call"
   else:
      return "unknown"
# ------------------------------------------------------------------------------
def ExtractClassInitializations(code):
   ClassAttributesInfo = {}
   tree = ast.parse(code)

   for node in ast.walk(tree):
      if isinstance(node, ast.Call) and isinstance(node.func, ast.Name):
         class_name = node.func.id
         if class_name not in ClassAttributesInfo:
            ClassAttributesInfo[class_name] = {}

         for i, arg in enumerate(node.args):
            arg_type = GetArgumentTypeFromNode(arg)
            ClassAttributesInfo[class_name][f"arg{i}"] = arg_type

         for kw in node.keywords:
            kw_name = kw.arg
            arg_type = GetArgumentTypeFromNode(kw.value)
            ClassAttributesInfo[class_name][kw_name] = arg_type
  
   for node in ast.walk(tree):
      if isinstance(node, ast.ClassDef):
         class_name = node.name
         if class_name not in ClassAttributesInfo:
            ClassAttributesInfo[class_name] = {}
         param_names = []
         for BodyNode in node.body:
            if isinstance(BodyNode, ast.FunctionDef) and BodyNode.name == "__init__":
               for arg in BodyNode.args.args:
                  if arg.arg != "self":
                     param_names.append(arg.arg)

               arg_names = [arg.arg for arg in BodyNode.args.args[1:]]  
               defaults = BodyNode.args.defaults
               default_values = [None] * (len(arg_names) - len(defaults)) + defaults
               default_map = {}
               for arg, default in zip(arg_names, default_values):
                  default_map[arg]=GetArgumentTypeFromNode(default)  
                  if default_map[arg] not in ["", "unknown"]:
                     ClassAttributesInfo[class_name][arg] = default_map[arg]   

         for i, real_name in enumerate(param_names):
            generic_name = f"arg{i}"
            if generic_name in ClassAttributesInfo[class_name]:
               ClassAttributesInfo[class_name][real_name] = ClassAttributesInfo[class_name].pop(generic_name)

   return ClassAttributesInfo
# ------------------------------------------------------------------------------
def IterStatements(statements):
   for stmt in statements:
      yield stmt
      if isinstance(stmt, (ast.If, ast.For, ast.While, ast.With, ast.Try)):
         for child in IterStatements(stmt.body):
            yield child
         if hasattr(stmt, 'orelse'):
            for child in IterStatements(stmt.orelse):
               yield child
         if hasattr(stmt, 'finalbody'):
            for child in IterStatements(stmt.finalbody):
               yield child
# ------------------------------------------------------------------------------
def ClassifyRelationship(annotation):
   if isinstance(annotation, ast.Name):
      return annotation.id, "1..1"
   if isinstance(annotation, ast.Subscript):
      container = GetName(annotation.value)
      inner = annotation.slice
      if container == "Optional":
         if isinstance(inner, ast.Name):
            return inner.id, "optional"
      if container in {"list", "set", "List", "Set"}:
         if isinstance(inner, ast.Name):
            return inner.id, "1..*"
      if container in {"dict", "Dict"}:
         if isinstance(inner, ast.Tuple) and len(inner.elts) == 2:
            value_type = inner.elts[1]
            if isinstance(value_type, ast.Name):
               return value_type.id, "1..*"
      if isinstance(inner, ast.Subscript):
         return ClassifyRelationship(inner)
   return None, None
# ------------------------------------------------------------------------------
def GetName(node):
   if isinstance(node, ast.Name):
      return node.id
   if isinstance(node, ast.Attribute):
      return node.attr
   return None
# ------------------------------------------------------------------------------
def IsSelfAttribute(target):
   return (
      isinstance(target, ast.Attribute)
      and isinstance(target.value, ast.Name)
      and target.value.id == "self"
   )
# ------------------------------------------------------------------------------
def GetAttributeName(target):
   if isinstance(target, ast.Attribute):
      return target.attr
   if isinstance(target, ast.Name):
      return target.id
   return None
# ------------------------------------------------------------------------------
# Parsing
# ------------------------------------------------------------------------------
def ParsePythonCode(PythonCode):
   ClassAttributesInfo = ExtractClassInitializations(PythonCode)  
   type_db = BuildTypeDatabase(PythonCode)
   tree = ast.parse(PythonCode)
   ClassInfo =[]
   variables=[]
   AssociationInfo=[]
   AggregationInfo=[]
   CompositionInfo=[]
   OuterMethodCalls=[]
   for node in ast.walk(tree):
      if isinstance(node, ast.ClassDef):
         ClassData = {"ClassInterface": "Class", 
                         "name": node.name, "Visibility": "", "IsStatic": "", "IsAbstract": "", "superclasses": [], "interfaces": [], "variables": [], "methods": []}
         OrderAttribute=0
         if node.bases:
            bases = [base.id for base in node.bases if isinstance(base, ast.Name) and base.id != 'ABC']
            ClassData["superclasses"].extend(bases)
         
         for BodyNode in node.body:
            if isinstance(BodyNode, ast.FunctionDef) and BodyNode.name == "__init__":
               for statement in IterStatements(BodyNode.body):
                  if isinstance(statement, ast.Assign):
                     for target in statement.targets:
                        if (isinstance(target, ast.Attribute) and 
                           isinstance(target.value, ast.Name) and 
                           target.value.id == "self"):
                           Attributes = GetVariableType(statement.value)
                           type = GetTypeOnly(Attributes)
                           if type in ["", "unknown"]:
                              newtype = GetSpecificAttributeType(node.name, target.attr, 999, ClassAttributesInfo)
                              if newtype not in ["", "unknown"]:
                                 type = newtype

                           if Attributes in ["", "unknown"] and type not in ["", "unknown"]:
                              Attributes = type

                           if "(" in type:
                              type = type.split("(", 1)[0]

                           variable = {
                              "name": target.attr,
                              "IsStatic": "",
                              "IsAbstract": "",
                              "type": type,
                              "typeprint": Attributes,
                              "Visibility": CheckVisibility(target.attr)
                           }
                           if "(" in Attributes:
                              variable['typeprint'] = type + '=new ' + Attributes 
                              
                           found = False
                           for existing in ClassData["variables"]:
                              if existing["name"] == target.attr:   
                                 found = True
                                 if existing["type"] != type:     
                                    existing["type"] = "OclAny"
                                    existing["typeprint"] = "OclAny"
                                 break

                           if not found:
                              ClassData["variables"].append(variable)                              
               for stmt in BodyNode.body:
                  if isinstance(stmt, ast.AnnAssign):
                     if IsSelfAttribute(stmt.target):
                        related, relation_type = ClassifyRelationship(stmt.annotation)
                        if related:
                           if stmt.value is not None:
                              Attributes = GetVariableType(stmt.value)
                           else:
                              Attributes = "unknown"
                           type = GetTypeOnly(Attributes)

                           if type in ["", "unknown"]:
                              newtype = GetSpecificAttributeType(node.name, stmt.target.attr, 999, ClassAttributesInfo)
                              if newtype not in ["", "unknown"]:
                                 type = newtype

                           if Attributes in ["", "unknown"] and type not in ["", "unknown"]:
                              Attributes = type

                           if "(" in type:
                              type = type.split("(", 1)[0]

                           variable = {
                              "name": stmt.target.attr,
                              "IsStatic": "",
                              "IsAbstract": "",
                              "type": type,
                              "typeprint": Attributes,
                              "Visibility": CheckVisibility(stmt.target.attr)
                           }
                           if "(" in Attributes:
                              variable['typeprint'] = type + '=new ' + Attributes 
                           found = False
                           for existing in ClassData["variables"]:
                              if existing["name"] == stmt.target.attr:   
                                 found = True
                                 if existing["type"] != type:     
                                    existing["type"] = "OclAny"
                                    existing["typeprint"] = "OclAny"
                                 break
                           if related in ["str", "int", "float", "complex", "bool"]: 
                              if variable['typeprint'] in ["Sequence", "Map", "Tuple", "Set"]:
                                 variable['typeprint']=variable['typeprint']+"("+ConvertPythonTypeName(related)+")" 
                              if not found:
                                 ClassData["variables"].append(variable)                              
                           else:
                              REL = {"Relationship": "Association", "Source": node.name, "Target": related, "Multiplicity1": relation_type, "Role1":stmt.target.attr, "Multiplicity2": "", "Role2":"","Deleted": False,"Flag": False}
                              AssociationInfo.append(REL)
            if isinstance(BodyNode, ast.AnnAssign):
               related, relation_type = ClassifyRelationship(BodyNode.annotation)
               if related:
                  if BodyNode.value is not None:
                     Attributes = GetVariableType(BodyNode.value)
                  else:
                     Attributes = "unknown"
                  bb=GetAttributeName(BodyNode.target)
                  type = GetTypeOnly(Attributes)

                  if type in ["", "unknown"]:
                     newtype = GetSpecificAttributeType(node.name, bb, 999, ClassAttributesInfo)
                     if newtype not in ["", "unknown"]:
                        type = newtype

                  if Attributes in ["", "unknown"] and type not in ["", "unknown"]:
                     Attributes = type

                  if "(" in type:
                     type = type.split("(", 1)[0]

                  variable = {
                     "name": bb,
                     "IsStatic": "Static",
                     "IsAbstract": "",
                     "type": type,
                     "typeprint": Attributes,
                     "Visibility": CheckVisibility(bb)
                  }
                  if "(" in Attributes:
                     variable['typeprint'] = type + '=new ' + Attributes 
                  found = False
                  for existing in ClassData["variables"]:
                     if existing["name"] == bb:   
                        found = True
                        if existing["type"] != type:     
                           existing["type"] = "OclAny"
                           existing["typeprint"] = "OclAny"
                        break
                  if related in ["str", "int", "float", "complex", "bool"]: 
                     if variable['typeprint'] in ["Sequence", "Map", "Tuple", "Set"]:
                        variable['typeprint']=variable['typeprint']+"("+ConvertPythonTypeName(related)+")" 
                     if not found:
                        ClassData["variables"].append(variable)                              
                  else:
                     REL = {"Relationship": "Association", "Source": node.name, "Target": related, "Multiplicity1": relation_type, "Role1":bb, "Multiplicity2": "", "Role2":"","Deleted": False,"Flag": False}
                     AssociationInfo.append(REL)

            if isinstance(BodyNode, ast.Assign):
               for target in BodyNode.targets:
                  if isinstance(target, ast.Name):
                      Attributes=GetVariableType(BodyNode.value)
                      type=GetTypeOnly(Attributes)
                      if type.find('(') != -1:
                         i=type.index('(')
                         type=type[:i]
                      variable = {
                         "name": target.id,
                         "IsStatic":"static", 
                         "IsAbstract": "",
                         "type": type,
                         "typeprint":Attributes,
                         "Visibility": CheckVisibility(target.id)
                      }
                      ClassData["variables"].append(variable)
                
            if isinstance(BodyNode, ast.FunctionDef): 
               if BodyNode.name in ["__init__", "__str__"]:
                  Vis1=""
               else:
                  Vis1= CheckVisibility(BodyNode.name)
               MethodData = {
                  "name": BodyNode.name,
                  "Visibility": Vis1, 
                  "IsStatic": "",
                  "returnType": [],
                  "IsAbstract": "",
                  "IsPass":"",
                  "parameters": [],
                  "localVariables": [],
                  "functions":[],
                  "selfAttributes":[],
                  "has_stmt":""
               }
               
               if any(isinstance(decorator, ast.Name) and decorator.id == 'classmethod' for decorator in BodyNode.decorator_list):
                  MethodData["IsStatic"]="static"
                  ClassData["IsStatic"]="static" 
               elif any(isinstance(decorator, ast.Name) and decorator.id == 'staticmethod' for decorator in BodyNode.decorator_list):
                  MethodData["IsStatic"]="static" 
                  ClassData["IsStatic"]="static" 
               elif any((isinstance(decorator, ast.Name) and decorator.id == 'abstractmethod') or (isinstance(decorator, ast.Attribute) and decorator.attr == 'abstractmethod') for decorator in BodyNode.decorator_list):
                  MethodData["IsAbstract"] = "abstract"     
                  ClassData["IsAbstract"] = "abstract" 
               for arg in BodyNode.args.args:
                  ParameterData = {
                  "name": "",
                  "type": "",
                  "typeprint": "",
                  "appendin":""
                  }
                  ParaType = GetParaType(arg)
                  ParameterData["name"]=arg.arg
                  ParameterData["type"]=ParaType
                  ParameterData["typeprint"]=ParaType
                  if arg.arg=="self":
                     ParameterData["type"]="self"
                     ParameterData["typeprint"]="self"
                  if BodyNode.name=="__init__":   
                     if ParameterData["type"]=="" or ParameterData["type"]=="unknown":
                        newtype= GetSpecificAttributeType(node.name, arg.arg, OrderAttribute, ClassAttributesInfo)

                        if newtype not in ["","unknown"]:
                           ParameterData["type"]=newtype 
                     
                     if ParameterData["typeprint"] in ["","unknown"] and ParameterData["type"] not in ["","unknown"]:
                        ParameterData["typeprint"]=ParameterData["type"] 
                     if arg.arg!="self":   
                        OrderAttribute=OrderAttribute+1 
                  if ParameterData["typeprint"] in ["","unknown"]:  
                     ParameterData["typeprint"]=ParameterData["type"]=GetTypeFromDatabase(type_db, node.name, BodyNode.name, arg.arg)  

                  MethodData["parameters"].append(ParameterData)
               if BodyNode.args.vararg:
                  ParameterData = {
                  "name": "",
                  "type": "",
                  "typeprint": "",
                  "appendin":""
                  }
                  ParaType = GetParaType(BodyNode.args.vararg)
                  ParameterData["name"]="*"+BodyNode.args.vararg.arg
                  ParameterData["type"]=ParaType
                  ParameterData["typeprint"]=ParaType
                  MethodData["parameters"].append(ParameterData)
               for kwarg in BodyNode.args.kwonlyargs:
                  ParameterData = {
                  "name": "",
                  "type": "",
                  "typeprint": "",
                  "appendin":""
                  }
                  
                  ParaType = GetParaType(kwarg)
                  ParameterData["name"]=kwarg.arg
                  ParameterData["type"]=ParaType
                  ParameterData["typeprint"]=ParaType
                  MethodData["parameters"].append(ParameterData)
               if BodyNode.args.kwarg:
                  ParameterData = {
                  "name": "",
                  "type": "",
                  "typeprint": "",
                  "appendin":""
                  }
                  ParaType = GetParaType(BodyNode.args.kwarg)
                  ParameterData["name"]="**"+BodyNode.args.kwarg.arg
                  ParameterData["type"]=ParaType
                  ParameterData["typeprint"]=ParaType
                  MethodData["parameters"].append(ParameterData)
               
               mm=BodyNode.body
               Statements = (ast.If, ast.For, ast.While, ast.Try, ast.With, ast.Return, ast.Raise, ast.Break, ast.Continue, ast.Expr, ast.Assert)  
               if any(isinstance(stmt, Statements) for stmt in mm):
                   MethodData["has_stmt"]="Yes"
               if all(isinstance(xx, ast.Pass) or 
                  (isinstance(xx, ast.Expr) and isinstance(xx.value, ast.Constant) and xx.value.value is Ellipsis)
                  for xx in mm):
                  if MethodData["IsAbstract"] != "abstract": 
                     MethodData["IsPass"]="True"   
               for statement in BodyNode.body:                 
                  if not isinstance(statement, (ast.Pass, ast.Import)) and hasattr(statement, 'value') and isinstance(statement.value, ast.Call):    
                     FunctionNode = statement.value.func
                     if isinstance(FunctionNode, ast.Name):
                        FunctionName = FunctionNode.id
                        if FunctionName.find('(') != -1:
                           i=FunctionName.index('(')
                           FunctionName=FunctionName[:i]
                        Expression = ast.unparse(statement.value)
                        Found=False
                        for item in MethodData["localVariables"]:
                           if item["type"] == FunctionName:
                              Found = True
                              break
                                                                              
                        if not Found and Check_Doal(FunctionName) and (FunctionName != BodyNode.name):
                           Function = {
                           "name": FunctionName,
                           "AllFunction": Expression
                           }
                           MethodData["functions"].append(Function)
                     elif isinstance(FunctionNode, ast.Attribute):
                        if Check_Doal(FunctionNode.attr) and (FunctionNode.attr != BodyNode.name): 
                           Function = {
                              "name": FunctionNode.attr,
                              "AllFunction": node.name
                              }
                           MethodData["functions"].append(Function)                

                  for sub_node in node.body:
                     if isinstance(sub_node, ast.ClassDef):  
                        if not IsRelationshipFound("Aggregation", sub_node.name, node.name, AggregationInfo):
                           RelationData = {"Relationship": "Aggregation", "Source": sub_node.name, "Target": node.name, "Multiplicity1": "", "Role1":"", "Multiplicity2": "", "Role2":"", "Deleted": False,"Flag": False}
                           AggregationInfo.append(RelationData)
                        break


                  if (not isinstance(statement, (ast.Pass, ast.Return, ast.Expr, ast.Import, ast.If, ast.For, ast.While, ast.AsyncFor, ast.AugAssign, ast.Global))):
                     target = None
                     if isinstance(statement, ast.Assign):
                        if statement.targets:
                           target = statement.targets[0]
                     elif isinstance(statement, ast.AnnAssign):
                        target = statement.target
                     if (isinstance(target, ast.Attribute) and BodyNode.name != "__init__" and isinstance(target.value, ast.Name) and target.value.id == "self"
                     and isinstance(target.attr, str)):
                        MethodData["selfAttributes"].append(target.attr)
                
               ReturnStatementsByFunction[BodyNode.name] = []
               FindReturnStatements(BodyNode, BodyNode.name, MethodData["localVariables"]) 

               for function_name, statements in ReturnStatementsByFunction.items():
                  if function_name==BodyNode.name:
                     for entry in statements:
                        MethodData["returnType"].append(entry) 
               if isinstance(BodyNode, ast.FunctionDef) and BodyNode.name != "__init__":
                  for inner_node in ast.walk(BodyNode):
                     if isinstance(inner_node, ast.Expr) and isinstance(inner_node.value, ast.Call): 
                        call = inner_node.value
                        if isinstance(call.func, ast.Attribute) and call.func.attr == 'append':
                           if isinstance(call.func.value, ast.Attribute):  
                              IsAttribute=False
                              for item in ClassData["variables"]:
                                 if item['name']==call.func.value.attr:
                                    IsAttribute=True
                              if IsAttribute:
                                 for arg in call.args:
                                    if isinstance(arg, ast.Name):
                                       for item in MethodData["parameters"]:
                                          if item['name']==arg.id:
                                             item['appendin']=call.func.value.attr
                    
                     if isinstance(inner_node, ast.Assign) and isinstance(inner_node.targets[0], ast.Name): 
                        Found=False
                        for item in MethodData["parameters"]:
                           if item["name"] == inner_node.targets[0].id:
                              Found = True
                              break
                        if not Found:   
                           TypeL=GetVariableType(inner_node.value)
                           type=GetTypeOnly(TypeL)
                           if type.find('(') != -1:
                              i=type.index('(')
                              type= type[:i]
                           Variable = {
                              "name": inner_node.targets[0].id,
                              "type": type,
                              "typeprint": TypeL
                           }
                           MethodData["localVariables"].append(Variable)
               for subnode in ast.walk(BodyNode):  
               
                  if isinstance(subnode, ast.Call):
                     if isinstance(subnode.func, ast.Attribute): 
                        if subnode.func.attr == "append": 
                           if subnode.args:
                              arg = subnode.args[0]
                              if isinstance(arg, ast.Call):
                                 if isinstance(arg.func, ast.Name): 
                                    source_class = arg.func.id 
                                    if not IsRelationshipFound("Aggregation", source_class,node.name, AggregationInfo):
                                       RelationData = {"Relationship": "Aggregation", "Source": source_class, "Target": node.name, "Multiplicity1": "1", "Role1":"", "Multiplicity2": "0..*", "Role2":"", "Deleted": False,"Flag": False} 
                                       AggregationInfo.append(RelationData)
               ClassData["methods"].append(MethodData)
         ClassInfo.append(ClassData)

      if isinstance(node, ast.Assign):
         if isinstance(node.value, ast.Call) and isinstance(node.value.func, ast.Name):
            ObjectName="" 
            if isinstance(node.targets[0], ast.Name):
               ObjectName = node.targets[0].id
            elif isinstance(node.targets[0], ast.Attribute):
               ObjectName = node.targets[0].attr
            elif isinstance(node.targets[0], ast.Subscript):
               if isinstance(node.targets[0].value, ast.Name):
                  ObjectName=node.targets[0].value.id
               elif isinstance(node.targets[0].value, ast.Attribute):
                  ObjectName=node.targets[0].value.attr
            type=node.value.func.id
            if type.find('(') != -1:
               i=type.index('(')
               type=type[:i]
            if IsClassOrInterface(type, ClassInfo) in ['Class', 'Interface']:
               typeprint= node.value.func.id
            else:
              typeprint=GetVariableType(node.value)
              type=GetTypeOnly(typeprint)
            variable = {
               "name": ObjectName,
               "type": type,
               "typeprint": typeprint,
               "Arguments":[]
            }
            if node.value.args:
               for arg in node.value.args:
                  if isinstance(arg, ast.Name):
                     type=""
                     temp=arg.id
                     if temp.find('(') != -1:
                        i=temp.index('(')
                        temp=temp[:i]
                     if IsClassOrInterface(temp, ClassInfo) in ['Class', 'Interface']:
                        type= temp 
                     else:
                        type = next((item.get('type') for item in variables if item.get('name') == arg.id), "")
                     Arguments={
                        "name":arg.id,
                        "type":type
                     }
                     variable["Arguments"].append(Arguments)
               variables.append(variable)
            else:
               variables.append(variable)

      if isinstance(node, ast.Call):
         if isinstance(node.func, ast.Attribute) and isinstance(node.func.value, ast.Name):
            ObjectName=node.func.value.id
            MethodName=node.func.attr
            Arguments=[arg.id for arg in node.args if isinstance(arg, ast.Name)]
            OuterMethodCall = {"name": ObjectName, "type": "", "method": MethodName, "Arguments":[]}
            type=""
            temp=ObjectName
            if temp.find('(') != -1:
               i=temp.index('(')
               temp=temp[:i]
            if IsClassOrInterface(temp, ClassInfo) in ['Class', 'Interface']:
               type= temp
            else:
               type = next((item.get('type') for item in variables if item.get('name') == temp), "")
            OuterMethodCall['type']=type           
            if type != "":
               CountArgClass=0 
               for arg in Arguments:
                  type=""
                  temp=arg
                  if temp.find('(') != -1:
                     i=temp.index('(')
                     temp=temp[:i]
                  if IsClassOrInterface(temp, ClassInfo) in ['Class', 'Interface']:
                     type=temp
                  else:
                     type = next((item.get('type') for item in variables if item.get('name') == temp), "")
                     CountArgClass+=1 
                  if type=="":
                     Arguments={
                        "name":arg,
                        "type":"unknown"
                     }
                  else:
                     Arguments={
                        "name":arg,
                        "type":type
                     }
                  OuterMethodCall["Arguments"].append(Arguments)
            if OuterMethodCall["Arguments"]!=[] and CountArgClass < 2: 
               OuterMethodCalls.append(OuterMethodCall)
   return ClassInfo, variables, AggregationInfo, OuterMethodCalls, CompositionInfo, AssociationInfo

# ------------------------------------------------------------------------------
# Processing Directories Containing Many Python Files
# ------------------------------------------------------------------------------
def ReadPythonFiles(Directory):
   ccount=0
   for Root, Dirs, Files in os.walk(Directory):
      for File in Files:
         if File.endswith(".py"):
            Program=Root+"\\"+File
            print(Program)
            ccount+=1
            with open(Program, 'r') as file:
               PythonCode = file.read()
              
            UMLInfo, variables, AggregationInfo, OuterMethodCalls, CompositionInfo, AssociationInfo = ParsePythonCode(PythonCode)
            UMLData, RELData= ConstructRelationships(UMLInfo, variables, AggregationInfo, OuterMethodCalls, CompositionInfo, AssociationInfo)

            tree = ast.parse(PythonCode)
            MethodSignatures = CollectMethodSignatures(tree)
            Calls  = AnalyzeCalls(tree, MethodSignatures)
            UMLData, RELData = Append2JSON(Calls, UMLData, RELData)
            
            FilteredData = [item for item in RELData if not item.get("Deleted")]
            for item in FilteredData:
               item.pop("Deleted", None)
            UMLData = json.dumps(UMLData, indent=4)
            RELData = json.dumps(FilteredData, indent=4)
            with open(Root+"\\"+File[:-2]+"UML", 'w') as Outfile1:
               Outfile1.write(UMLData)
            print(f"UML data saved to {Root}\{File[:-4]}.UML")
            with open(Root+"\\"+File[:-2]+"REL", 'w') as Outfile2:
               Outfile2.write(RELData)
            print(f"Relation data saved to {Root}\{File[:-4]}.REL")
    
# ------------------------------------------------------------------------------
# Processing Only One Python Program
# ------------------------------------------------------------------------------
def ReadPythonFile(Path, File):
   if not os.path.exists(Path):
      print(f"Directory '{Path}' does not exist.")
   else:
      FilePath = os.path.join(Path, File)
      if os.path.exists(FilePath):
         Program= Path+File
         with open(Program, 'r') as file:
            PythonCode = file.read()
         UMLInfo, variables, AggregationInfo, OuterMethodCalls, CompositionInfo, AssociationInfo = ParsePythonCode(PythonCode)
         UMLData, RELData= ConstructRelationships(UMLInfo, variables, AggregationInfo, OuterMethodCalls, CompositionInfo, AssociationInfo)
         tree = ast.parse(PythonCode)
         MethodSignatures = CollectMethodSignatures(tree)
         Calls = AnalyzeCalls(tree, MethodSignatures)
         UMLData, RELData = Append2JSON(Calls, UMLData, RELData)
         FilteredData = [item for item in RELData if not item.get("Deleted")]
         for item in FilteredData:
            item.pop("Deleted", None)
         UMLData = json.dumps(UMLData, indent=4)
         RELData = json.dumps(FilteredData, indent=4)
         with open(Path+File[:-2]+"UML", 'w') as Outfile1:
            Outfile1.write(UMLData)
         print(f"UML data saved to {Path+File[:-4]+'UML'}")
         with open(Path+File[:-2]+"REL", 'w') as Outfile2:
            Outfile2.write(RELData)
         print(f"Relation data saved to {Path+File[:-4]+'REL'}")
      else:
         print(f"File '{File}' does not exist in directory '{Path}'.")
# ------------------------------------------------------------------------------
if IWantOnlyOneProgram:
   File= ####
   Path = ####
   ReadPythonFile(Path, File)
else:
   PythonDir = ####
   ReadPythonFiles(PythonDir)
# ------------------------------------------------------------------------------

"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 2020-10-05

Description:
    This program compares the outputs from the LLM4Models LLM and the output from Java2JSON and Python2JSON parsers.
"""
# ------------------------------------------------------------------------------
import json
import os
import csv
import hashlib
import matplotlib.pyplot as plt
from typing import List, Dict, Tuple
import numpy as np
from openpyxl import Workbook
# ------------------------------------------------------------------------------
Directory = ####
Language="Java"
#Language="Python"
# ------------------------------------------------------------------------------
def PlotGraphs(results):
   categories = list(results.keys())
   precisions = [results[cat]['precision'] for cat in categories]
   recalls = [results[cat]['recall'] for cat in categories]
   f1s = [results[cat]['f1'] for cat in categories]

   x = range(len(categories))
   width = 0.25

   plt.figure(figsize=(10, 6))
   plt.bar([i - width for i in x], precisions, width=width, label='Precision')
   plt.bar(x, recalls, width=width, label='Recall')
   plt.bar([i + width for i in x], f1s, width=width, label='F1 Score')

   plt.xticks(x, [cat.capitalize() for cat in categories])
   plt.ylim(0, 1.1)
   plt.ylabel("Score")
   plt.title("Evaluation Metrics by Category")
   plt.legend()
   plt.grid(axis='y', linestyle='--', alpha=0.7)
   plt.tight_layout()
   plt.savefig(os.path.join(Root, "UML_Categories.png"))
   plt.show()
# ------------------------------------------------------------------------------
def PlotLineChart(results):
   categories = list(results.keys())
   precisions = [results[cat]['precision'] for cat in categories]
   recalls = [results[cat]['recall'] for cat in categories]
   f1s = [results[cat]['f1'] for cat in categories]

   plt.figure(figsize=(10, 6))
   plt.plot(categories, precisions, marker='o', label='Precision')
   plt.plot(categories, recalls, marker='o', label='Recall')
   plt.plot(categories, f1s, marker='o', label='F1 Score')

   plt.ylim(0, 1.1)
   plt.ylabel("Score")
   plt.title("Evaluation Metrics by Category")
   plt.legend()
   plt.grid(True)
   plt.tight_layout()
   plt.savefig(os.path.join(Root, "Precision_Recall_F1.png"))
   plt.show()
# ------------------------------------------------------------------------------
def NormalizeAttrOrMethod(class_name, name, typeprint=None, parameters=None, is_method=False, visibility=None, is_static=None, is_abstract=None, is_constructor=False, return_type=None):
   if is_method:
      if parameters:
         param_str = ",".join([param.get('typeprint', '') for param in parameters])
         return f"{class_name}.{name}({param_str}):{visibility}:{is_static}:{is_abstract}"
      else:
         return f"{class_name}.{name}:{visibility}"
   elif is_constructor:    
      if parameters:
         param_str = ",".join([param['typeprint'] for param in parameters])
         return f"{class_name}.{name}({param_str}):{visibility}"
      else:
         return f"{class_name}.{name}:{visibility}"
   else:
      return f"{class_name}.{name}:{typeprint}:{visibility}:{is_static}:{is_abstract}"
# ------------------------------------------------------------------------------
def ExtractElements(classes_data, classes_relationship):
   class_names, attributes, methods, constructors, relationships = set(), set(), set(), set(), set()

   for rel in classes_relationship:
      source = rel.get("Source")
      rel_type = rel.get("Relationship", "").lower()
      target = rel.get("Target")
      role1 = rel.get("Role1", "")
      mult1 = rel.get("Multiplicity1", "")
      role2 = rel.get("Role2", "")
      mult2 = rel.get("Multiplicity2", "")
      if rel_type in ["association", "aggregation", "composition"]:
         relationships.add(f"{rel_type}:{source}->{target}:{role1},{mult1}:{role2},{mult2}")
      else:
         relationships.add(f"{rel_type}:{source}->{target}") 
   for cls in classes_data:
      cls_name = cls.get("name")
      cls_type = cls.get("ClassInterface")
      cls_visibility = cls.get("Visibility")
      cls_IsStatic = cls.get("IsStatic")
      cls_IsAbstract = cls.get("IsAbstract")
      class_names.add(f"{cls_name}:{cls_type}:{cls_visibility}:{cls_IsStatic}:{cls_IsAbstract}")

      for var in cls.get("variables", []):
         attr_str = NormalizeAttrOrMethod(cls_name, var["name"], var.get("typeprint"), visibility=var.get("Visibility", ""), is_static=var.get("IsStatic", "") == "static", is_abstract=var.get("IsAbstract", "") == "abstract")
         attributes.add(attr_str)

      for method in cls.get("methods", []):
         method_str = NormalizeAttrOrMethod(cls_name, method["name"], parameters=method.get("parameters", []), visibility=method.get("Visibility", ""), is_static=method.get("IsStatic", "") == "static", is_abstract=method.get("IsAbstract", "") == "abstract", is_method=True, return_type=method.get("returnType", ""))
         methods.add(method_str)
      if Language=="Java":
         for constructor in cls.get("constructors", []):
            constructor_str = NormalizeAttrOrMethod(cls_name, constructor["name"], parameters=constructor.get("parameters", []), visibility=constructor.get("Visibility", ""), is_constructor=True)
            constructors.add(constructor_str)

   return class_names, attributes, methods, constructors, relationships
# ------------------------------------------------------------------------------
def ComputeMetrics(predicted, actual):
   TP = predicted & actual
   FP = predicted - actual
   FN = actual - predicted
   if not actual and not predicted:
      precision = recall = f1 = 1.0
   else:
      recall = len(TP) / (len(TP) + len(FN)) if (len(TP) + len(FN)) > 0 else 0.0
      precision = len(TP) / (len(TP) + len(FP)) if (len(TP) + len(FP)) > 0 else 0.0
      f1 = 2 * precision * recall / (precision + recall) if (precision + recall) > 0 else 0.0

   return {"TP": TP, "FP": FP, "FN": FN, "precision": precision, "recall": recall, "f1": f1}
# ------------------------------------------------------------------------------
def PrintMetrics(name, metrics):
   output = [f"{name}"]
   output.append(f"Precision: {metrics['precision']:.2f}")
   output.append(f"Recall:    {metrics['recall']:.2f}")
   output.append(f"F1 Score:  {metrics['f1']:.2f}\n")
   output.append("Matched:")
   output += [f"  {e}" for e in sorted(metrics['TP'])]
   output.append("Missing (in ground truth but not in tool):")
   output += [f"  {e}" for e in sorted(metrics['FN'])]
   output.append("Extra (in tool but not in ground truth):")  
   output += [f"  {e}" for e in sorted(metrics['FP'])]
   output.append("")
   return "\n".join(output)
# ------------------------------------------------------------------------------
def WriteSingleColumnExcel(content, excel_path):
   wb = Workbook()
   ws = wb.active
   for i, line in enumerate(content.splitlines(), start=1):
      ws.cell(row=i, column=1, value=line)
   wb.save(excel_path)
# ------------------------------------------------------------------------------
def WriteCSV(results: Dict[str, Dict], filename: str):
   with open(filename, mode="w", newline="") as file:
      writer = csv.writer(file)
      writer.writerow(["Section", "Precision", "Recall", "F1"])
      for key, row in results.items():
         writer.writerow([key, row["precision"], row["recall"], row["f1"]])
# ------------------------------------------------------------------------------
def BuildFullOutput(classes, attributes, constructors, methods, relationships):
   if Language=="Java":
      return "\n".join([
         PrintMetrics("Classes", classes),
         PrintMetrics("Attributes", attributes),
         PrintMetrics("Constructors", constructors),
         PrintMetrics("Methods", methods),
         PrintMetrics("Relationships", relationships)
      ])
   else:
      return "\n".join([
         PrintMetrics("Classes", classes),
         PrintMetrics("Attributes", attributes),
         PrintMetrics("Methods", methods),
         PrintMetrics("Relationships", relationships)
      ])
# ------------------------------------------------------------------------------     
def evaluate(tool_output_data, ground_truth_data, tool_output_relation, ground_truth_relation, uml_path):
   tool_cls, tool_attr, tool_methods, tool_constr, tool_rels = ExtractElements(tool_output_data, tool_output_relation)
   gt_cls, gt_attr, gt_methods, gt_constr, gt_rels = ExtractElements(ground_truth_data, ground_truth_relation)

   class_metrics = ComputeMetrics(tool_cls, gt_cls)
   attr_metrics = ComputeMetrics(tool_attr, gt_attr)
   method_metrics = ComputeMetrics(tool_methods, gt_methods)
   constructor_metrics = ComputeMetrics(tool_constr, gt_constr)
   rel_metrics = ComputeMetrics(tool_rels, gt_rels)

   full_output = BuildFullOutput(class_metrics, attr_metrics, constructor_metrics, method_metrics, rel_metrics)
   base_dir = os.path.dirname(uml_path)
   uml_name = os.path.splitext(os.path.basename(uml_path))[0]
   excel_output_path = os.path.join(base_dir, f"{uml_name}.xlsx")
   WriteSingleColumnExcel(full_output, excel_output_path)
   if Language=="Java":
      results = {
         "classes": class_metrics,
         "attributes": attr_metrics,
         "constructors": constructor_metrics,
         "methods": method_metrics,
         "relationships": rel_metrics
      }
   else:
      results = {
         "classes": class_metrics,
         "attributes": attr_metrics,
         "methods": method_metrics,
         "relationships": rel_metrics
      }
        
   PlotGraphs(results)
   PlotLineChart(results)
   WriteCSV(results, os.path.join(base_dir, "evaluation_results.csv"))
# ------------------------------------------------------------------------------
if __name__ == "__main__":
   for Root, Dirs, Files in os.walk(Directory):
      for File in Files:
         if File == "Test1.UML":
            print(Root + "\\" + File)

            UML = os.path.join(Root, "Test1.UML")
            UMLG = os.path.join(Root, "Test1G.UML")
            REL = os.path.join(Root, "Test1.REL")
            RELG = os.path.join(Root, "Test1G.REL")

            with open(UML, 'r') as f:
               tool_output_data = json.load(f)
            with open(UMLG, 'r') as f:
               ground_truth_data = json.load(f)
            with open(REL, 'r') as f:
               tool_output_relation = json.load(f)
            with open(RELG, 'r') as f:
               ground_truth_relation = json.load(f)

            evaluate(tool_output_data, ground_truth_data, tool_output_relation, ground_truth_relation, UML)
# ------------------------------------------------------------------------------

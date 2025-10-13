"""
Author: Hanan Abdulwahab Siala
University: King's College London
Date: 2020-10-05

Description:
    This program compares the outputs from the LLM4Models LLM and the AgileUML toolset.
"""
# ------------------------------------------------------------------------------
import os
import re
import csv
from difflib import SequenceMatcher
from termcolor import colored
import matplotlib.pyplot as plt  
import numpy as np
# ------------------------------------------------------------------------------
threshold=1.0 
output_file="LLM4Models.txt"

I_want_small_figures=False
#I_want_small_figures=True

Dir = ####
# ------------------------------------------------------------------------------
def NormalizeOCL(ocl: str) -> str:
   ocl = re.sub(r"--.*", "", ocl)
   ocl = re.sub(r"\s+", "", ocl)
   return ocl.strip().lower()
# ------------------------------------------------------------------------------
def LoadOCLRaw(path: str) -> list:
   with open(path, 'r', encoding='utf-8') as f:
      return [line.strip() for line in f if line.strip()]
# ------------------------------------------------------------------------------
def CompareOCL(gt_lines, tool_lines, Dir, number, threshold=1.0):
   gt_norm = {NormalizeOCL(c): c for c in gt_lines}
   tool_norm = {NormalizeOCL(c): c for c in tool_lines}

   tp, fp, fn = [], [], []
   matched_gt_keys = set()

   for t_key, t_val in tool_norm.items():
      found = False
      for g_key, g_val in gt_norm.items():
         sim = SequenceMatcher(None, t_key, g_key).ratio()
         if sim >= threshold and g_key not in matched_gt_keys:
            tp.append((t_val, g_val))
            matched_gt_keys.add(g_key)
            found = True
            break
      if not found:
         fp.append(t_val)

   for g_key, g_val in gt_norm.items():
      if g_key not in matched_gt_keys:
         fn.append(g_val)

   recall = len(tp) / (len(tp) + len(fn)) if (tp + fn) else 0
   precision = len(tp) / (len(tp) + len(fp)) if (tp + fp) else 0
   f1 = 2 * precision * recall / (precision + recall) if (precision + recall) else 0

   csv_path = f"{Dir}\\ocl_eval{number}_results.csv"
   with open(csv_path, 'w', newline='', encoding='utf-8') as f:
      writer = csv.writer(f)
      writer.writerow(["Type", "Tool_OCL", "GroundTruth_OCL"])
      for tool, truth in tp:
         writer.writerow(["TP", tool, truth])
      for extra in fp:
         writer.writerow(["FP", extra, ""])
      for missing in fn:
         writer.writerow(["FN", "", missing])

   return {
      "tp": tp, "fp": fp, "fn": fn,
      "precision": precision,
      "recall": recall,
      "f1": f1
  }
# ------------------------------------------------------------------------------
def PlotGraphs(results): 
   files = [r["filename"] for r in results]
   tp = [r["tp"] for r in results]
   fp = [r["fp"] for r in results]
   fn = [r["fn"] for r in results]
   precision = [r["precision"] for r in results]
   recall = [r["recall"] for r in results]
   f1 = [r["f1"] for r in results]
   if I_want_small_figures==False:
      plt.figure(figsize=(12, 6))
      plt.bar(files, tp, label='TP', color='green')
      plt.bar(files, fp, bottom=tp, label='FP', color='red')
      plt.bar(files, fn, bottom=[tp[i] + fp[i] for i in range(len(tp))], label='FN', color='orange')
      plt.ylabel("Count")
      plt.title("TP / FP / FN per File")
      plt.xticks(rotation=45)
      plt.legend()
      plt.tight_layout()
      image=Dir+"\\ocl_tp_fp_fn.png"
      plt.savefig(image)
      plt.show()

      plt.figure(figsize=(12, 6))
      plt.plot(files, precision, marker='o', label='Precision')
      plt.plot(files, recall, marker='s', label='Recall')
      plt.plot(files, f1, marker='^', label='F1 Score')
      plt.ylabel("Score")
      plt.ylim(0, 1.05)
      plt.title("Precision / Recall / F1 Score")
      plt.xticks(rotation=45)
      plt.legend()
      plt.tight_layout()
      image=Dir+"\\ocl_scores.png"
      plt.savefig(image)
      plt.show()
   else:   
      x = np.arange(len(files))
      bar_width = 0.6
      plt.figure(figsize=(6, 5))  
      p1 = plt.bar(x, tp, width=bar_width, label='TP', color='green')
      p2 = plt.bar(x, fp, width=bar_width, bottom=tp, label='FP', color='red')
      p3 = plt.bar(x, fn, width=bar_width, bottom=[tp[i] + fp[i] for i in range(len(tp))], label='FN', color='orange')

      for i in range(len(x)):
         total = tp[i] + fp[i] + fn[i]
         plt.text(x[i], total + 1, str(total), ha='center', va='bottom', fontsize=9)

      plt.ylabel("Count")
      plt.title("TP / FP / FN per File")
      plt.xticks(x, files)
      plt.yticks(np.arange(0, max(tp) + max(fp) + max(fn) + 10, 10))  # nice ticks
      plt.legend()
      plt.tight_layout()
      image = Dir + "\\ocl_tp_fp_fn.png"
      plt.savefig(image, dpi=150)
      plt.show()
       
      x = np.arange(len(files)) 
      plt.figure(figsize=(6, 5)) 
      plt.plot(x, precision, marker='o', markersize=8, label='Precision')
      plt.plot(x, recall, marker='s', markersize=8, label='Recall')
      plt.plot(x, f1, marker='^', markersize=8, label='F1 Score')
      for i, (p, r, f) in enumerate(zip(precision, recall, f1)):
         plt.text(i, p + 0.02, f"{p:.2f}", ha='center', fontsize=9)
         plt.text(i, r + 0.02, f"{r:.2f}", ha='center', fontsize=9)
         plt.text(i, f + 0.02, f"{f:.2f}", ha='center', fontsize=9)

      plt.xticks(x, files)
      plt.ylim(0, 1.05)
      plt.ylabel("Score")
      plt.title("Precision / Recall / F1 Score")
      plt.grid(True, linestyle='--', alpha=0.6)
      plt.legend()
      plt.tight_layout()

      image = Dir + "\\ocl_scores.png"
      plt.savefig(image, dpi=150)
      plt.show()
# ------------------------------------------------------------------------------
def BatchEvaluate():
   results = []
   output_csv=Dir+"\\Overall_results.csv"
   subdirs = [d for d in os.listdir(Dir) if os.path.isdir(os.path.join(Dir, d)) and d.isdigit()]
   subdirs = sorted(subdirs, key=lambda x: int(x))  

   for subdir in subdirs:
      subdir_path = os.path.join(Dir, subdir)
      for root, dirs, files in os.walk(subdir_path):
         for file in files:          
            if file.endswith(".km3"):             
               gt_path = root+"\\"+file
               tool_path = root+"\\"+output_file
             
               matches = re.findall(r'\\(\d+)\\', tool_path)
               number = matches[-1]

               gt_lines = LoadOCLRaw(gt_path)
               tool_lines = LoadOCLRaw(tool_path)

               eval_result = CompareOCL(gt_lines, tool_lines, root, number, threshold)
               results.append({
                  "AgileUML_path":gt_path,
                  "LLM4Models_path":tool_path,
                  "filename": number,
                  "tp": len(eval_result["tp"]),
                  "fp": len(eval_result["fp"]),
                  "fn": len(eval_result["fn"]),
                  "precision": round(eval_result["precision"], 2),
                  "recall": round(eval_result["recall"], 2),
                  "f1": round(eval_result["f1"], 2)
               })

   with open(output_csv, 'w', newline='', encoding='utf-8') as f:
      fieldnames = ["AgileUML_path", "LLM4Models_path", "filename", "tp", "fp", "fn", "precision", "recall", "f1"]
      writer = csv.DictWriter(f, fieldnames=fieldnames)
      writer.writeheader()
      total_metrics = {
         "tp": 0,
         "fp": 0,
         "fn": 0,
         "precision": 0.0,
         "recall": 0.0,
         "f1": 0.0
      }

      count = len(results)

      for row in results:
         writer.writerow(row)
         total_metrics["tp"] += row.get("tp", 0)
         total_metrics["fp"] += row.get("fp", 0)
         total_metrics["fn"] += row.get("fn", 0)
         total_metrics["precision"] += row.get("precision", 0.0)
         total_metrics["recall"] += row.get("recall", 0.0)
         total_metrics["f1"] += row.get("f1", 0.0)

      avg_metrics = {k: (v / count if count > 0 else 0) for k, v in total_metrics.items()}

      writer.writerow({
         "AgileUML_path": "AVERAGE",
         "LLM4Models_path": "",
         "filename": "",
         "tp": avg_metrics["tp"],
         "fp": avg_metrics["fp"],
         "fn": avg_metrics["fn"],
         "precision": avg_metrics["precision"],
         "recall": avg_metrics["recall"],
         "f1": avg_metrics["f1"]
      })

   PlotGraphs(results)
# ------------------------------------------------------------------------------
if __name__ == "__main__":   
   BatchEvaluate()
# ------------------------------------------------------------------------------




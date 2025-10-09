<img src="Images/LLM4Models.png" alt="Banner" width="800"/>

# Thesis Artifacts

This repository contains all artifacts used in my thesis, which are classified into four main groups:

## 1. [Training datasets](./Training-datasets/)
Contains training datasets.
## 2. [LLM4Models](./LLM4Models/)  
Contains models to abstract UML and OCL representations from Java and Python programs.
## 3. [Evaluation](./Evaluation/)  
Provides case studies used to evaluate the LLM4Models approach.
## 4. [Scripts](./Scripts/)
Contains the most important scripts used in the thesis work.

---

### Repository Structure

```
.
├── Evaluation
│   ├── Experiments
│   │   ├── LLM4Models
│   │   │   ├── Java-OCL
│   │   │   ├── Java-UML                
│   │   │   ├── Python-OCL              
│   │   │   ├── Python-UML
│   │   ├── LLMs
│   │   │   ├── Java-OCL
│   │   │   ├── Java-UML                
│   │   │   ├── Python-OCL              
│   │   │   ├── Python-UML
│   ├── Case-studies
│   │   ├── Java-OCL
│   │   ├── Java-UML                
│   │   ├── Python-OCL              
│   │   ├── Python-UML
├── Images
├── LLM4Models                  # The generated LLM4Models LLM
│   ├── Java-OCL
│   ├── Java-UML                
│   ├── Python-OCL              
│   ├── Python-UML
├── Scripts                     # Scripts for finetuning models
│   ├── Dataset                 # Scripts to construct the training datasets
│   ├── Fine-tuning             # Scripts to fine-tune Mistral-7B LLM   
│   ├── Inference               # Scripts for inferring UML class diagrams and OCL specification
│   ├── Parsers                 # Java2JSON Parser and Python2JSON Parser
│   ├── Statistics              # Scripts to compare the results of LLM4Models LLM with: 1) the results of Java2JSON and Python2JSON (UML)
│   │                                                                                    2) the results of the AgileUML toolset (OCL)
├── Training-datasets
│   ├── Java-OCL
│   ├── Java-UML                
│   ├── Python-OCL              
│   ├── Python-UML              
└── README.md
```

---
### Inferring Process
1- Preprocessing Stage:
Before the inferring process for abstracting UML and OCL representation, a pre-processing stage should be applied to the Java/Python program by executing a Python script "Preprocessing", where you need to choose the following: 
    1. Language: Java or Python.
    2. The source directory that contains the source program before pre-processing in "InputDirectoryProgram" variable.
    3. The output directory that contains the program after pre-processing in "OutputDirectory" variable.
    4. Whether classes are just one directory or many directories by assigning true or false value to "ManyDirectories" variable.
    5. The output will be saved in 'CleanFile.java' for Java, and 'CleanFile.py' for Python.  

2- Inferring Stage:

   1) Put your program in Test1.java for Java programs or Test1.py for Python programs.
   2) The output will be saved in LLM4Models.txt
   3) For What_I_Want variable:
       - Choose 1 to abstract UML from Java
       - Choose 2 to abstract UML from Python
       - Choose 3 to abstract OCL from Java
       - Choose 4 to abstract OCL from Python
   4) Put Full_Model=True for using a full model or Full_Model=False for using LoRA adapter
   5) Choose version 1 or 2 for variable "version"

3- Post-Processing Stage:
  1) For abstracting UML class diagrams from both Java and Python programs, a post-processing stage should be applied to the output of the LLM4Models LLM, which includes: 
    a) Splitting the output into two JSON files by executing a Python script in "PostprocessingUML". 
    b) Drawing the generated UML class diagrams graphically using the Graphviz tool, which can be saved in png format by running the Python script "DrawingClassDiagram". Three choices are available for drawing  methods in the generated UML class diagrams:
        - Methods with parameters' names and types.
        - Methods with parameter' types.
        - Methods only (Default).

  2) For abstracting OCL specifications from Java and Python programs, a post-processing stage should be applied to the LLM4Models LLM output by running the post-processing Python script "PostprocessingOCL". 

---

### Languages Used
- Python (primary).

---

## Citation

If you use this repository or reference the thesis, please cite:

**Model-driven Approaches for Reverse Engineering, PhD Thesis, Hanan Abdulwahab Siala, supervised by Kevin Lano and Gunel Jahangirova, 2025, King's College London**

---

### BibTeX
```bibtex
@phdthesis{siala2025reverse,
  title        = {Model-driven Approaches for Reverse Engineering},
  author       = {Hanan Abdulwahab Siala},
  school       = {King's College London},
  year         = {2025},
  note         = {PhD Thesis. Supervised by Kevin Lano and Gunel Jahangirova}
}


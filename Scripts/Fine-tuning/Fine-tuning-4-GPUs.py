"""
Based on: Philipp Schmid's code (https://github.com/philschmid/deep-learning-pytorch-huggingface/blob/main/training/scripts/run_fsdp_qlora.py)
Modified by: Hanan Abdulwahab Siala, King's College London
Date: 2020-10-05

Description:
    This program fine-tunes on 4 GPUs to construct OCL specifications from both Java and Python programs.
"""
# ----------------------------------------------------------------------------
import logging
import os
import random
import torch

from huggingface_hub import login
from datasets import load_dataset, Dataset, load_from_disk
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_recall_fscore_support

from transformers import AutoTokenizer, TrainingArguments
from trl.commands.cli_utils import TrlParser
from transformers import (
    AutoModelForCausalLM,
    BitsAndBytesConfig,
    set_seed,
)
from trl import SFTTrainer
from peft import LoraConfig, prepare_model_for_kbit_training, get_peft_model, PeftModel, PeftConfig
# ----------------------------------------------------------------------------
# 1=Java-UML
# 2=Python-UML
# 3=Java-OCL
# 4=Python-OCL
# ----------------------------------------------------------------------------
What_I_Want=1
Big_or_Small=2  # 1 or 2
Resume= False
# https://wandb.ai/tripoliuniversity/huggingface/runs/7zs2irqu?nw=nwuserlibya9009   id="7zs2irqu",
if Resume==True:
   import wandb
   wandb.init(
      project="huggingface",                      
      id="6gq09why",                              
      resume="must"                               
   )
   PathResume="/scratch/users/k20122072/Mistral-7B-PythonOCLB/checkpoint-1105" 
# ----------------------------------------------------------------------------
if Big_or_Small==1:
   if What_I_Want==1:
      FileName = "JavaUMLB.json" 
   elif What_I_Want==2: 
      FileName = "PythonUMLB.json" 
   elif What_I_Want==3:
      FileName = "JavaOCLB.json"
   else:           
      FileName = "PythonOCLB.json"   
else:
   if What_I_Want==1:
      FileName = "JavaUML.json" 
   elif What_I_Want==2: 
      FileName = "PythonUML.json" 
   elif What_I_Want==3:
      FileName = "JavaOCL.json"
   else:           
      FileName = "PythonOCL.json"
# ----------------------------------------------------------------------------
if Big_or_Small==1:
   if What_I_Want==1:
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-JavaUMLB"
   elif What_I_Want==2:  
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-PythonUMLB" 
   elif What_I_Want==3:
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-JavaOCLB"
   else:
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-PythonOCLB"
else:
   if What_I_Want==1:
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-JavaUML"
   elif What_I_Want==2:  
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-PythonUML" 
   elif What_I_Want==3:
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-JavaOCL"
   else:
      OutputDirectory="/scratch/users/k20122072/Mistral-7B-PythonOCL" 
# ----------------------------------------------------------------------------
config = {
   "compute_environment": "LOCAL_MACHINE",
   "debug": False,
   "distributed_type": "FSDP",
   "downcast_bf16": "no",
   "fsdp_config": {
       "fsdp_auto_wrap_policy": "transformer_based_wrap", #"TRANSFORMER_BASED_WRAP",
       "fsdp_backward_prefetch": "backward_pre", #"BACKWARD_PRE",
       "fsdp_cpu_ram_efficient_loading": True,
       "fsdp_forward_prefetch": False,
       "fsdp_offload_params": True,
       "fsdp_sharding_strategy": "full_shard", #"FULL_SHARD",
       "fsdp_state_dict_type": "shared_state_dict", #"SHARDED_STATE_DICT",
       "fsdp_sync_module_states": True,
       "fsdp_use_orig_params": False #,
   },
   "machine_rank": 0,
   "main_training_function": "main",
   "mixed_precision": "no",
   "num_machines": 1,
   "num_processes": 4,
   "rdzv_backend": "static",
   "same_network": True,
   "tpu_env": [],
   "tpu_use_cluster": False,
   "tpu_use_sudo": False,
   "use_cpu": False,
}
# ----------------------------------------------------------------------------
My_token="YOUR_TOKEN_HERE"
os.environ["HUGGINGFACE_HUB_TOKEN"] = My_token
model_id = "mistralai/Mistral-7B-v0.3"  
# ----------------------------------------------------------------------------
def SplitData(Data, TrainSize=.90, TestSize=0.1):
   train_test = Data["train"].train_test_split(test_size=TestSize, shuffle=True, seed=42)
   Temp       = train_test["train"]
   TestData   = train_test["test"]

   train_valid = Temp.train_test_split(test_size=TestSize, shuffle=True, seed=42)
   TrainData   = train_valid["train"]
   ValidData   = train_valid["test"]
   return TrainData, ValidData, TestData
# ----------------------------------------------------------------------------
def GenerateTrainingPrompt(Sample, Instruction):
   if What_I_Want==1 or What_I_Want==2:
      Prompt="""<s>[INST] Below is an instruction that describes a task, paired with an input that provides further context. Write a response, which is in JSON format that appropriately solves the following Task:""".strip()
   else:
      Prompt="""<s>[INST] Below is an instruction that describes a task, paired with an input that provides further context. Write a response that appropriately solves the following Task:""".strip()
   return f"""{Prompt} \n
### Instruction:    
{Instruction} 
### Input:
{Sample["input"]}
[/INST]

### Response:
{Sample["output"]} </s>"""
# ----------------------------------------------------------------------------
def GenerateText(Sample):
   if What_I_Want==1:     
      Instruction="""Generate a concise UML class diagram for the provided Java code. The output should:
1. Define each class and interface only once, including its attributes, methods, and relationships.
2. Include all relationships (Inheritance, Realization, Dependency, Association, Composition, Aggregation) without duplication.
3. Avoid redundant or repeated operations, classes, or relationships.""" 
   elif What_I_Want==2: 
      Instruction="""Generate a concise UML class diagram for the provided Python code. The output should:
1. Define each class and interface only once, including its attributes, methods, and relationships.
2. Include all relationships (Inheritance, Realization, Dependency, Association, Composition, Aggregation) without duplication.
3. Avoid redundant or repeated operations, classes, or relationships.""" 
   elif What_I_Want==3: 
      Instruction="""Generate an Object Constraint Language (OCL) specification for the provided Java code. The output should:
1. Ensure no repeated or redundant operations or classes.
2. Include only the OCL code for the provided Java code.
3. Do not include statements for items not found in the Java code."""  
   else:
      Instruction="""Generate an Object Constraint Language (OCL) specification for the provided Python code. The output should:
1. Ensure no repeated or redundant operations or classes.
2. Include only the OCL code for the provided Python code.
3. Do not include statements for items not found in the Python code."""  
   return {
       "instruction": Instruction,
       "input":  Sample["input"],
       "output": Sample["output"],
       "text": GenerateTrainingPrompt(Sample, Instruction),
 }
# ----------------------------------------------------------------------------
def training_function():

    # Mistral Tokenizer and Formatting

    login(My_token) 
    tokenizer = AutoTokenizer.from_pretrained(model_id, 
                                          padding_side="left", 
                                          add_eos_token=True, 
                                          add_bos_token=True, 
                                          token=My_token,
                                          use_fast=True) # fsdp
    tokenizer.pad_token = tokenizer.unk_token 
    tokenizer.padding_side = "left" 

    # Dataset

    if Big_or_Small==1:
       if What_I_Want==1:
          SavingDirectory = "/scratch/users/k20122072/JavaUMLB"
       elif What_I_Want==2:    
          SavingDirectory = "/scratch/users/k20122072/PythonUMLB"
       elif What_I_Want==3:    
          SavingDirectory = "/scratch/users/k20122072/JavaOCLB"
       else:    
          SavingDirectory = "/scratch/users/k20122072/PythonOCLB"
    else:       
       if What_I_Want==1:
          SavingDirectory = "/scratch/users/k20122072/JavaUML"
       elif What_I_Want==2:    
          SavingDirectory = "/scratch/users/k20122072/PythonUML"
       elif What_I_Want==3:    
          SavingDirectory = "/scratch/users/k20122072/JavaOCL"
       else:    
          SavingDirectory = "/scratch/users/k20122072/PythonOCL"
    print(Resume)
    if Resume==True:
       print("In resume section") 
       TrainData = load_from_disk(SavingDirectory+"/TrainData")
       ValidData = load_from_disk(SavingDirectory+"/ValidData")
       TestData = load_from_disk(SavingDirectory+"/TestData")
    else:    
       Data = load_dataset("json", data_files=FileName)
       Data=Data.map(GenerateText)
       train_dataset = Data['train']
       TrainData, ValidData, TestData = SplitData(Data)

       print(f'Number of prompts: {len(TrainData)}')
       print(f'Column names are: {TrainData.column_names}')

       TrainData.shape

       TrainData = TrainData.map(lambda x: {'text': x['text']})  
       TrainData = TrainData.remove_columns(['program', 'input', 'output', 'instruction'])  

       ValidData = ValidData.map(lambda x: {'text': x['text']})  
       ValidData = ValidData.remove_columns(['program', 'input', 'output', 'instruction'])  

       TestData = TestData.map(lambda x: {'text': x['text']})  
       TestData = TestData.remove_columns(['program', 'input', 'output', 'instruction'])     

       # Save files to disk
       
       TrainData.save_to_disk(SavingDirectory+"/TrainData")
       ValidData.save_to_disk(SavingDirectory+"/ValidData")
       TestData.save_to_disk(SavingDirectory+"/TestData")
    
    # Model

    torch_dtype = torch.bfloat16
    quant_storage_dtype = torch.bfloat16

    quantization_config = BitsAndBytesConfig(
       load_in_4bit=True,
       bnb_4bit_use_double_quant=True,
       bnb_4bit_quant_type="nf4",
       bnb_4bit_compute_dtype=torch_dtype,
       bnb_4bit_quant_storage=quant_storage_dtype,
    )

    model = AutoModelForCausalLM.from_pretrained(
       model_id,
       quantization_config=quantization_config,
       attn_implementation='flash_attention_2', #"sdpa",
       torch_dtype=quant_storage_dtype,
       use_cache=not training_args.gradient_checkpointing,
    )
    model.config.pad_token_id = tokenizer.pad_token_id 
    
    if training_args.gradient_checkpointing:
       model.gradient_checkpointing_enable()

    # PEFT

    peft_config = LoraConfig(
       lora_alpha=8,
       lora_dropout=0.05,
       r=16,
       bias="none",
       target_modules="all-linear",
       task_type="CAUSAL_LM",
    )
    #############################################
    # for version 2
    #peft_config = LoraConfig(r=16,lora_alpha=32,
    #target_modules=[
    #    "q_proj",
    #    "k_proj",
    #    "v_proj",
    #    "o_proj",
    #    "gate_proj",
    #    "up_proj",
    #    "down_proj",
    #    "lm_head",
    #],
    #bias="none",
    #lora_dropout=0.05,
    #task_type="CAUSAL_LM",
    #)
    #############################################
    # Training

    trainer = SFTTrainer(
       model= model, #peft_model,
       args=training_args,
       train_dataset=TrainData,
       dataset_text_field="text",
       eval_dataset=ValidData,
       peft_config=peft_config,
       tokenizer=tokenizer,
       packing=True,
       max_seq_length=2048,
       dataset_kwargs={
          "add_special_tokens": False,
          "append_concat_token": False,
       },
    )
    print("Before training")
    if trainer.accelerator.is_main_process:
       print("Printing trainable parameters...")
       trainer.model.print_trainable_parameters()

    # Train model

    checkpoint = training_args.resume_from_checkpoint

    print("Starting training")
    if Resume:
       trainer.train(resume_from_checkpoint=PathResume)
    else:
       trainer.train(resume_from_checkpoint=checkpoint)

    # Save Model

    if trainer.is_fsdp_enabled:
       trainer.accelerator.state.fsdp_plugin.set_state_dict_type("FULL_STATE_DICT")
# ----------------------------------------------------------------------------
if __name__ == "__main__":
   training_args = TrainingArguments(
       output_dir=OutputDirectory,
       num_train_epochs=20, #10
       per_device_train_batch_size= 8,
       evaluation_strategy="epoch",
       save_strategy="epoch",
       save_total_limit=20, # 10
       logging_dir="./logs",
       logging_steps=50, #10,
       gradient_checkpointing=config["fsdp_config"]["fsdp_offload_params"],
       fsdp=config["fsdp_config"]["fsdp_sharding_strategy"],
    #############################################
    # for version 2
       learning_rate=5e-5,
       #optim="adamw_hf",
       #lr_scheduler_type="cosine", # for Python. "linear" for Java
    #############################################
   )
   set_seed(training_args.seed)
   training_function()
# ----------------------------------------------------------------------------

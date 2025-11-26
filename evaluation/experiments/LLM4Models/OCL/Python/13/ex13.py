import pandas as pd
data = pd.read_csv("Data/Data1.csv")
marks_column = data["Marks"]
average_marks = marks_column.mean()
print(f"The average marks are: {average_marks}")

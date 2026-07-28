import pandas as pd

try:
    file_path = 'Jithu Daily Expense Sheet  (1).xlsx'
    xls = pd.ExcelFile(file_path)
    print("Sheets:", xls.sheet_names)
    for sheet in xls.sheet_names:
        print(f"\n--- Sheet: {sheet} ---")
        df = pd.read_excel(xls, sheet_name=sheet, nrows=15)
        print(df.to_string())
except Exception as e:
    print("Error:", e)

const xlsx = require('xlsx');
const fs = require('fs');

try {
    const workbook = xlsx.readFile('Jithu Daily Expense Sheet  (1).xlsx');
    console.log("Sheet Names:", workbook.SheetNames);
    
    workbook.SheetNames.forEach(sheetName => {
        console.log(`\n--- Sheet: ${sheetName} ---`);
        const sheet = workbook.Sheets[sheetName];
        const data = xlsx.utils.sheet_to_json(sheet, { header: 1 }); // read as array of arrays
        
        // Print first 20 rows
        for (let i = 0; i < Math.min(data.length, 20); i++) {
            console.log(data[i]);
        }
    });
} catch (e) {
    console.error("Error reading excel file:", e);
}

#!/usr/bin/env python3
"""
Generate sample XLSX file for testing X5 Recruitment System import.
This creates an Excel file with the exact format expected by the system.
"""

import sys
from datetime import datetime, timedelta
import random

try:
    from openpyxl import Workbook
    from openpyxl.styles import Font
except ImportError:
    print("Error: openpyxl not installed. Install with: pip install openpyxl")
    sys.exit(1)

def generate_sample_data():
    """Generate sample candidate data."""
    first_names = ["Иван", "Петр", "Мария", "Анна", "Алексей", "Елена", "Дмитрий", "Ольга"]
    last_names = ["Иванов", "Петров", "Сидоров", "Козлов", "Новиков", "Морозов", "Волков", "Соколов"]
    cities = ["Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург", "Казань"]
    universities = ["МГУ", "СПбГУ", "НГУ", "УрФУ", "КФУ"]
    specialities = ["Информатика", "Прикладная математика", "Программная инженерия", "Информационные системы"]
    programs = ["Backend Development", "Frontend Development", "Data Science", "DevOps", "QA Engineering"]
    sources = ["Университет", "HeadHunter", "Социальные сети", "Друзья", "Сайт компании"]
    languages_sets = [
        "Python; Java; SQL",
        "JavaScript; TypeScript; React",
        "Python; R; SQL",
        "Java; Spring; Kotlin",
        "C++; Python; Go"
    ]
    
    data = []
    base_date = datetime.now() - timedelta(days=30)
    
    for i in range(10):
        row = {
            "Фамилия": random.choice(last_names),
            "Имя": random.choice(first_names),
            "ТГ": f"@user{i+1}",
            "Телефон": f"7(9{random.randint(10, 99)})999-{random.randint(10, 99)}-{random.randint(10, 99)}",
            "Почта": f"candidate{i+1}@example.com",
            "Резюме": f"https://example.com/resume/{i+1}",
            "Первый приоритет": random.choice(programs),
            "Второй приоритет": random.choice([p for p in programs]),
            "Курс": str(random.randint(1, 5)),
            "Специальность": random.choice(specialities),
            "Другая специальность": "",
            "График": random.choice(["Полный день", "Гибкий график", "Удаленно"]),
            "Город": random.choice(cities),
            "Другой город": "",
            "Откуда узнал": random.choice(sources),
            "Год рождения": random.randint(1995, 2005),
            "Гражданство": "РФ",
            "ВУЗ": random.choice(universities),
            "Другой ВУЗ": "",
            "Языки": random.choice(languages_sets),
            "Дата заявки": base_date + timedelta(days=i)
        }
        data.append(row)
    
    # Add a few rows with validation errors for testing
    # Missing required fields
    error_row1 = {
        "Фамилия": "",
        "Имя": "",
        "ТГ": "@erroruser",
        "Телефон": "",
        "Почта": "error1@example.com",
        "Резюме": "",
        "Первый приоритет": "Backend Development",
        "Второй приоритет": "",
        "Курс": "3",
        "Специальность": "",
        "Другая специальность": "",
        "График": "",
        "Город": "",
        "Другой город": "",
        "Откуда узнал": "",
        "Год рождения": 2000,
        "Гражданство": "",
        "ВУЗ": "",
        "Другой ВУЗ": "",
        "Языки": "",
        "Дата заявки": datetime.now()
    }
    data.append(error_row1)
    
    # Invalid birth year
    error_row2 = {
        "Фамилия": "Тестов",
        "Имя": "Тест",
        "ТГ": "@testuser",
        "Телефон": "7(999)999-99-99",
        "Почта": "test@example.com",
        "Резюме": "",
        "Первый приоритет": "DevOps",
        "Второй приоритет": "",
        "Курс": "4",
        "Специальность": "Информатика",
        "Другая специальность": "",
        "График": "Полный день",
        "Город": "Москва",
        "Другой город": "",
        "Откуда узнал": "Друзья",
        "Год рождения": 1850,  # Invalid year
        "Гражданство": "РФ",
        "ВУЗ": "МГУ",
        "Другой ВУЗ": "",
        "Языки": "Python; Java",
        "Дата заявки": datetime.now()
    }
    data.append(error_row2)
    
    return data

def create_excel_file(output_path):
    """Create Excel file with sample data."""
    wb = Workbook()
    ws = wb.active
    ws.title = "Лист1"
    
    # Define headers
    headers = [
        "Фамилия", "Имя", "ТГ", "Телефон", "Почта", "Резюме",
        "Первый приоритет", "Второй приоритет", "Курс", "Специальность",
        "Другая специальность", "График", "Город", "Другой город",
        "Откуда узнал", "Год рождения", "Гражданство", "ВУЗ",
        "Другой ВУЗ", "Языки", "Дата заявки"
    ]
    
    # Write headers
    ws.append(headers)
    
    # Make header row bold
    for cell in ws[1]:
        cell.font = Font(bold=True)
    
    # Generate and write data
    data = generate_sample_data()
    for row_data in data:
        row = [row_data.get(header, "") for header in headers]
        ws.append(row)
    
    # Auto-adjust column widths
    for column in ws.columns:
        max_length = 0
        column_letter = column[0].column_letter
        for cell in column:
            try:
                if len(str(cell.value)) > max_length:
                    max_length = len(str(cell.value))
            except:
                pass
        adjusted_width = min(max_length + 2, 50)
        ws.column_dimensions[column_letter].width = adjusted_width
    
    # Save workbook
    wb.save(output_path)
    print(f"Sample Excel file created: {output_path}")
    print(f"Total rows: {len(data)} (including {2} with validation errors for testing)")

if __name__ == "__main__":
    output_file = "test-data/sample_applications.xlsx"
    create_excel_file(output_file)

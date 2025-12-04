import pandas as pd
import os
import shutil
from data_loader import load_interactions

DATASET_DIR = "dataset/bookstore"

def prepare_dataset():
    if os.path.exists("dataset"):
        shutil.rmtree("dataset")
    
    os.makedirs(DATASET_DIR, exist_ok=True)

    data = load_interactions()
    if not data:
        print("Không có dữ liệu interaction nào!")
        return

    rows = []
    for user_id, item_id, rating in data:
        rows.append([user_id, item_id, float(rating)])

    df = pd.DataFrame(rows, columns=["user_id", "item_id", "rating"])
    
    inter_path = os.path.join(DATASET_DIR, "bookstore.inter")

    header = "user_id:token\titem_id:token\trating:float\n"
    
    with open(inter_path, "w", encoding="utf-8") as f:
        f.write(header)
        df.to_csv(f, sep="\t", index=False, header=False)

    print(f"Đã tạo {inter_path} với {len(df)} bản ghi")

    yaml_content = """
user_id: token
item_id: token
rating: float
"""
    yaml_path = os.path.join(DATASET_DIR, "bookstore.inter.yaml")
    with open(yaml_path, "w", encoding="utf-8") as f:
        f.write(yaml_content.strip() + "\n")
    print(f"Đã tạo file config: {yaml_path}")

if __name__ == "__main__":
    prepare_dataset()
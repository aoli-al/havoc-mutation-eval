from email.mime import base
from genericpath import isdir
from re import sub
import sys
import os
from typing import List
import shutil



def process(items: List[str]):
    base_index = {}
    merged_path = "merged"
    os.mkdir(merged_path)
    for item in items:
        for subdir in os.listdir(item):
            dir_path = os.path.join(item, subdir)
            if os.path.isdir(dir_path):
                base_name = "-".join(subdir.split("-")[:-1])
                if base_name not in base_index:
                    base_index[base_name] = 0
                new_path = os.path.join(merged_path, base_name + "-" + str(base_index[base_name]))
                print(f"mapping from {dir_path} to {new_path}")
                shutil.copytree(dir_path, new_path)
                base_index[base_name] += 1

if __name__ == "__main__":
    process(sys.argv[1:])

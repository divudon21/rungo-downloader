import requests
import json
import sys

url = "https://nayani7-ok.hf.space/upload"
file_path = "app/build/outputs/apk/debug/app-debug.apk"

print("Uploading latest APK to your server to test filename...")
with open(file_path, "rb") as f:
    files = {"file": ("FastGo-v1.2-FixedExtension.apk", f)}
    response = requests.post(url, files=files)
    
    if response.status_code == 200:
        data = response.json()
        task_id = data.get("task_id")
        print(f"Task ID: {task_id}")
        print(f"Download Link: https://nayani7-ok.hf.space/download/{task_id}")
    else:
        print(f"Failed: {response.status_code} - {response.text}")

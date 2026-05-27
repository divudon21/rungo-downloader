with open("hf_space/app.py", "r") as f:
    content = f.read()

import_statement = "from fastapi import FastAPI, BackgroundTasks, Request, File, UploadFile\nimport shutil"
content = content.replace("from fastapi import FastAPI, BackgroundTasks, Request", import_statement)

upload_endpoint = """
@app.post("/upload")
async def upload_file(file: UploadFile = File(...)):
    task_id = str(uuid.uuid4())
    file_path = os.path.join(DATA_DIR, f"{task_id}.bin")
    
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    file_size = os.path.getsize(file_path)
    
    tasks[task_id] = {
        "task_id": task_id,
        "url": "local_upload",
        "status": "completed",
        "total_size": file_size,
        "downloaded": file_size,
        "speed": 0.0,
        "file_path": file_path,
        "timestamp": time.time()
    }
    return {"task_id": task_id}

if __name__ == "__main__":
"""
content = content.replace("if __name__ == \"__main__\":", upload_endpoint)

with open("hf_space/app.py", "w") as f:
    f.write(content)

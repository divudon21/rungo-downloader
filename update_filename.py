with open("hf_space/app.py", "r") as f:
    content = f.read()

# For /start_download (URL downloads)
download_code = """
@app.post("/start_download")
async def start_download(req: DownloadRequest, background_tasks: BackgroundTasks):
    task_id = str(uuid.uuid4())
    
    # Try to extract filename from URL
    filename = req.url.split("/")[-1]
    if "?" in filename:
        filename = filename.split("?")[0]
    if not filename or "." not in filename:
        filename = "downloaded_file.bin"
        
    tasks[task_id] = {
        "task_id": task_id,
        "url": req.url,
        "status": "downloading",
        "total_size": 0,
        "downloaded": 0,
        "speed": 0.0,
        "file_path": os.path.join(DATA_DIR, f"{task_id}_{filename}"),
        "original_filename": filename,
        "timestamp": time.time()
    }
    
    background_tasks.add_task(download_file, task_id, req.url)
    return {"task_id": task_id}
"""

# Replace the start_download function
import re
content = re.sub(r'@app\.post\("/start_download"\).*?return \{"task_id": task_id\}', download_code.strip(), content, flags=re.DOTALL)

# Update the download_file function signature and creation to not overwrite tasks[task_id] entirely
download_file_code = """
async def download_file(task_id: str, url: str):
    file_path = tasks[task_id]["file_path"]
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url) as response:
                response.raise_for_status()
                total_size = int(response.headers.get('Content-Length', 0))
                tasks[task_id]["total_size"] = total_size
                
                # Check for Content-Disposition header for filename
                cd = response.headers.get('Content-Disposition')
                if cd and 'filename=' in cd:
                    # Extract filename from header
                    fname = re.findall('filename="([^"]+)"', cd)
                    if not fname:
                        fname = re.findall('filename=([^;]+)', cd)
                    if fname:
                        new_filename = fname[0]
                        new_file_path = os.path.join(DATA_DIR, f"{task_id}_{new_filename}")
                        tasks[task_id]["file_path"] = new_file_path
                        tasks[task_id]["original_filename"] = new_filename
                        file_path = new_file_path

                downloaded = 0
                start_time = time.time()
                last_time = start_time
                last_downloaded = 0
                
                with open(file_path, 'wb') as f:
                    async for chunk in response.content.iter_chunked(1024 * 1024):
                        if not chunk:
                            break
                        f.write(chunk)
                        downloaded += len(chunk)
                        tasks[task_id]["downloaded"] = downloaded
                        
                        current_time = time.time()
                        if current_time - last_time >= 1.0:
                            speed = (downloaded - last_downloaded) / (current_time - last_time)
                            tasks[task_id]["speed"] = speed
                            last_time = current_time
                            last_downloaded = downloaded
                            
                tasks[task_id]["status"] = "completed"
                tasks[task_id]["speed"] = 0.0
    except Exception as e:
        tasks[task_id]["status"] = "error"
        tasks[task_id]["error"] = str(e)
"""
content = re.sub(r'async def download_file\(task_id: str, url: str\):.*?tasks\[task_id\]\["error"\] = str\(e\)', download_file_code.strip(), content, flags=re.DOTALL)

# Add import re at the top if not exists
if "import re" not in content:
    content = content.replace("import os", "import os\nimport re")

# Update /upload endpoint
upload_code = """
@app.post("/upload")
async def upload_file(file: UploadFile = File(...)):
    task_id = str(uuid.uuid4())
    original_filename = file.filename if file.filename else "uploaded_file.bin"
    file_path = os.path.join(DATA_DIR, f"{task_id}_{original_filename}")
    
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
        "original_filename": original_filename,
        "timestamp": time.time()
    }
    return {"task_id": task_id}
"""
content = re.sub(r'@app\.post\("/upload"\).*?return \{"task_id": task_id\}', upload_code.strip(), content, flags=re.DOTALL)

# Update /download endpoint to return the correct filename
download_endpoint_code = """
@app.get("/download/{task_id}")
async def download(task_id: str):
    if task_id not in tasks or tasks[task_id]["status"] != "completed":
        return {"error": "File not ready"}
    
    file_path = tasks[task_id]["file_path"]
    filename = tasks[task_id].get("original_filename", f"downloaded_{task_id}.bin")
    
    return FileResponse(file_path, filename=filename)
"""
content = re.sub(r'@app\.get\("/download/\{task_id\}"\).*?return FileResponse.*?$', download_endpoint_code.strip(), content, flags=re.MULTILINE | re.DOTALL)

with open("hf_space/app.py", "w") as f:
    f.write(content)

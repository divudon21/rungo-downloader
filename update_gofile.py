import re

with open("hf_space/app.py", "r") as f:
    content = f.read()

gofile_code = """
@app.post("/start_gofile_transfer")
async def start_gofile_transfer(req: DownloadRequest, background_tasks: BackgroundTasks):
    task_id = str(uuid.uuid4())
    filename = req.url.split("/")[-1]
    if "?" in filename:
        filename = filename.split("?")[0]
    if not filename or "." not in filename:
        filename = "transfer_file.bin"
        
    tasks[task_id] = {
        "task_id": task_id,
        "url": req.url,
        "status": "downloading",
        "total_size": 0,
        "downloaded": 0,
        "speed": 0.0,
        "file_path": os.path.join(DATA_DIR, f"{task_id}_{filename}"),
        "original_filename": filename,
        "gofile_url": None,
        "timestamp": time.time()
    }
    background_tasks.add_task(process_gofile_transfer, task_id, req.url)
    return {"task_id": task_id}

async def process_gofile_transfer(task_id: str, url: str):
    file_path = tasks[task_id]["file_path"]
    try:
        # 1. Download
        async with aiohttp.ClientSession() as session:
            async with session.get(url) as response:
                response.raise_for_status()
                total_size = int(response.headers.get('Content-Length', 0))
                tasks[task_id]["total_size"] = total_size
                
                cd = response.headers.get('Content-Disposition')
                if cd and 'filename=' in cd:
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
                            tasks[task_id]["speed"] = (downloaded - last_downloaded) / (current_time - last_time)
                            last_time = current_time
                            last_downloaded = downloaded
        
        # 2. Upload to GoFile
        tasks[task_id]["status"] = "uploading_to_gofile"
        tasks[task_id]["speed"] = 0.0
        
        async with aiohttp.ClientSession() as session:
            # Get server
            async with session.get("https://api.gofile.io/servers") as resp:
                servers_data = await resp.json()
                server = servers_data["data"]["servers"][0]["name"]
            
            # Upload
            upload_url = f"https://{server}.gofile.io/uploadFile"
            with open(file_path, 'rb') as f:
                data = aiohttp.FormData()
                data.add_field('file', f, filename=tasks[task_id]["original_filename"])
                async with session.post(upload_url, data=data) as upload_resp:
                    upload_result = await upload_resp.json()
                    if upload_result["status"] == "ok":
                        tasks[task_id]["gofile_url"] = upload_result["data"]["downloadPage"]
                        tasks[task_id]["status"] = "completed"
                    else:
                        raise Exception("GoFile upload failed")
                        
        # Cleanup local file after successful transfer
        try:
            os.remove(file_path)
        except:
            pass
            
    except Exception as e:
        tasks[task_id]["status"] = "error"
        tasks[task_id]["error"] = str(e)
"""

if "start_gofile_transfer" not in content:
    content = content.replace('if __name__ == "__main__":', gofile_code + '\nif __name__ == "__main__":')
    with open("hf_space/app.py", "w") as f:
        f.write(content)

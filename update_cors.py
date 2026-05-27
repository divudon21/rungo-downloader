with open("hf_space/app.py", "r") as f:
    content = f.read()

import_statement = """from fastapi import FastAPI, BackgroundTasks, Request, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
import shutil"""

content = content.replace("from fastapi import FastAPI, BackgroundTasks, Request, File, UploadFile\nimport shutil", import_statement)

cors_setup = """app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)"""

content = content.replace("app = FastAPI()", cors_setup)

with open("hf_space/app.py", "w") as f:
    f.write(content)

import re

with open("app/src/main/java/com/agon/app/data/ApiClient.kt", "r") as f:
    content = f.read()

# Remove the duplicate startGoFileTransfer at the bottom
content = re.sub(r'    suspend fun startGoFileTransfer\(url: String\): Result<DownloadResponse> \{.*?\n    \}\n\n    fun getDownloadUrl', '    fun getDownloadUrl', content, flags=re.DOTALL)

with open("app/src/main/java/com/agon/app/data/ApiClient.kt", "w") as f:
    f.write(content)

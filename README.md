# 🗜️ Secure Data Archiver

## 📘 Overview
The **Secure Data Archiver** is a Java-based web application that allows users to securely upload, compress, and encrypt files before archiving them.  
It also supports decryption and extraction of files with a valid passphrase. The project uses **Servlets** for backend logic and can be easily deployed via **Docker**.

---

## 🚀 Features
- 🔐 **Encryption & Decryption** of uploaded files  
- 📦 **Automatic ZIP compression** before encryption  
- 🌐 **Web Interface** for file uploads and passphrase input  
- ⚙️ **Modular Servlets Architecture** (`ArchiveServlet`, `DecryptServlet`)  
- 🧱 **Dockerized Build** for seamless deployment  
- ☕ **Maven Project** with clear dependency management  

---

## 🏗️ Project Structure
```
Secure-Data-Archiver/
├── Dockerfile
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/example/archiver/
│       │   ├── App.java
│       │   ├── ArchiveServlet.java
│       │   ├── DecryptServlet.java
│       │   └── Encryptor.java
│       └── resources/static/
│           └── index.html
└── target/
    └── secure-data-archiver-1.0.0.jar
```

---

## ⚙️ Technologies Used
| Layer | Technology |
|-------|-------------|
| Backend | Java, Servlets |
| Framework | Jetty Embedded Server |
| Build Tool | Maven |
| Deployment | Docker |
| Frontend | HTML, CSS, JavaScript |

---

## 🧩 How It Works
1. **User uploads a file** via the web interface.  
2. The `ArchiveServlet` compresses it into a ZIP archive.  
3. The file is **encrypted** using AES or another algorithm defined in `Encryptor.java`.  
4. The encrypted archive is saved on the server.  
5. For **decryption**, users can upload the encrypted file and provide the correct passphrase, which triggers the `DecryptServlet`.

---

## 🐳 Docker Deployment
```bash
# Build the Docker image
docker build -t secure-data-archiver .

# Run the container
docker run -p 8080:8080 secure-data-archiver
```
Then open your browser and navigate to:  
👉 **http://localhost:8080**

---

## 🧠 Maven Commands
```bash
mvn clean package
java -jar target/secure-data-archiver-1.0.0.jar
```

---

## 📁 Important Files
| File | Description |
|------|--------------|
| `App.java` | Initializes the Jetty server and registers servlets |
| `ArchiveServlet.java` | Handles file upload and encryption |
| `DecryptServlet.java` | Handles file decryption and extraction |
| `Encryptor.java` | Defines encryption/decryption algorithms |
| `index.html` | Frontend for user interaction |
| `Dockerfile` | Container setup for deployment |
| `pom.xml` | Maven dependencies and build configuration |

---

## 🔒 Security Note
Ensure the passphrase used during encryption is **secure and stored safely**.  
Losing the passphrase will make decryption impossible.

---

## 📈 Future Enhancements
- Add user authentication and session management  
- Support for multiple encryption algorithms via dropdown  
- File size and type validation  
- Encrypted cloud storage integration  
- Frontend UI improvement with progress bars  

---

## 👨‍💻 Author
Developed by **Essanth Sarvajith R K**  
> *A secure and elegant way to archive your data.*

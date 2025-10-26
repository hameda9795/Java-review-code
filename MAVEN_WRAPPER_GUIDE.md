# Maven Wrapper Guide

## ✅ Maven Wrapper Installed!

This project now includes **Maven Wrapper** - you can run Maven commands WITHOUT installing Maven on your system!

---

## 🚀 Quick Start

### **Windows (PowerShell):**

```powershell
# Build the project
.\mvnw.cmd clean install

# Run the application
.\mvnw.cmd spring-boot:run

# Run tests
.\mvnw.cmd test

# Package the application
.\mvnw.cmd package
```

### **Windows (CMD):**

```bash
# Build the project
mvnw.cmd clean install

# Run the application
mvnw.cmd spring-boot:run

# Run tests
mvnw.cmd test

# Package the application
mvnw.cmd package
```

### **Linux / Mac / Git Bash:**

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package the application
./mvnw package
```

---

## 📁 Files Included

The Maven Wrapper consists of these files (already included in your project):

```
devmentor-ai/
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.properties    # Wrapper configuration
│       └── maven-wrapper.jar          # Downloaded automatically
├── mvnw                               # Unix/Mac script
└── mvnw.cmd                           # Windows script
```

---

## 🔧 How It Works

1. **First Run**: When you run `./mvnw` or `mvnw.cmd` for the first time, it automatically downloads Maven 3.9.6
2. **Cached**: Maven is downloaded to `~/.m2/wrapper/` and reused for future runs
3. **No Installation**: You don't need to install Maven globally on your system
4. **Consistent Versions**: Everyone on the team uses the same Maven version (3.9.6)

---

## 💡 Common Commands

### Build and Run

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run

# Windows CMD
mvnw.cmd spring-boot:run

# Linux/Mac/Git Bash
./mvnw spring-boot:run
```

### Clean Build

```bash
# Windows
mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

### Run Tests

```bash
# Windows
mvnw.cmd test

# Linux/Mac
./mvnw test
```

### Package (JAR file)

```bash
# Windows
mvnw.cmd clean package

# Linux/Mac
./mvnw clean package
```

### Skip Tests

```bash
# Windows
mvnw.cmd clean install -DskipTests

# Linux/Mac
./mvnw clean install -DskipTests
```

---

## 🐛 Troubleshooting

### "Permission denied" (Linux/Mac)

If you get permission denied error:

```bash
chmod +x mvnw
./mvnw clean install
```

### "mvnw is not recognized" (Windows)

Use the full filename with extension:

```bash
mvnw.cmd clean install
```

### Download Issues

If Maven Wrapper can't download:

1. Check your internet connection
2. Try again - the download might have timed out
3. Check if you're behind a proxy:

```bash
# Set proxy (if needed)
export MAVEN_OPTS="-Dhttp.proxyHost=proxy.example.com -Dhttp.proxyPort=8080"
```

### Java Not Found

Make sure Java 21+ is installed:

```bash
java -version
```

If not installed, download from: https://adoptium.net/

---

## 🎯 Benefits

✅ **No Maven Installation**: Team members don't need to install Maven
✅ **Consistent Versions**: Everyone uses Maven 3.9.6
✅ **Easy Setup**: Just clone and run `./mvnw`
✅ **CI/CD Friendly**: Works in build pipelines
✅ **Cross-Platform**: Works on Windows, Linux, Mac

---

## 📚 Example Workflow

```bash
# 1. Clone the project
git clone <repo-url>
cd devmentor-ai

# 2. Run directly (no Maven installation needed!)
./mvnw spring-boot:run

# 3. Build for production
./mvnw clean package

# 4. Run the JAR
java -jar target/devmentor-ai-1.0.0.jar
```

---

## 🔗 Related Commands

### Check Maven Version

```bash
./mvnw -version
```

### Generate Wrapper (if needed)

If you ever need to regenerate the wrapper:

```bash
mvn -N wrapper:wrapper -Dmaven=3.9.6
```

### Update Maven Version

Edit `.mvn/wrapper/maven-wrapper.properties` and change the version in the `distributionUrl`.

---

## ✅ Verification

To verify the wrapper is working:

```bash
# Should show Maven 3.9.6
./mvnw -version

# Should build successfully
./mvnw clean install

# Should run the application
./mvnw spring-boot:run
```

---

## 🎉 You're Ready!

Maven Wrapper is now set up. You can use `./mvnw` (or `mvnw.cmd` on Windows) anywhere you would normally use `mvn`.

**No Maven installation required!** 🚀

---

## 📝 Quick Reference

| Command | PowerShell | CMD | Linux/Mac |
|---------|------------|-----|-----------|
| Build | `.\mvnw.cmd clean install` | `mvnw.cmd clean install` | `./mvnw clean install` |
| Run | `.\mvnw.cmd spring-boot:run` | `mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| Test | `.\mvnw.cmd test` | `mvnw.cmd test` | `./mvnw test` |
| Package | `.\mvnw.cmd package` | `mvnw.cmd package` | `./mvnw package` |
| Clean | `.\mvnw.cmd clean` | `mvnw.cmd clean` | `./mvnw clean` |
| Version | `.\mvnw.cmd -version` | `mvnw.cmd -version` | `./mvnw -version` |

---

**Happy Coding!** 🎯

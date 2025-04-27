# RASP Emulator and SimplerRASP Language Project

## Introduction

**SimplerRASP** is a complete educational stack designed to simulate the process of programming and running software on a theoretical machine architecture, the **RASP (Random Access Stored Program)**.
This project covers all stages of a full compilation and execution pipeline: from a high-level programming language to assembly and machine-level emulation, providing students and developers with hands-on experience in compiler design, virtual machine implementation, and system architecture.

Built entirely in **Java 21** using **JavaFX** for the GUI, the project also leverages **ANTLR4** for parser generation. The development and build environment is configured for **Maven**, **IntelliJ IDEA**, and **Temurin 21 JDK**, with instructions included for building Windows installer packages (.msi) using **WiX Toolset** and **jpackage**.

## Components
### 1. RASP Emulator

- Emulates a Von Neumann-style RASP machine.
- Supports symbolic assembly language.
- Instructions: arithmetic, memory access, jumps, I/O.
- Features a lexer, parser, and interpreter.
- Executable via CLI:
  ```bash
  java -jar rasp-emulator.jar
  ```

### 2. SimplerRASP Language & Compiler

#### 2.1 SimplerRASP Language
- A custom imperative programming language compiled to RASP Assembly.
- Features:
 - Integer & boolean variables
 - Arithmetic & logical operations
 - if / else, while, functions
 - I/O: cin -> var; and cout <- expr;
- Uses a formal EBNF grammar and is compiled using ANTLR4.

#### 2.1 SimplerRASP Compiler
- Translates SimplerRASP into RASP Assembly to be executed in the emulated RASP machine.
- Based on ANTLR's Visitor pattern.
- Handles:
 - Type checking
 - Scope management (stack-based memory model)
 - Recursive function calls

### 3. SimplerRASP IDE
- Built with JavaFX.
- Features:
 - Text editor for SimplerRASP code
 - One-click Compile and Run buttons
 - Built-in I/O console.
 - Requires java 21 and javafx sdk.

## Development Environment
Requirements
 - Temurin JDK 21
 - IntelliJ IDEA
 - Maven
 - JavaFX SDK https://openjfx.io/
 - ANTLR 4 Plugin (configured via Maven)


## Building and Packaging
1. Compile and Run Locally
    ```bash
    mvn clean install
    ```
**Important**: In IntelliJ IDEA, mark the folder `SimplerRasp/src/main/generated-sources` as `Generated Sources Root` after generating the parser with ANTLR.

Then run the project from IntelliJ using the main class `it.palex.raspgui.RaspGuiApp`.

### Running the JAR

To run the application, use the following command:

```bash
java -jar --module-path="C:\Users\user\Programs\JavaFx\javafx-sdk-21.0.7\lib" --add-modules "javafx.controls,javafx.fxml" runnable-jar/SimplerRaspGui-1.0.0-jar-with-dependencies.jar
```
- `--module-path="C:\Users\user\Programs\JavaFx\javafx-sdk-21.0.7\lib"`: Specifies the path to the **JavaFX SDK** libraries. Replace this with the path to your own JavaFX SDK installation. The `--module-path` is required to locate the JavaFX modules.

- `--add-modules "javafx.controls,javafx.fxml"`: Tells Java which JavaFX modules to load. `javafx.controls` is used for UI controls (like buttons, text fields), and `javafx.fxml` is used for loading FXML files (if your project uses them for layout).

- `runnable-jar/SimplerRaspGui-1.0.0-jar-with-dependencies.jar`: Specifies the path to the executable JAR file, which contains all necessary dependencies for running the **SimplerRaspGui** application.

The JavaFX SDK is necessary for the GUI components to function. Without specifying the --module-path, the application will not run properly.

### 📦 Building the MSI Installer using jpackage

2. Build MSI Installer for Windows
To create an .msi installer you need to install Wix Toolset:
 - Install WiX Toolset 3.x (https://github.com/wixtoolset/wix3/releases/)

To package the SimplerRASP project as a Windows `.msi` installer, you can use `jpackage`, which is included in JDK 21.

> ⚡ **Important:**  
> You must run the following command from the **root folder** of the project, where the **parent `pom.xml`** is located.

Open a PowerShell terminal and execute a command like the one below (make sure to modify the paths to yours):

```powershell
    jpackage `
    --type msi `
    --module-path "C:/Users/user/Programs/JavaFx/javafx-jmods-21.0.7" `
    --add-modules java.base,javafx.graphics,javafx.controls,javafx.fxml `
    --main-class it.palex.raspgui.RaspGuiApp `
    --main-jar SimplerRaspGui-1.0.0-jar-with-dependencies.jar `
    --vendor "Palex Sample SW" `
    --input SimplerRaspGui/runnable-jar `
    --win-shortcut `
    --win-menu `
    --name "SimplerRaspApp" `
    --icon SimplerRaspGui/src/main/resources/icons/icon.ico
```
After running the command, an `.msi` installer will be generated, which can be used to install the SimplerRaspApp application on Windows.

The path `C:/Users/user/Programs/JavaFx/javafx-jmods-21.0.7` should points to the JavaFX SDK jmods folder.
Ensure that the JavaFX SDK is properly downloaded and extracted to this path or adjust it according to your system.

#### 📋 jpackage Parameters used Table


| Parameter        | Description                                                                 |
|------------------|-----------------------------------------------------------------------------|
| --type msi       | Specifies that the output will be an MSI Windows installer.                 |
| --module-path    | Path to the JavaFX SDK jmods directory. Needed to bundle JavaFX modules.    |
| --add-modules    | List of Java and JavaFX modules required by the application.                |
| --main-class     | Fully qualified name of the JavaFX application's main class.               |
| --main-jar       | Name of the JAR file containing the application, including dependencies.    |
| --vendor         | Vendor name to be shown in the installer and installed program list.       |
| --input          | Directory containing the runnable JAR and any supporting files.            |
| --win-shortcut   | Creates a shortcut on the desktop/start menu during installation.          |
| --win-menu       | Adds the application to the Windows start menu.                             |
| --name           | Name of the application (used for the installer and app folder).           |
| --icon           | Path to the .ico icon file for the application shortcut and installer.     |

## 🚀 Running the Application

Once the installer is created you can launch it and install the app in your PC. 
- Write code in SimplerRASP language in the editor window.
- Click **Compile** to translate it into RASP Assembly.
- Click **Run** to execute it on the virtual RASP machine.
- View output and provide input via the embedded console.

```groovy
def factorial(number n): number {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

main {
    number factToCompute = 0;
    cin -> factToCompute;
    number result = factorial(factToCompute);
    cout <- result;
}
```

## Contributing
We welcome contributions from the community! If you would like to contribute to this project, please follow these guidelines:

Submit bug reports or feature requests by opening an issue on the project's GitHub repository.
Fork the repository, make your changes, and submit a pull request for review.
Follow coding standards and maintain clean code.
Set up your development environment by following the instructions in the README.

## License
This project is licensed under the GNU General Public License v3.0 (GPLv3). See the LICENSE file for details.

## Credits

We acknowledge and appreciate the following third-party libraries and resources used in this project:
- ANTLR
- OpenJFX
- WiX Toolset
- Eclipse Temurin JDK

## Contact
For any questions, feedback, or inquiries, feel free to reach out to us:

Email: alessandro.a.pagliaro@gmail.com  
GitHub: [My Profile](https://github.com/paaxel/)

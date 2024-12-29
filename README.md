# Java Todo App 🚀

## Description 📝

The **Java Todo App** is a desktop application built using **JavaFX** and **SQLite**. It allows users to manage their tasks effectively with a user-friendly graphical interface. The app includes features like user registration, login, task creation, task management, and more.

---

## Table of Contents 📑

1. [Features](#features-)
2. [Technology Stack](#technology-stack-)
3. [Setup Instructions](#setup-instructions-)
4. [Running the Application](#running-the-application-)
5. [Contribution Guidelines](#contribution-guidelines-)
6. [Contributors](#contributors-)
7. [License](#license-)

---

## Features ✅

### **User Authentication**
- Register new users.
- Login with existing credentials.

### **Task Management**
- Add new tasks with:
  - Title
  - Description
  - Due date
  - Category
  - Priority
- Mark tasks as complete.
- Delete tasks.
- Sort tasks by:
  - Due date
  - Priority
- Search for tasks by:
  - Title
  - Description
- Set dependencies between tasks.

### **Database Integration**
- Uses **SQLite** to store:
  - User data
  - Task data

---

## Technology Stack 💻

- **JavaFX 17.0.13**: For the graphical user interface (GUI).
- **SQLite**: For database storage.
- **FXML**: For defining the UI layout.

---

## Setup Instructions 🛠️

### **Prerequisites**
1. **Java Development Kit (JDK) 17 or later**:
   - Download and install the JDK from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) or [Adoptium](https://adoptium.net/).
2. **Visual Studio Code (VS Code)**:
   - Download and install VS Code from [here](https://code.visualstudio.com/).
3. **Java Extensions for VS Code**:
   - Install the following extensions in VS Code:
     - **Language Support for Java™ by Red Hat**
     - **Debugger for Java**
     - **Java Extension Pack** (optional, but recommended).

### **Clone the Repository**
1. Open a terminal or command prompt.
2. Run the following command to clone the repository:
   ```bash
   git clone https://github.com/your-repo-url/java-todo-app.git
   ```
3. Open project in Vs Code

---

## Running the Application ▶️

### **Set Up the Database**
1. SQLite database file (todo.db) included in project
2. Verify database location:
   - Should be in `src/main/resources/db/todo.db`

### **Configure JavaFX**
1. Project pre-configured with JavaFX 17.0.13
2. Verify lib folder contains required .jar files:
   - javafx.controls.jar
   - javafx.fxml.jar

### **Run the Application**
1. Open ToDoApp.java in VS Code
2. Run application:
   - Press F5 
   - OR: Click Run > Start Debugging
3. Login/Register window will appear to access main application

---

## Contribution Guidelines 📜

1. Fork the Repository
  - Fork repository to your GitHub account

2. Create a New Branch
  - Create branch for your feature:
    ```bash
    git checkout -b feature/your-feature-name
    ```

3. Make Your Changes
  - Implement changes
  - Test thoroughly before committing

4. Commit Your Changes
  - Add descriptive commit message:
    ```bash
    git commit -m "Add your commit message"
    ```

5. Push to the Branch
  - Push changes to your branch:
    ```bash
    git push origin feature/your-feature-name
    ```

6. Submit a Pull Request
  - Open pull request on GitHub
  - Provide clear description of changes

---

### Next Steps for Teammates 🚧

1. **Design the GUI**:
   - Use **FXML** for layout design:
     - Task list view
     - Task creation form
     - Task management controls

2. **Integrate with the Database**:
   - Ensure the GUI interacts with the SQLite database to:
     - Fetch task data
     - Update task information
     - Handle user operations

3. **Test the Application**:
   - Perform thorough testing to ensure:
     - GUI functionality
     - Database operations
     - User interactions

### Notes 📌

- **JavaFX 17.0.13**: The project is already configured to use JavaFX 17.0.13. No additional download is required.
- **Database**: The SQLite database (`todo.db`) is included in the project. Ensure it is correctly set up before running the app.

---

## Contributors 🧑‍🚀

* MAH QING FUNG
* HANA BINTI BADRUL HISHAM
* KUEH ZHI LING
* SAJEEV A/L JAYAPARAGASAM
* VERONICA YAPP YEE HONG
* JASMINE TEO GIE MING

---

## License

This project is licensed under the [MIT License](LICENSE).
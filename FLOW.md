# ToDo List App - Next Steps Flowchart 🗺️

## 1. Database Setup (SQLite) 🗃️
- [ ] Create SQLite database file
- [ ] Set up database tables
  ```sql
  - Users (id, username, password_hash, email)
  - Tasks (id, user_id, title, description, due_date, status)
  ```
- [ ] Create DatabaseConnection utility class
- [ ] Implement basic CRUD operations
  - [ ] UserDAO (create, read, update, delete)
  - [ ] TaskDAO (create, read, update, delete)

## 2. JavaFX GUI Implementation 🎨
### Login/Register System
- [ ] Create base FXML files
  - [ ] login.fxml
  - [ ] register.fxml
- [ ] Design Controllers
  - [ ] LoginController.java
  - [ ] RegisterController.java
- [ ] Style with CSS
  - [ ] Create styles.css
  - [ ] Design modern input fields
  - [ ] Style buttons and labels
  - [ ] Add animations/transitions

### Main Application Interface
- [ ] Create main.fxml
- [ ] Design MainController.java
- [ ] Implement key components
  - [ ] Task list view/table
  - [ ] Add/Edit task forms
  - [ ] Search functionality
  - [ ] Filter/Sort options
- [ ] Style with CSS
  - [ ] Task list styling
  - [ ] Custom buttons and controls
  - [ ] Responsive layout

## 3. Integration Phase 🔄
- [ ] Connect GUI to Database
  - [ ] Implement login/register functionality
  - [ ] Connect task CRUD operations
  - [ ] Add error handling
- [ ] Add Data Validation
  - [ ] Input validation
  - [ ] User feedback (alerts/notifications)
  - [ ] Error messages

## 4. Email Notification System Implementation 📧
- [ ] Create User Email Configuration
  - [ ] Allow users to input their email addresses during registration or in settings
- [ ] Implement Task Reminder Service
  - [ ] Create TaskReminderService class to check for tasks due within 24 hours
  - [ ] Implement logic to send email reminders
- [ ] Set Up Email Sending Functionality
  - [ ] Use JavaMail or an email API to send emails
  - [ ] Create EmailService class to handle email sending
- [ ] Schedule Email Notifications
  - [ ] Use ScheduledExecutorService to run the reminder check periodically (e.g., every hour)
- [ ] Integrate Email Notification with Main Application
  - [ ] Start the email notification service after user logs in successfully

## 5. Testing & Polish ✨
- [ ] Test all features
  - [ ] User authentication
  - [ ] Task management
  - [ ] Database operations
  - [ ] Email notifications
- [ ] Add final UI touches
  - [ ] Icons and images
  - [ ] Loading animations
  - [ ] Color scheme refinement
- [ ] Bug fixes and optimization

## Recommended Order of Implementation:
1. Set up SQLite database and basic connections
2. Create login/register GUI
3. Implement authentication
4. Create main interface GUI
5. Connect task management functionality
6. Implement email notification system
7. Add styling and polish
8. Final testing and refinement
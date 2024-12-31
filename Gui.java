/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

class Task {
    String title;
    Date dueDate;

    public Task(String title, Date dueDate) {
        this.title = title;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return title + " (Due: " + sdf.format(dueDate) + ")";
    }
}

class LoginFrame extends JFrame {
    private JTextField nameField, emailField;
    private List<Task> tasks = new ArrayList<>();

    public LoginFrame() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            if (validateFields()) {
                openTaskManager();
            }
        });
        add(loginButton);

        setLocationRelativeTo(null);
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return false;
        }
        return true;
    }

    private void openTaskManager() {
        TaskManagerFrame taskManager = new TaskManagerFrame(tasks);
        taskManager.setVisible(true);
        this.dispose();
    }
}

class TaskManagerFrame extends JFrame {
    private List<Task> tasks;
    private DefaultListModel<Task> listModel;
    private JList<Task> taskList;
    private JTextField taskField;
    private JTextField dateField;

    public TaskManagerFrame(List<Task> tasks) {
        this.tasks = tasks;
        setTitle("Task Manager");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Add task panel
        JPanel addPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        addPanel.add(new JLabel("Task:"));
        taskField = new JTextField();
        addPanel.add(taskField);

        addPanel.add(new JLabel("Due Date (yyyy-MM-dd):"));
        dateField = new JTextField();
        addPanel.add(dateField);

        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> addTask());
        addPanel.add(addButton);

        add(addPanel, BorderLayout.NORTH);

        // Task list
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteTask());

        JButton sortAscButton = new JButton("Sort Ascending");
        sortAscButton.addActionListener(e -> sortTasks(true));

        JButton sortDescButton = new JButton("Sort Descending");
        sortDescButton.addActionListener(e -> sortTasks(false));

        controlPanel.add(deleteButton);
        controlPanel.add(sortAscButton);
        controlPanel.add(sortDescButton);

        add(controlPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void addTask() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dueDate = sdf.parse(dateField.getText());
            Task task = new Task(taskField.getText(), dueDate);
            tasks.add(task);
            updateList();
            taskField.setText("");
            dateField.setText("");
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd");
        }
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            tasks.remove(selectedIndex);
            updateList();
        }
    }

    private void sortTasks(boolean ascending) {
        tasks.sort((t1, t2) -> ascending ?
                t1.dueDate.compareTo(t2.dueDate) :
                t2.dueDate.compareTo(t1.dueDate));
        updateList();
    }

    private void updateList() {
        listModel.clear();
        for (Task task : tasks) {
            listModel.addElement(task);
        }
    }
}

public class Gui {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
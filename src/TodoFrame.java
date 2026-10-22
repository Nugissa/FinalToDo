import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class TodoFrame extends JFrame {
    private final TaskService service = new TaskService();
    private final JTextField titleField = new JTextField();
    private final JTextArea newCommentArea = new JTextArea(3, 30);
    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    private final JTextArea commentArea = new JTextArea(4, 30);
    private final JLabel createdLabel = new JLabel("Создана: —");
    private final JLabel completedLabel = new JLabel("Выполнена: —");
    private final JButton saveButton = new JButton("Сохранить комментарий");
    private final JButton completeButton = new JButton("Выполнено");
    private final JButton deleteButton = new JButton("Удалить");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public TodoFrame() {
        super("Мои задачи");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(680, 650);
        setLocationRelativeTo(null);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newCommentArea.setLineWrap(true);
        newCommentArea.setWrapStyleWord(true);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);

        // Форма новой задачи.
        JButton addButton = new JButton("Добавить");
        titleField.setBorder(BorderFactory.createTitledBorder("Название новой задачи"));
        JScrollPane newCommentScroll = new JScrollPane(newCommentArea);
        newCommentScroll.setBorder(BorderFactory.createTitledBorder("Комментарий новой задачи"));
        JPanel form = new JPanel(new BorderLayout(5, 5));
        form.add(titleField, BorderLayout.NORTH);
        form.add(newCommentScroll, BorderLayout.CENTER);
        form.add(addButton, BorderLayout.SOUTH);

        // Подробности выбранной задачи.
        JPanel dates = new JPanel(new GridLayout(2, 1));
        dates.add(createdLabel);
        dates.add(completedLabel);
        JPanel buttons = new JPanel();
        buttons.add(saveButton);
        buttons.add(completeButton);
        buttons.add(deleteButton);
        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createTitledBorder(
                "Комментарий выбранной задачи (сохранение кнопкой)"));
        JPanel details = new JPanel(new BorderLayout(5, 5));
        details.add(dates, BorderLayout.NORTH);
        details.add(commentScroll, BorderLayout.CENTER);
        details.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(form, BorderLayout.NORTH);
        content.add(new JScrollPane(taskList), BorderLayout.CENTER);
        content.add(details, BorderLayout.SOUTH);
        setContentPane(content);

        addButton.addActionListener(event -> handleAdd());
        titleField.addActionListener(event -> newCommentArea.requestFocusInWindow());
        saveButton.addActionListener(event -> handleSaveComment());
        completeButton.addActionListener(event -> handleComplete());
        deleteButton.addActionListener(event -> handleDelete());
        taskList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) showDetails();
        });
        showDetails();

        try {
            service.loadTasks();
            for (Task task : service.getTasks()) listModel.addElement(task);
        } catch (IOException e) {
            showError(e);
            addButton.setEnabled(false);
            titleField.setEnabled(false);
            newCommentArea.setEnabled(false);
            taskList.setEnabled(false);
        }
    }

    private void showDetails() {
        Task task = taskList.getSelectedValue();
        boolean selected = task != null;
        commentArea.setEditable(selected);
        saveButton.setEnabled(selected);
        completeButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
        if (!selected) {
            commentArea.setText("");
            createdLabel.setText("Создана: —");
            completedLabel.setText("Выполнена: —");
            return;
        }
        commentArea.setText(task.getComment());
        commentArea.setCaretPosition(0);
        createdLabel.setText("Создана: " + task.getCreatedAt().format(DATE_FORMAT));
        completedLabel.setText(task.getCompletedAt() == null
                ? "Ещё не выполнена"
                : "Выполнена: " + task.getCompletedAt().format(DATE_FORMAT));
    }

    private void handleAdd() {
        try {
            Task task = service.addTask(titleField.getText(), newCommentArea.getText());
            listModel.addElement(task);
            taskList.setSelectedValue(task, true);
            titleField.setText("");
            newCommentArea.setText("");
            titleField.requestFocusInWindow();
        } catch (IOException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void handleSaveComment() {
        Task task = taskList.getSelectedValue();
        if (task == null) return;
        try {
            service.updateComment(task, commentArea.getText());
        } catch (IOException e) {
            showError(e);
        }
    }

    private void handleComplete() {
        Task task = taskList.getSelectedValue();
        if (task == null) return;
        try {
            service.completeTask(task);
            taskList.repaint();
            completedLabel.setText("Выполнена: " + task.getCompletedAt().format(DATE_FORMAT));
        } catch (IOException e) {
            showError(e);
        }
    }

    private void handleDelete() {
        Task task = taskList.getSelectedValue();
        if (task == null) return;
        try {
            service.deleteTask(task);
            listModel.removeElement(task);
            showDetails();
        } catch (IOException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
                "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}


//import javax.swing.*;
//import java.awt.BorderLayout;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.GridLayout;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.IOException;
//import java.time.format.DateTimeFormatter;
//
//public class TodoFrame extends JFrame {
//
//    private final TaskService service = new TaskService();
//
//    // Поля для создания новой задачи.
//    private final JTextField titleField = new JTextField();
//
//    private final JTextArea newCommentArea =
//            new JTextArea(3, 30);
//
//    // Список задач.
//    private final DefaultListModel<Task> listModel =
//            new DefaultListModel<>();
//
//    private final JList<Task> taskList =
//            new JList<>(listModel);
//
//    // Подробности выбранной задачи.
//    private final JTextArea commentArea =
//            new JTextArea(4, 30);
//
//    private final JLabel createdLabel =
//            new JLabel("Создана: —");
//
//    private final JLabel completedLabel =
//            new JLabel("Выполнена: —");
//
//    private final JButton saveCommentButton =
//            new JButton("Сохранить комментарий");
//
//    // Задача, чей комментарий сейчас открыт.
//    private Task displayedTask;
//
//    // Защита от повторной обработки программного изменения списка.
//    private boolean updatingList;
//
//    private static final DateTimeFormatter DATE_FORMAT =
//            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
//
//    public TodoFrame() {
//        super("Мои задачи");
//
//        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//
//        setSize(720, 700);
//        setMinimumSize(new Dimension(620, 600));
//        setLocationRelativeTo(null);
//
//        taskList.setSelectionMode(
//                ListSelectionModel.SINGLE_SELECTION
//        );
//
//        newCommentArea.setLineWrap(true);
//        newCommentArea.setWrapStyleWord(true);
//
//        commentArea.setLineWrap(true);
//        commentArea.setWrapStyleWord(true);
//
//        JButton addButton = new JButton("Добавить");
//        JButton completeButton = new JButton("Выполнено");
//        JButton deleteButton = new JButton("Удалить");
//
//        JPanel content =
//                new JPanel(new BorderLayout(10, 10));
//
//        content.setBorder(
//                BorderFactory.createEmptyBorder(12, 12, 12, 12)
//        );
//
//        // Верхняя часть окна: создание новой задачи.
//        JPanel newTaskPanel =
//                new JPanel(new BorderLayout(0, 6));
//
//        newTaskPanel.setBorder(
//                BorderFactory.createTitledBorder("Новая задача")
//        );
//
//        JPanel titlePanel =
//                new JPanel(new BorderLayout(8, 0));
//
//        titlePanel.add(
//                new JLabel("Название:"),
//                BorderLayout.WEST
//        );
//
//        titlePanel.add(
//                titleField,
//                BorderLayout.CENTER
//        );
//
//        newTaskPanel.add(
//                titlePanel,
//                BorderLayout.NORTH
//        );
//
//        JScrollPane newCommentScroll =
//                new JScrollPane(newCommentArea);
//
//        newCommentScroll.setBorder(
//                BorderFactory.createTitledBorder("Комментарий")
//        );
//
//        newTaskPanel.add(
//                newCommentScroll,
//                BorderLayout.CENTER
//        );
//
//        JPanel addPanel =
//                new JPanel(new FlowLayout(FlowLayout.RIGHT));
//
//        addPanel.add(addButton);
//
//        newTaskPanel.add(
//                addPanel,
//                BorderLayout.SOUTH
//        );
//
//        content.add(
//                newTaskPanel,
//                BorderLayout.NORTH
//        );
//
//        // Средняя часть окна: список задач.
//        JScrollPane listScroll =
//                new JScrollPane(taskList);
//
//        listScroll.setBorder(
//                BorderFactory.createTitledBorder("Задачи")
//        );
//
//        content.add(
//                listScroll,
//                BorderLayout.CENTER
//        );
//
//        // Нижняя часть окна: выбранная задача.
//        JPanel details =
//                new JPanel(new BorderLayout(0, 8));
//
//        details.setBorder(
//                BorderFactory.createTitledBorder("Выбранная задача")
//        );
//
//        JPanel dates =
//                new JPanel(new GridLayout(2, 1, 0, 4));
//
//        dates.add(createdLabel);
//        dates.add(completedLabel);
//
//        details.add(
//                dates,
//                BorderLayout.NORTH
//        );
//
//        JScrollPane commentScroll =
//                new JScrollPane(commentArea);
//
//        commentScroll.setBorder(
//                BorderFactory.createTitledBorder(
//                        "Комментарий — можно изменить"
//                )
//        );
//
//        details.add(
//                commentScroll,
//                BorderLayout.CENTER
//        );
//
//        JPanel buttons =
//                new JPanel(new FlowLayout(FlowLayout.RIGHT));
//
//        buttons.add(saveCommentButton);
//        buttons.add(completeButton);
//        buttons.add(deleteButton);
//
//        details.add(
//                buttons,
//                BorderLayout.SOUTH
//        );
//
//        content.add(
//                details,
//                BorderLayout.SOUTH
//        );
//
//        setContentPane(content);
//
//        // Подключение действий.
//        addButton.addActionListener(
//                event -> handleAdd()
//        );
//
//        // Enter в названии переводит курсор в комментарий.
//        titleField.addActionListener(
//                event -> newCommentArea.requestFocusInWindow()
//        );
//
//        completeButton.addActionListener(
//                event -> handleComplete()
//        );
//
//        deleteButton.addActionListener(
//                event -> handleDelete()
//        );
//
//        saveCommentButton.addActionListener(
//                event -> saveComment()
//        );
//
//        taskList.addListSelectionListener(event -> {
//            if (!event.getValueIsAdjusting()) {
//                handleSelection();
//            }
//        });
//
//        // Закрываем окно после проверки несохранённых правок.
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent event) {
//                handleClose();
//            }
//        });
//
//        showDetails(null);
//
//        // Загружаем задачи при запуске.
//        if (!loadTasks()) {
//            titleField.setEnabled(false);
//            newCommentArea.setEnabled(false);
//            addButton.setEnabled(false);
//            completeButton.setEnabled(false);
//            deleteButton.setEnabled(false);
//            taskList.setEnabled(false);
//        }
//    }
//
//    // Обновление списка с выбором указанной задачи.
//    private void refreshList(Task selectedTask) {
//        updatingList = true;
//
//        try {
//            listModel.clear();
//
//            for (Task task : service.getTasks()) {
//                listModel.addElement(task);
//            }
//
//            taskList.setSelectedValue(selectedTask, true);
//
//            showDetails(taskList.getSelectedValue());
//
//        } finally {
//            updatingList = false;
//        }
//    }
//
//    // Показ комментария и дат выбранной задачи.
//    private void showDetails(Task task) {
//        displayedTask = task;
//
//        commentArea.setEditable(task != null);
//        saveCommentButton.setEnabled(task != null);
//
//        if (task == null) {
//            commentArea.setText("");
//            createdLabel.setText("Создана: —");
//            completedLabel.setText("Выполнена: —");
//            return;
//        }
//
//        commentArea.setText(task.getComment());
//        commentArea.setCaretPosition(0);
//
//        createdLabel.setText(
//                "Создана: "
//                        + task.getCreatedAt().format(DATE_FORMAT)
//        );
//
//        completedLabel.setText(
//                task.getCompletedAt() == null
//                        ? "Ещё не выполнена"
//                        : "Выполнена: "
//                        + task.getCompletedAt().format(DATE_FORMAT)
//        );
//    }
//
//    // Обработка перехода к другой задаче.
//    private void handleSelection() {
//        if (updatingList) {
//            return;
//        }
//
//        Task requestedTask = taskList.getSelectedValue();
//
//        if (requestedTask == displayedTask) {
//            return;
//        }
//
//        if (!confirmCommentChange()) {
//            updatingList = true;
//
//            try {
//                taskList.setSelectedValue(displayedTask, true);
//            } finally {
//                updatingList = false;
//            }
//
//            return;
//        }
//
//        showDetails(requestedTask);
//    }
//
//    // Проверка несохранённого комментария.
//    private boolean confirmCommentChange() {
//        if (displayedTask == null
//                || commentArea.getText().equals(
//                displayedTask.getComment())) {
//
//            return true;
//        }
//
//        Object[] options = {
//                "Сохранить",
//                "Не сохранять",
//                "Отмена"
//        };
//
//        int choice = JOptionPane.showOptionDialog(
//                this,
//                "Комментарий к задаче «"
//                        + displayedTask.getTitle()
//                        + "» изменён. Сохранить его?",
//                "Несохранённый комментарий",
//                JOptionPane.YES_NO_CANCEL_OPTION,
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                options,
//                options[0]
//        );
//
//        if (choice == 0) {
//            return saveComment();
//        }
//
//        // «Не сохранять» разрешает действие.
//        // «Отмена» и закрытие диалога останавливают его.
//        return choice == 1;
//    }
//
//    // Сохранение комментария выбранной задачи.
//    private boolean saveComment() {
//        if (displayedTask == null) {
//            return false;
//        }
//
//        try {
//            service.updateComment(
//                    displayedTask,
//                    commentArea.getText()
//            );
//
//            return true;
//
//        } catch (IOException e) {
//            showSaveError(e);
//            return false;
//        }
//    }
//
//    // Добавление новой задачи.
//    private void handleAdd() {
//        if (!confirmCommentChange()) {
//            return;
//        }
//
//        try {
//            Task task = service.addTask(
//                    titleField.getText(),
//                    newCommentArea.getText()
//            );
//
//            refreshList(task);
//
//            titleField.setText("");
//            newCommentArea.setText("");
//
//            titleField.requestFocusInWindow();
//
//        } catch (IllegalArgumentException e) {
//            JOptionPane.showMessageDialog(
//                    this,
//                    e.getMessage(),
//                    "Ошибка ввода",
//                    JOptionPane.WARNING_MESSAGE
//            );
//
//        } catch (IOException e) {
//            showSaveError(e);
//        }
//    }
//
//    // Выполнение выбранной задачи.
//    private void handleComplete() {
//        Task task = displayedTask;
//
//        if (task == null) {
//            JOptionPane.showMessageDialog(
//                    this,
//                    "Сначала выберите задачу."
//            );
//            return;
//        }
//
//        if (!confirmCommentChange()) {
//            return;
//        }
//
//        try {
//            service.completeTask(task);
//            refreshList(task);
//
//        } catch (IOException e) {
//            showSaveError(e);
//        }
//    }
//
//    // Удаление выбранной задачи.
//    private void handleDelete() {
//        Task task = displayedTask;
//
//        if (task == null) {
//            JOptionPane.showMessageDialog(
//                    this,
//                    "Сначала выберите задачу."
//            );
//            return;
//        }
//
//        if (!confirmCommentChange()) {
//            return;
//        }
//
//        try {
//            service.deleteTask(task);
//            refreshList(null);
//
//        } catch (IOException e) {
//            showSaveError(e);
//        }
//    }
//
//    // Закрытие окна.
//    private void handleClose() {
//        if (confirmCommentChange()) {
//            dispose();
//        }
//    }
//
//    // Загрузка сохранённых задач.
//    private boolean loadTasks() {
//        try {
//            service.loadTasks();
//            refreshList(null);
//            return true;
//
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(
//                    this,
//                    "Не удалось загрузить задачи. "
//                            + "Изменения заблокированы.\n"
//                            + e.getMessage(),
//                    "Ошибка загрузки",
//                    JOptionPane.ERROR_MESSAGE
//            );
//
//            return false;
//        }
//    }
//
//    // Сообщение об ошибке сохранения.
//    private void showSaveError(IOException e) {
//        JOptionPane.showMessageDialog(
//                this,
//                "Изменение не сохранено. "
//                        + "Проверьте доступ к файлу "
//                        + "и повторите действие.\n"
//                        + e.getMessage(),
//                "Ошибка сохранения",
//                JOptionPane.ERROR_MESSAGE
//        );
//    }
//}

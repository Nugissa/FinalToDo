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

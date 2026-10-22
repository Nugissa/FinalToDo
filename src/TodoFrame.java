import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TodoFrame extends JFrame{
    private final TaskService service = new TaskService();
    private final JTextField titleField = new JTextField();
    private final JTextArea newCommentArea = new JTextArea(3,30);
    private final DefaultListModel <Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    private final JTextArea commentArea = new JTextArea(4,30);
    private final JLabel createdLabel = new JLabel("Создана: -");
    private final JLabel completedLabel = new JLabel("Выполнена: -");
    private final JButton saveButton = new JButton("Сохранить комментарий");
    private final JButton completeButton = new JButton("Выполнено");
    private final JButton deleteButton = new JButton("Удалить");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public TodoFrame () {
        super ("Мой задачи");
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
        JPanel form = new JPanel(new BorderLayout(5,5));
        form.add(titleField, BorderLayout.NORTH);
        form.add(newCommentScroll, BorderLayout.CENTER);
        form.add(addButton, BorderLayout.SOUTH);

        JPanel dates = new JPanel(new GridLayout(2,1));
        dates.add(createdLabel);
        dates.add(completedLabel);
        JPanel button = new JPanel();
        button.add(saveButton);
        button.add(completeButton);
        button.add(deleteButton);
        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createTitledBorder(
                "Комментарий выбранной задачи (сохранение кнопкой)" ));


    }
}

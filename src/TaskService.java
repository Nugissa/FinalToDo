import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.util.Properties;

public class TaskService {

    private final List<Task> tasks = new ArrayList<>();

    private final Path storageFile;
    private boolean loaded;

    public TaskService(){
        this(Path.of(
                System.getProperty("user.home"),
                "TodoApp",
                "tasks.properties"
        ));
    }

    public TaskService (Path storageFile) {
        this.storageFile = storageFile.toAbsolutePath();
    }

    public Task addTask (String title) throws IOException {
        return addTask(title, "");
    }

    public Task addTask (String title, String comment)
        throws IOException {

        ensureLoaded();

        Task task = new Task( title, comment);
        tasks.add(task);

        try {
            saveTasks();
        } catch(IOException e){
            tasks.remove(task);
            throw e;
        }
        return  task;
    }

    public void completeTask (Task task) throws IOException{
        ensureLoaded();
        requireTask(task);

        if (task.isCompleted()){
            return;
        }

        task.markComleted();

        try{
            saveTasks();
        } catch (Exception e){
            task.undoCompletion();
            throw e;
        }
    }

    public void updateComment (Task task, String comment)
        throws IOException {
        ensureLoaded();
        requireTask(task);

        String previous = task.getComment();
        task.setComment(comment);

        try{
            saveTasks();
        } catch (IOException e) {
            task.setComment(previous);
            throw e;
        }
    }

    public void deleteTask (Task task) throws IOException{
        ensureLoaded();

        int index = tasks.indexOf(task);

        if (index < 0) {
            throw new IllegalArgumentException(
                    "Задача не найдена"
            );
        }

        tasks.remove(index);

        try {
            saveTasks();
        } catch (IOException e) {
            tasks.add(index, task);
            throw e;
        }
    }

    public List<Task> getTasks () {return new ArrayList<>(tasks);}

    public void loadTasks() throws IOException {
        loaded = false;

        //Первый запуск: файла еще нет.

        if (Files.notExists(storageFile)) {
            tasks.clear();
            loaded = true;
            return;
        }

        List<Task> restored = new ArrayList<>();

        try {
            Properties data = new Properties();

            try (Reader reader = Files.newBufferedReader(
                    storageFile, StandardCharsets.UTF_8)) {
                data.load(reader);
            }

            int count = Integer.parseInt(
                    required(data, "task.count")
            );

            if (count < 0) {
                throw new IllegalArgumentException(
                        "Отрицательное число задач."
                );
            }

            for (int i = 0; i < count; i++) {
                String prefix = "task." + i + ".";

                String title = required(data, prefix + "title");
                String comment = data.getProperty(prefix + "comment", "");
                LocalDateTime createdAt = LocalDateTime.parse(
                        required(data, prefix + "createdAt")
                );

                String state = required(
                        data, prefix + "completed"
                );

                if (!state.equals("true")
                        && !state.equals("false")) {
                    throw new IllegalArgumentException("Неверное состояние задачи.");
                }

                String time = required(
                        data, prefix + "completedAt"
                );

                LocalDateTime completedAt = time.isEmpty()
                        ? null
                        : LocalDateTime.parse(time);

                Task task = new Task(
                        title,
                        comment,
                        createdAt,
                        Boolean.parseBoolean(state),
                        completedAt
                );
                restored.add(task);
            }
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new IOException(
                    "Некорректные данные в " + storageFile
                    + "+ " + e.getMessage(),
                    e
            );
        }

        tasks.clear();
        tasks.addAll(restored);
        loaded = true;

    }

    private void saveTasks() throws IOException{
        ensureLoaded();

        Properties data = new Properties();

        data.setProperty(
                "task.count",
                String.valueOf(tasks.size())
        );

        for (int i=0; i< tasks.size(); i++){
            Task task = tasks.get(i);
            String prefix = "task." + i + ".";

            data.setProperty(
                    prefix + "title",
                    task.getTitle()
            );

            data.setProperty(
                    prefix + "comment",
                    task.getComment()
            );

            data.setProperty(
                    prefix + "createdAt",
                    task.getCreatedAt().toString()
            );

            data.setProperty(
                    prefix + "completed",
                    String.valueOf(task.isCompleted())
            );

            data.setProperty(
                    prefix + "completedAt",
                    task.getCompletedAt()==null
                            ? ""
                            : task.getCompletedAt().toString()
            );
        }

        Files.createDirectories(storageFile.getParent());

        Path temporary = Files.createTempFile(
                storageFile.getParent(),
                "tasks-",
                ".tmp"
        );

        try {
            try (Writer writer = Files.newBufferedWriter(
                    temporary, StandardCharsets.UTF_8)){
                data.store(writer, "TodoApp");
            }

            Files.move(
                    temporary,
                    storageFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        }finally {
            try{
                Files.deleteIfExists(temporary);
            } catch (IOException ignored){

            }
        }
    };
    private String required(Properties data, String key){
        String value = data.getProperty(key);

        if (value == null) {
            throw new IllegalArgumentException(
                    "Отсутствует поле " + key
            );
        }
        return value;
    };

    private void ensureLoaded() throws IOException{
        if (!loaded) {
            throw new IOException(
                    "Список не загружается. Изминения заблдокированныю"
            );
        }
    }

    private void requireTask(Task task){
        if (!tasks.contains(task)) {
            throw new IllegalArgumentException(
                    "Задача не найдена."
            );
        }
    }
}

import java.time.LocalDateTime;

public class Task {
    private final String title;
    private String comment;
    private boolean completed;

    private final LocalDateTime createAt;
    private LocalDateTime completeAt;

    // Сохранить возможность создать задачу только с названием.
    public Task(String title) {
        this(title, "");
    }

    //Создание новой задачи.
    public Task(String title, String comment) {
        this(title, comment, LocalDateTime.now(), false, null);
    }

    //Востановление задачи из сохраненных данных
    public Task(String title, String comment,
                LocalDateTime createAt,
                boolean completed,
                LocalDateTime comleteAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Введите название задачи."
            );
        }
        if (createAt == null) {
            throw new IllegalArgumentException(
                    "Неуказана дата сохранения."
            );
        }
        if (completed && createAt == null) {
            throw new IllegalArgumentException(
                    "Не указана дата выполнения."
            );
        }
        if (!completed && completeAt != null) {
            throw new IllegalArgumentException(
                    "У невыполненной задачи есть дата выполнения."
            );
        }

        this.title = title.strip();
        setComment(comment);

        this.createAt = createAt;
        this.completed = completed;
        this.completeAt = comleteAt;
    }

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment == null ? "" : comment;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createAt;
    }

    public LocalDateTime getCompletedAt() {
        return completeAt;
    }


    public void markComleted(){
        if (!completed) {
            completeAt = LocalDateTime.now();
            completed = true;
        }
    }
    // Используется сервисом, если сохранить выполнение не удалось.

    void undoCompletion () {
        completed = false;
        completeAt = null;
    }
    @Override
    public String toString (){
        return (completed ? "[o] " : "[x] " ) + title;
    }

}

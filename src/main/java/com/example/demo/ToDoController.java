package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ToDoController {

    // --- UI Components ---
    @FXML private ListView<Task> taskListView;
    @FXML private Button markDoneButton;
    @FXML private Button deleteTaskButton;
    @FXML private Button sortPriorityButton;
    @FXML private Button addTaskButton;
    @FXML private Button toggleThemeButton;
    @FXML
    private VBox rootVBox;

    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> priorityComboBox;

    // --- Task Management ---
    private ObservableList<Task> tasks;
    private final String SAVE_FILE = "tasks.dat";

    // --- Theme State ---
    private String currentTheme = "lilac";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private double xOffset = 0;
    private double yOffset = 0;
    private Stage stage;

    @FXML
    public void initialize() {
        tasks = FXCollections.observableArrayList();
        taskListView.setItems(tasks);
        makeWindowDraggable(rootVBox);
        priorityComboBox.setItems(FXCollections.observableArrayList("High", "Medium", "Low"));

        addTaskButton.setOnAction(e -> {
            addTask();
            saveTasksToFile();
        });

        markDoneButton.setOnAction(e -> {
            markTaskAsDone();
            saveTasksToFile();
        });

        deleteTaskButton.setOnAction(e -> {
            deleteTask();
            saveTasksToFile();
        });

        sortPriorityButton.setOnAction(e -> {
            sortByPriority();
            saveTasksToFile();
        });

        toggleThemeButton.setOnAction(e -> switchTheme());
        toggleThemeButton.setText("💜");

        loadTasksFromFile();
        setupListViewCells();
        saveTasksToFile();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void addTask() {
        String title = titleField.getText().trim();

        String dueDate = (datePicker.getValue() != null) ? datePicker.getValue().format(formatter) : "No Date";

        String priority = priorityComboBox.getValue();

        if (title.isEmpty() || priority == null) {
            showAlert("Missing Info", "Enter a title and select a priority! <3");
            return;
        }

        Task newTask = new Task(title, dueDate, priority);
        tasks.add(newTask);

        titleField.clear();
        datePicker.setValue(null);
        priorityComboBox.setValue(null);
    }

    private void markTaskAsDone() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.markAsCompleted();
            taskListView.refresh();
        } else {
            showAlert("No Selection", "Select a task to mark as done.");
        }
    }

    private void deleteTask() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tasks.remove(selected);
        } else {
            showAlert("No Selection", "Select a task to delete.");
        }
    }

    private void sortByPriority() {
        FXCollections.sort(tasks, Comparator.comparingInt(t -> priorityOrder(t.getPriority())));
    }

    private int priorityOrder(String priority) {
        return switch (priority) {
            case "High" -> 1;
            case "Medium" -> 2;
            case "Low" -> 3;
            default -> 4;
        };
    }

    private void saveTasksToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(new ArrayList<>(tasks));
            System.out.println("✅ Tasks saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromFile() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Task> loadedTasks = (List<Task>) ois.readObject();
            tasks.setAll(loadedTasks);
            System.out.println("📂 Tasks loaded from file.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ---------- THEME SWITCHING ----------
    private void switchTheme() {
        Scene scene = toggleThemeButton.getScene();

        scene.getStylesheets().removeIf(s ->
                s.contains("style.css") || s.contains("blue.css") || s.contains("dark.css"));

        switch (currentTheme) {
            case "lilac" -> {
                scene.getStylesheets().add(getClass().getResource("/com/example/demo/dark.css").toExternalForm());
                toggleThemeButton.setText("🌙");
                currentTheme = "dark";
            }
            case "dark" -> {
                scene.getStylesheets().add(getClass().getResource("/com/example/demo/blue.css").toExternalForm());
                toggleThemeButton.setText("💙");
                currentTheme = "blue";
            }
            case "blue" -> {
                scene.getStylesheets().add(getClass().getResource("/com/example/demo/style.css").toExternalForm());
                toggleThemeButton.setText("💜");
                currentTheme = "lilac";
            }
        }
    }

    // ---------- LIST CELL STYLING ----------
    private void setupListViewCells() {
        taskListView.setCellFactory(listView -> {
            ListCell<Task> cell = new ListCell<>() {
                @Override
                protected void updateItem(Task task, boolean empty) {
                    super.updateItem(task, empty);
                    if (empty || task == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Map<String, Double> columnWidths = calculateMaxColumnWidths(tasks, Font.font("System", 12));

                        Label priorityTag = new Label(task.getPriority());
                        priorityTag.getStyleClass().add("priority-" + task.getPriority().toLowerCase());
                        priorityTag.setPrefWidth(columnWidths.get("priority"));
                        priorityTag.setAlignment(Pos.CENTER_LEFT);

                        Label title = new Label(task.getTitle());
                        title.setStyle("-fx-font-weight: normal;");
                        title.setPrefWidth(Math.max(columnWidths.get("title"), 150));
                        title.setAlignment(Pos.CENTER_LEFT);
                        title.setWrapText(false);

                        String statusText = task.isCompleted() ? "✔ Done" : "\uD83D\uDD52 Pending";
                        Label statusLabel = new Label(statusText);
                        statusLabel.setPrefWidth(columnWidths.get("status"));
                        statusLabel.setAlignment(Pos.CENTER_LEFT);

                        Label dueDateLabel = new Label(task.getDueDate());
                        dueDateLabel.setPrefWidth(columnWidths.get("dueDate"));
                        dueDateLabel.setAlignment(Pos.CENTER_LEFT);

                        if (!"No Date".equals(task.getDueDate())) {
                            try {
                                LocalDate due = LocalDate.parse(task.getDueDate(), formatter);
                                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due);
                                String msg = (daysLeft > 0)
                                        ? daysLeft + " day" + (daysLeft == 1 ? "" : "s") + " left"
                                        : (daysLeft == 0)
                                        ? "Due today!"
                                        : "Overdue by " + Math.abs(daysLeft) + " day" + (daysLeft == -1 ? "" : "s");
                                dueDateLabel.setTooltip(new Tooltip(msg));
                            } catch (Exception e) {
                                dueDateLabel.setTooltip(new Tooltip("Invalid date"));
                            }
                        }

                        HBox container = new HBox(priorityTag, title, statusLabel, dueDateLabel);
                        container.setSpacing(30);
                        container.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(container);
                    }
                }
            };

            // --- Drag and Drop Handlers ---
            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null) return;
                Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(Integer.toString(cell.getIndex()));
                dragboard.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragEntered(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    cell.setOpacity(0.3);
                }
            });

            cell.setOnDragExited(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    cell.setOpacity(1);
                }
            });

            cell.setOnDragDropped(event -> {
                if (cell.getItem() == null) return;

                Dragboard dragboard = event.getDragboard();
                boolean success = false;

                if (dragboard.hasString()) {
                    int draggedIndex = Integer.parseInt(dragboard.getString());
                    Task draggedTask = taskListView.getItems().get(draggedIndex);

                    int thisIndex = cell.getIndex();
                    ObservableList<Task> items = taskListView.getItems();

                    items.remove(draggedTask);
                    items.add(thisIndex, draggedTask);
                    taskListView.getSelectionModel().select(draggedTask);
                    success = true;
                }

                event.setDropCompleted(success);
                event.consume();
            });

            cell.setOnDragDone(DragEvent::consume);

            return cell;
        });
    }


    private Map<String, Double> calculateMaxColumnWidths(List<Task> tasks, Font font) {
        double maxPriority = 0, maxTitle = 0, maxStatus = 0, maxDueDate = 0;
        Text textHelper = new Text();
        textHelper.setFont(font);


        String[] priorities = {"High", "Medium", "Low"};
        maxPriority = 0;
        for (String p : priorities) {
            textHelper.setText(p);
            maxPriority = Math.max(maxPriority, textHelper.getLayoutBounds().getWidth());
        }

        for (Task t : tasks) {
            textHelper.setText(t.getTitle());
            maxTitle = Math.max(maxTitle, textHelper.getLayoutBounds().getWidth());

            String status = t.isCompleted() ? "✔ Done" : "\uD83D\uDD52 Pending";
            textHelper.setText(status);
            maxStatus = Math.max(maxStatus, textHelper.getLayoutBounds().getWidth());

            textHelper.setText(t.getDueDate());
            maxDueDate = Math.max(maxDueDate, textHelper.getLayoutBounds().getWidth());
        }

        double padding = 20;
        double extraTitlePadding = 20;

        Map<String, Double> widths = new HashMap<>();
        widths.put("priority", maxPriority + padding);
        widths.put("title", maxTitle + padding + extraTitlePadding);
        widths.put("status", maxStatus + padding);
        widths.put("dueDate", maxDueDate + padding);

        return widths;
    }

    // --- Window control handlers (called from FXML) ---
    @FXML
    private void handleMinimize() {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
    private void makeWindowDraggable(javafx.scene.Node node) {
        node.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        node.setOnMouseDragged(event -> {
            if (stage != null) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
    }

}

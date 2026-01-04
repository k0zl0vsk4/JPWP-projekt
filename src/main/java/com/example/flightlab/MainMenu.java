package com.example.flightlab;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainMenu extends Application {

    private Stage window;
    private Scene loginScene, menuScene, quizScene, scoresScene;

    private int quizIndex = 0;
    private int sessionScore = 0; //Points earned in this quiz session
    private List<Question> questions = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("FlightLab - System");

        createLoginScene();
        createMenuScene();
        createScoresScene();
        buildQuestions();

        window.setScene(loginScene);
        window.show();
    }

    private void createLoginScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50;");

        Label label = new Label("Witaj pilocie! Podaj swój nick:");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(20));

        TextField nameInput = new TextField();
        nameInput.setMaxWidth(300);
        nameInput.setPromptText("Wpisz imię...");

        Button loginButton = new Button("ZALOGUJ / STWÓRZ");
        loginButton.setPrefWidth(200);

        loginButton.setOnAction(e -> {
            String name = nameInput.getText();
            if (!name.trim().isEmpty()) {

                GameData.getInstance().login(name);
                updateTitle();
                window.setScene(menuScene);
            }
        });

        layout.getChildren().addAll(label, nameInput, loginButton);
        loginScene = new Scene(layout, 1000, 700);
    }

    private void createMenuScene() {
        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-image: url('/images/background.png'); -fx-background-size: cover;");
        // Jeśli nie masz tła, użyj gradientu:
        // menu.setStyle("-fx-background-color: linear-gradient(to bottom, #4A90E2, #003B7A);");

        Label title = new Label("FlightLab - Hangar");
        title.setFont(Font.font(36));
        title.setTextFill(Color.WHITE);

        Button simButton = new Button("Start Symulatora");
        Button quizButton = new Button("Panel Edukacyjny (Quiz ABCD)");
        Button scoresButton = new Button("Tabela Wyników");
        Button changeUserButton = new Button("Wyloguj / Zmień Gracza");
        Button exitButton = new Button("Wyjście");

        simButton.setPrefWidth(300);
        quizButton.setPrefWidth(300);
        scoresButton.setPrefWidth(300);
        changeUserButton.setPrefWidth(300);
        exitButton.setPrefWidth(300);

        simButton.setOnAction(e -> startSimulation());
        quizButton.setOnAction(e -> startQuiz());
        scoresButton.setOnAction(e -> { window.setScene(scoresScene); });
        changeUserButton.setOnAction(e -> window.setScene(loginScene));
        exitButton.setOnAction(e -> window.close());

        menu.getChildren().addAll(title, simButton, quizButton, scoresButton, changeUserButton, exitButton);
        menuScene = new Scene(menu, 1000, 700);
    }

    private void createScoresScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #202020;");

        Label label = new Label("RANKING PODNIEBNYCH ADEPTÓW");
        label.setTextFill(Color.GOLD);
        label.setFont(Font.font(30));

        TableView<Player> table = new TableView<>();
        table.setItems(GameData.getInstance().getPlayers());
        table.setMaxWidth(600);

        TableColumn<Player, String> nameCol = new TableColumn<>("Pilot");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(200);

        TableColumn<Player, Integer> quizCol = new TableColumn<>("Punkty Quiz");
        quizCol.setCellValueFactory(new PropertyValueFactory<>("quizPoints"));
        quizCol.setMinWidth(150);

        TableColumn<Player, Integer> missionCol = new TableColumn<>("Punkty Misji");
        missionCol.setCellValueFactory(new PropertyValueFactory<>("missionPoints"));
        missionCol.setMinWidth(150);

        TableColumn<Player, Integer> totalCol = new TableColumn<>("SUMA");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        totalCol.setMinWidth(100);

        table.getColumns().addAll(nameCol, quizCol, missionCol, totalCol);

        Button backButton = new Button("Powrót do Menu");
        backButton.setOnAction(e -> window.setScene(menuScene));

        layout.getChildren().addAll(label, table, backButton);
        scoresScene = new Scene(layout, 1000, 700);
    }

    private void startQuiz() {
        quizIndex = 0;
        sessionScore = 0;
        showQuizQuestion();
    }

    private void showQuizQuestion() {
        if (this.quizIndex >= this.questions.size()) {
            this.showQuizSummary();
        } else {
            Question q = this.questions.get(this.quizIndex);
            VBox box = new VBox(20.0);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-background-color: #202020;");

            Label qText = new Label(q.text);
            qText.setFont(Font.font(22.0));
            qText.setTextFill(Color.WHITE);
            box.getChildren().add(qText);

            if (q.imageUrl != null) {
                try {
                    Image img = new Image(this.getClass().getResourceAsStream("/" + q.imageUrl));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(300.0);
                    iv.setPreserveRatio(true);
                    box.getChildren().add(iv);
                } catch (Exception e) {
                    System.out.println("Błąd obrazka: " + q.imageUrl);
                }
            }

            if (q.audioUrl != null) {
                Button playAudio = new Button("Odtwórz nagranie");
                playAudio.setOnAction(e -> {
                    try {
                        String fullPath = "/" + q.audioUrl;
                        Media audio = new Media(this.getClass().getResource(fullPath).toString());
                        new MediaPlayer(audio).play();
                    } catch (Exception ex) {
                        System.out.println("Błąd audio.");
                    }
                });
                box.getChildren().add(playAudio);
            }

            Button a = new Button("A: " + q.a);
            Button b = new Button("B: " + q.b);
            Button c = new Button("C: " + q.c);
            Button d = new Button("D: " + q.d);

            a.setOnAction(e -> checkAnswer(1));
            b.setOnAction(e -> checkAnswer(2));
            c.setOnAction(e -> checkAnswer(3));
            d.setOnAction(e -> checkAnswer(4));

            VBox answers = new VBox(10.0, a, b, c, d);
            answers.setAlignment(Pos.CENTER);

            Button back = new Button("Powrót do menu");
            back.setOnAction(e -> window.setScene(menuScene));
            back.setPrefWidth(200.0);

            box.getChildren().addAll(answers, back);
            quizScene = new Scene(box, 1000.0, 700.0);
            window.setScene(quizScene);
        }
    }

    private void checkAnswer(int chosen) {
        Question currentQ = this.questions.get(this.quizIndex);
        boolean isCorrect = (chosen == currentQ.correct);

        if (isCorrect) {
            sessionScore++;

            Player current = GameData.getInstance().getCurrentPlayer();
            if (current != null) {
                current.addQuizPoint();
                GameData.getInstance().saveData();
            }
        }
        showFeedback(isCorrect, currentQ);
    }

    private void showFeedback(boolean correct, Question q) {
        VBox feedbackBox = new VBox(20.0);
        feedbackBox.setAlignment(Pos.CENTER);
        feedbackBox.setStyle(correct ? "-fx-background-color: #27A65B;" : "-fx-background-color: #C0392B;");

        Label status = new Label(correct ? "DOBRZE!" : "ŹLE!");
        status.setFont(Font.font(36.0));
        status.setTextFill(Color.WHITE);

        if (!correct) {
            Label explanation = new Label("Poprawna odpowiedź: " + getAnswerText(q.correct) + ".");
            explanation.setFont(Font.font(18.0));
            explanation.setTextFill(Color.WHITE);
            feedbackBox.getChildren().add(explanation);
        }

        Button nextButton = new Button("Następne Pytanie");
        nextButton.setOnAction(e -> {
            quizIndex++;
            showQuizQuestion();
        });

        feedbackBox.getChildren().addAll(status, nextButton);
        window.setScene(new Scene(feedbackBox, 1000.0, 700.0));
    }

    private String getAnswerText(int index) {
        Question q = this.questions.get(this.quizIndex);
        return switch (index) {
            case 1 -> "A: " + q.a;
            case 2 -> "B: " + q.b;
            case 3 -> "C: " + q.c;
            case 4 -> "D: " + q.d;
            default -> "Nieznana";
        };
    }

    private void showQuizSummary() {
        VBox box = new VBox(25);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1B1B1B;");

        Label result = new Label("Wynik tej sesji: " + sessionScore + " / " + questions.size());
        result.setFont(Font.font(26));
        result.setTextFill(Color.WHITE);

        Player p = GameData.getInstance().getCurrentPlayer();
        Label totalInfo = new Label("Całkowite punkty quizowe gracza " + p.getName() + ": " + p.getQuizPoints());
        totalInfo.setTextFill(Color.LIGHTGRAY);
        totalInfo.setFont(Font.font(18));

        Button back = new Button("Powrót do menu");
        back.setOnAction(e -> window.setScene(menuScene));

        box.getChildren().addAll(result, totalInfo, back);
        window.setScene(new Scene(box, 1000, 700));
    }

    private void buildQuestions() {
        questions.clear();
        questions.add(new Question("Co powoduje wzrost siły nośnej skrzydeł?", "Zwiększenie prędkości lotu", "Zmniejszenie kąta natarcia", "Zwiększenie masy samolotu", "Wyłączenie silnika", 1, null, null));
        questions.add(new Question("Jaką część samolotu pokazano na ilustracji?", "Lotki", "Klapy", "Ster wysokości", "Podwozie", 2, "images/flaps.jpg", null));
        questions.add(new Question("Co oznacza usłyszany komunikat?", "Gwałtowne przeciągnięcie", "Dużą prędkość", "Silny boczny wiatr", "Awarię silnika", 2, null, "audio/overspeed.mp3"));
        questions.add(new Question("Jaki kolor ma światło nawigacyjne na lewym skrzydle samolotu?", "Zielony", "Czerwony", "Biały", "Żółty", 2, null, null));
        questions.add(new Question("Jaki kod transpondera oznacza sytuację awaryjną?", "7500", "7600", "7777", "7700", 4, null, null));
        questions.add(new Question("Co oznacza prędkość V1?", "Prędkość oderwania od ziemi", "Prędkość decyzji (startu nie można przerwać)", "Maksymalną prędkość przelotową", "Prędkość lądowania", 2, null, null));
    }

    private void startSimulation() {
        try {
            new FlightLab_Main().start(window);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateTitle() {
        Player p = GameData.getInstance().getCurrentPlayer();
        if (p != null) {
            window.setTitle("FlightLab - Pilot: " + p.getName());
        }
    }

    public static class Question {
        String text, a, b, c, d;
        int correct;
        String imageUrl;
        String audioUrl;

        public Question(String text, String a, String b, String c, String d, int correct, String imageUrl, String audioUrl) {
            this.text = text;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.correct = correct;
            this.imageUrl = imageUrl;
            this.audioUrl = audioUrl;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
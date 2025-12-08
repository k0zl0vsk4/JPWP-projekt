package com.example.flightlab;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainMenu extends Application {

    private Stage window;
    private Scene menuScene, eduScene, quizScene, simulationScene;

    private int quizIndex = 0;
    private int score = 0;

    private List<Question> questions = new ArrayList<>();

    @Override

    public void start(Stage primaryStage) {

        window = primaryStage;
        window.setTitle("FlightLab - Menu");

        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: linear-gradient(to bottom, #4A90E2, #003B7A);");

        Label title = new Label("FlightLab - Nauka i symulator");
        title.setFont(Font.font(36));
        title.setTextFill(Color.WHITE);

        Button simButton = new Button("Start Symulatora");
        Button eduButton = new Button("Panel Edukacyjny (Quiz ABCD)");
        Button exitButton = new Button("Wyjście");

        simButton.setPrefWidth(300);
        eduButton.setPrefWidth(300);
        exitButton.setPrefWidth(300);

        simButton.setOnAction(e -> startSimulation());
        eduButton.setOnAction(e -> startQuiz());
        exitButton.setOnAction(e -> window.close());

        menu.getChildren().addAll(title, simButton, eduButton, exitButton);
        menuScene = new Scene(menu, 1000, 700);

        buildQuestions();

        window.setScene(menuScene);
        window.show();

    }

    private void buildQuestions() {
        questions.clear();

        //Pytanie w formie tekstu:
        questions.add(new Question(
                "Co powoduje wzrost siły nośnej skrzydeł?",
                "Zwiększenie prędkości lotu",
                "Zmniejszenie kąta natarcia",
                "Zwiększenie masy samolotu",
                "Wyłączenie silnika",
                1,
                null,
                null
        ));

        //Pytanie w formie graficznej:
        questions.add(new Question(
                "Jaką część samolotu pokazano na ilustracji?",
                "Lotki",
                "Klapę",
                "Ster wysokości",
                "Podwozie",
                2,
                "images/flaps.jpg",
                null
        ));


        //Pytanie w formie dźwiękowej:
        questions.add(new Question(
                "Co oznacza usłyszany komunikat?",
                "Gwałtowne przeciągnięcie",
                "Dużą prędkość",
                "Silny boczny wiatr",
                "Awarię silnika",
                2,
                null,
                "audio/overspeed.mp3"
        ));
    }

    private void startQuiz() {
        quizIndex = 0;
        score = 0;
        showQuizQuestion();
    }

    private void showQuizQuestion() {
        if (quizIndex >= questions.size()) {
            showQuizSummary();
            return;
        }


        Question q = questions.get(quizIndex);

        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #202020;");

        Label qText = new Label(q.text);
        qText.setFont(Font.font(22));
        qText.setTextFill(Color.WHITE);
        box.getChildren().add(qText);

        if (q.imageUrl != null) {

            Image img = new Image(getClass().getResourceAsStream("/" + q.imageUrl));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(500);
            iv.setPreserveRatio(true);
        }

        if (q.audioUrl != null) {
            Button playAudio = new Button("Odtwórz nagranie");
            playAudio.setOnAction(e -> {
                try {
                    Media audio = new Media(getClass().getResource(q.audioUrl).toString());
                    new MediaPlayer(audio).play();
                } catch (Exception ex) {
                    System.out.println("Brak pliku audio");
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

        VBox answers = new VBox(10, a, b, c, d);
        answers.setAlignment(Pos.CENTER);

        Button back = new Button("Powrót do menu");
        back.setOnAction(e -> window.setScene(menuScene));
        back.setPrefWidth(200);

        box.getChildren().addAll(answers, back);

        quizScene = new Scene(box, 1000, 700);
        window.setScene(quizScene);
    }

    private void checkAnswer(int chosen) {
        if (chosen == questions.get(quizIndex).correct)
            score++;

        quizIndex++;
        showQuizQuestion();
    }

    private void showQuizSummary() {
        VBox box = new VBox(25);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1B1B1B;");

        Label result = new Label("Twój wynik: " + score + " / " + questions.size());
        result.setFont(Font.font(26));
        result.setTextFill(Color.WHITE);

        Button back = new Button("Powrót do menu");
        back.setOnAction(e -> window.setScene(menuScene));

        box.getChildren().addAll(result, back);

        window.setScene(new Scene(box, 1000, 700));
    }


    private void startSimulation() {
        try {
            new FlightLab_Main().start(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Question {
        String text, a, b, c, d;
        int correct;
        String imageUrl;
        String audioUrl;

        public Question(String text, String a, String b, String c, String d,
                        int correct, String imageUrl, String audioUrl) {
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
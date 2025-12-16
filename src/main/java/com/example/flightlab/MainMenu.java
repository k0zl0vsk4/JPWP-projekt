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
import javafx.scene.Node;

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
        menu.setStyle("-fx-background-image: url('/images/background.png'); " + "-fx-background-size: cover;");

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
                "Klapy",
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
        if (this.quizIndex >= this.questions.size()) {
            this.showQuizSummary();
        } else {
            Question q = (Question)this.questions.get(this.quizIndex);
            VBox box = new VBox((double)20.0F);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-background-color: #202020;");

            Label qText = new Label(q.text);
            qText.setFont(Font.font((double)22.0F));
            qText.setTextFill(Color.WHITE);
            box.getChildren().add(qText);

            if (q.imageUrl != null) {
                try {
                    Image img = new Image(this.getClass().getResourceAsStream("/" + q.imageUrl));
                    ImageView iv = new ImageView(img);

                    //Ustalenie wymiarów obrazu, by widoczne było pytanie i odpowiedzi
                    iv.setFitWidth((double)300.0F);
                    iv.setPreserveRatio(true);

                    box.getChildren().add(iv);
                } catch (Exception e) {
                    System.out.println("Nie udało się załadować obrazu: " + q.imageUrl);
                }
            }

            if (q.audioUrl != null) {
                Button playAudio = new Button("Odtwórz nagranie");
                playAudio.setOnAction((e) -> {
                    try {
                        String fullPath = "/" + q.audioUrl;
                        Media audio = new Media(this.getClass().getResource(fullPath).toString());
                        (new MediaPlayer(audio)).play();
                    } catch (Exception var4) {
                        System.out.println("Brak pliku audio dla ścieżki: ");
                    }
                });
                box.getChildren().add(playAudio);
            }

            Button a = new Button("A: " + q.a);
            Button b = new Button("B: " + q.b);
            Button c = new Button("C: " + q.c);
            Button d = new Button("D: " + q.d);
            a.setOnAction((e) -> this.checkAnswer(1));
            b.setOnAction((e) -> this.checkAnswer(2));
            c.setOnAction((e) -> this.checkAnswer(3));
            d.setOnAction((e) -> this.checkAnswer(4));

            VBox answers = new VBox((double)10.0F, new Node[]{a, b, c, d});
            answers.setAlignment(Pos.CENTER);

            Button back = new Button("Powrót do menu");
            back.setOnAction((e) -> this.window.setScene(this.menuScene));
            back.setPrefWidth((double)200.0F);

            box.getChildren().addAll(new Node[]{answers, back});

            this.quizScene = new Scene(box, (double)1000.0F, (double)700.0F);
            this.window.setScene(this.quizScene);
        }
    }

    private void checkAnswer(int chosen) {
        Question currentQ = this.questions.get(this.quizIndex);
        boolean isCorrect = (chosen == currentQ.correct);
        if (isCorrect) {
            ++this.score;
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
            ++this.quizIndex;
            this.showQuizQuestion();
        });

        feedbackBox.getChildren().addAll(status, nextButton);
        this.window.setScene(new Scene(feedbackBox, 1000.0, 700.0));
    }

    private String getAnswerText(int index) {
        Question q = this.questions.get(this.quizIndex);
        return switch (index) {
            case 1 -> "A: " + q.a;
            case 2 -> "B: " + q.b;
            case 3 -> "C: " + q.c;
            case 4 -> "D: " + q.d;
            default -> "Odpowiedź nie jest znana";
        };
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
package com.example.flightlab;

import com.example.flightlab.GameData;
import com.example.flightlab.Player;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class FlightLab_Main extends Application {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;

    private static final double DESTINATION_X = 5000.0; //goal to fly: 5k meters
    private static final double RUNWAY_LENGTH = 1500.0; //length in meters

    private Plane plane;
    private javafx.scene.image.Image planeImage;
    private boolean up, down, left, right, quickClimb;

    private double windX = 0.0;
    private double windY = 0.0;

    private long startTime;
    private boolean missionActive = false;
    private boolean gameWon = false;

    private double cameraX = 0;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        try {
            planeImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/images/plane_model.png"), 80, 40, true, true);
        } catch (Exception e) {
            System.out.println("Błąd: Nie udało się załadować grafiki samolotu!");
        }

        plane = new Plane(100, 400);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(this::onKeyPressed);
        scene.setOnKeyReleased(this::onKeyReleased);

        primaryStage.setTitle("Flight Lab - Misja");
        primaryStage.setScene(scene);
        primaryStage.show();

        startTime = System.currentTimeMillis();

        AnimationTimer loop = new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) last = now;
                double dt = (now - last) / 1_000_000_000.0;
                last = now;

                update(dt);
                render(gc);
            }
        };
        loop.start();
    }

    private void onKeyPressed(KeyEvent e) {
        KeyCode c = e.getCode();
        switch (c) {
            case W -> up = true;
            case S -> down = true;
            case A -> left = true;
            case D -> right = true;
            case SPACE -> quickClimb = true;
            case ENTER -> {
                if (gameWon) {
                    resetGame(); //reset after winning
                } else {
                    missionActive = !missionActive;
                    if (missionActive) startTime = System.currentTimeMillis();
                }
            }
            case ESCAPE -> {
                try {
                    new MainMenu().start((Stage) ((Scene) e.getSource()).getWindow());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            default -> {
            }
        }
    }

    private void onKeyReleased(KeyEvent e) {
        KeyCode c = e.getCode();
        switch (c) {
            case W -> up = false;
            case S -> down = false;
            case A -> left = false;
            case D -> right = false;
            case SPACE -> quickClimb = false;
            default -> {
            }
        }
    }

    private void resetGame() {
        plane = new Plane(100, 400);
        gameWon = false;
        missionActive = false;
        cameraX = 0;
    }

    private void update(double dt) {
        if (gameWon) return; //Pause after winning the game

        //W/S keys control up/down
        if (up) plane.turn(-60 * dt);
        if (down) plane.turn(60 * dt);

        //A/D keys throttle control
        if (left) plane.throttle = clamp(plane.throttle - 0.5 * dt, 0, 1);
        if (right) plane.throttle = clamp(plane.throttle + 0.5 * dt, 0, 1);

        if (quickClimb) plane.pitch += 20 * dt;

        plane.updatePhysics(dt, windX, windY);

        if (plane.y > HEIGHT - 50) {
            plane.y = HEIGHT - 50;
            plane.vy = 0;
            plane.onGround = true;
        } else {
            plane.onGround = false;
        }

        if (plane.y < 20) {
            plane.y = 20;
            if (plane.vy < 0) {
                plane.vy = 0;
            }
        }

        if (plane.x > DESTINATION_X && plane.x < DESTINATION_X + RUNWAY_LENGTH) {
            if (plane.onGround && plane.getSpeed() < 5.0) {
                if (!gameWon) {
                    gameWon = true;
                    missionActive = false;

                    Player current = GameData.getInstance().getCurrentPlayer();
                    if (current != null) {
                        current.addMissionPoints(50);
                        GameData.getInstance().saveData();
                        System.out.println("Przyznano punkty dla: " + current.getName());
                    }
                }
            }
        }

        cameraX = Math.max(0, plane.x - 200); //moving background

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (missionActive && elapsed % 10 == 0 && elapsed != 0) {
            windX = (Math.sin(elapsed) * 40); //temporary wind gust in m/s
        } else {
            windX *= 0.95;
        }

        plane.fuel = Math.max(0, plane.fuel - 0.01 * plane.throttle * dt); //consumption of fuel
        if (plane.fuel <= 0)
            plane.throttle = Math.max(0, plane.throttle - 0.5 * dt); //reducing thrust if no fuel on board
    }

    private void render(GraphicsContext g) {
        g.setFill(Color.web("#4986c4")); //sky
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.save();
        g.translate(-cameraX, 0);

        g.setFill(Color.web("#266308")); // horizon
        g.fillRect(cameraX, HEIGHT - 80, WIDTH, 80);

        g.setFill(Color.DARKGRAY);
        g.fillRect(0, HEIGHT - 80, 1500, 80); //runway

        int startMarker = (int) (cameraX / 500) * 500;
        for (int i = startMarker; i < startMarker + WIDTH + 500; i += 500) {
            if (i > 1500) {
                g.setFill(Color.LIGHTGREEN);
                g.fillRect(i, HEIGHT - 80, 20, 80);
            } else {
                g.setFill(Color.WHITE);
                g.fillRect(i, HEIGHT - 45, 100, 10);
            }
        }

        g.setFill(Color.RED);
        g.fillRect(DESTINATION_X, HEIGHT - 150, 10, 70);
        g.fillPolygon(new double[]{DESTINATION_X, DESTINATION_X + 50, DESTINATION_X},
                new double[]{HEIGHT - 150, HEIGHT - 135, HEIGHT - 120}, 3);

        drawPlane(g);
        g.restore();

        drawHUD(g);
        drawMissionInfo(g);

        if(gameWon) {
        g.setFill(Color.rgb(0, 0, 0, 0.7));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFill(Color.LIME);
        g.setFont(Font.font("Arial", 48));
        g.fillText("MISSION COMPLETE!", 280, 300);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Arial", 24));
        g.fillText("Punkty zapisane, naciśnij ENTER, by uruchomić ponownie", 300, 350);
    }
}

    private void drawRunway(GraphicsContext g, double startX) {
        g.setFill(Color.DARKGRAY);
        g.fillRect(startX, HEIGHT - 80, RUNWAY_LENGTH, 80);
        g.setFill(Color.WHITE);
        for (double j = startX; j < startX + RUNWAY_LENGTH; j += 100) {
            g.fillRect(j, HEIGHT - 45, 60, 10);
        }
    }

    private void drawPlane (GraphicsContext g){
            g.save();
            g.translate(plane.x, plane.y);
            g.rotate(plane.angle);

            if (planeImage != null) {
                double imgWidth = planeImage.getWidth();
                double imgHeight = planeImage.getHeight();
                g.drawImage(planeImage, -imgWidth / 2.0, -imgHeight / 2.0, imgWidth, imgHeight);
            } else {
                g.setFill(Color.RED);
                g.fillRect(-30, -10, 60, 20);
            }
            g.restore();
    }

    private void drawHUD (GraphicsContext g){
            g.setFill(Color.rgb(0, 0, 0, 0.6));
            g.fillRoundRect(10, 10, 260, 120, 8, 8);

            g.setFill(Color.WHITE);
            g.setFont(Font.font(14));
            g.fillText(String.format("Altitude: %.0f m", Math.max(0, (HEIGHT - plane.y))), 20, 34);
            g.fillText(String.format("Speed : %.1f m/s", plane.getSpeed()), 20, 56);
            g.fillText(String.format("Heading: %.0f°", plane.angle), 20, 78);
            g.fillText(String.format("Throttle: %.0f%%", plane.throttle * 100), 20, 100);

            g.setFill(Color.WHITE);
            g.fillText("Fuel", 150, 34);
            g.setStroke(Color.GRAY);
            g.strokeRect(150, 40, 100, 14);
            g.setFill(Color.LIMEGREEN);
            g.fillRect(150, 40, plane.fuel, 14);
    }

    private void drawMissionInfo (GraphicsContext g){
            g.setFill(Color.rgb(255, 255, 255, 0.9));
            g.setFont(Font.font(16));
            g.fillText(missionActive ? "Mission: ACTIVE (Press ENTER to toggle)" : "Mission: INACTIVE (Press ENTER to start)", 300, 30);

            g.setFont(Font.font(12));
            g.fillText("Controls: W/S throttle, A/D turn, SPACE climb", 300, 50);
            g.fillText("Goal: Take off, follow heading, land on runway", 300, 68);

            if (plane.fuel < 20) {
                g.setFill(Color.RED);
                g.fillText("WARNING!: Low fuel!", 300, 92);
            }
            if (plane.onGround && missionActive) {
                g.setFill(Color.ORANGE);
                g.fillText("Status: On ground. Ready for next mission.", 300, 110);
            }
    }

    private static double clamp ( double v, double a, double b){
            return Math.max(a, Math.min(b, v));
    }

    private static class Plane {
            double x, y;
            double vx = 0, vy = 0;
            double angle = 0; // degrees (0 = rightwards)
            double pitch = 0; // small pitch variable for lift influence
            double throttle = 0.2; // 0..1
            double fuel = 100;
            boolean onGround = false;

            Plane(double x, double y) {
                this.x = x;
                this.y = y;
            }

            void turn(double dAngle) {
                angle = (angle + dAngle) % 360;
            }

            double getSpeed() {
                return Math.hypot(vx, vy);
            }

            void updatePhysics(double dt, double windX, double windY) {

                double rad = Math.toRadians(angle);
                double thrust = 200 * throttle;
                double ax = Math.cos(rad) * thrust;
                double ay = Math.sin(rad) * thrust; //thrust generating forward acceleration in direction of angle


                double forwardSpeed = getSpeed();
                double lift = 50 * (forwardSpeed / 50.0) * (1 + pitch * 0.5); //lift proportional to forward speed and small pitch

                double gravity = 9.81 * 20; //gravity is scaled for pixel world

                double dragX = -vx * 0.5;
                double dragY = -vy * 0.5;

                vx += (ax + dragX + windX * 0.2) * dt;
                vy += ((-lift) + ay + dragY + gravity + windY * 0.2) * dt;

                x += vx * dt;
                y += vy * dt;

                if (x < 0) {
                    x = 0;
                    vx = 0;
                }

                pitch *= 0.96;
            }
        }

        public static void main (String[]args){
            launch(args);
        }
    }

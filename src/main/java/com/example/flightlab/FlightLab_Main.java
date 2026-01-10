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

            //Flap Control:
            case F -> plane.toggleFlaps();

            case ENTER -> {
                if (gameWon) {
                    resetGame();
                } else {
                    missionActive = !missionActive;
                    if (missionActive) startTime = System.currentTimeMillis();
                }
            }
            case ESCAPE -> {
                try {
                    new MainMenu().start((Stage)((Scene)e.getSource()).getWindow());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            default -> {}
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
            default -> {}
        }
    }

    private void resetGame() {
        plane = new Plane(100, 400);
        gameWon = false;
        missionActive = false;
        cameraX = 0;
        startTime = System.currentTimeMillis(); //Time reset
    }

    private void update(double dt) {
        if (gameWon) return;

        //Controls:
        if (up) plane.turn(-60 * dt);
        if (down) plane.turn(60 * dt);
        if (left) plane.throttle = clamp(plane.throttle - 0.5 * dt, 0, 1);
        if (right) plane.throttle = clamp(plane.throttle + 0.5 * dt, 0, 1);
        if (quickClimb) plane.pitch += 20 * dt;

        plane.updatePhysics(dt, windX, windY);

        //Ground and braking:
        if (plane.y > HEIGHT - 50) {
            plane.y = HEIGHT - 50;
            plane.vy = 0;
            plane.onGround = true;

            //Wheel braking only when the throttle is low
            if (plane.throttle < 0.1) {
                plane.vx *= 0.95;
            }
        } else {
            plane.onGround = false;
        }

        //Upper limit (ceiling)
        if (plane.y < 20) {
            plane.y = 20;
            if (plane.vy < 0) plane.vy = 0;
        }

        //WINNING CONDITION AND SCORING
        if (plane.x > DESTINATION_X && plane.x < DESTINATION_X + RUNWAY_LENGTH) {
            if (plane.onGround && plane.getSpeed() < 5.0) {
                if (!gameWon) {
                    gameWon = true;
                    missionActive = false;

                    int basePoints = 50;

                    //1. Bonus for using flaps (20 points)
                    int flapsBonus = plane.flapsExtended ? 20 : 0;

                    //2. Time Bonus (Max 50 points - time in seconds)
                    long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                    int timeBonus = Math.max(0, 50 - (int)elapsedSeconds); //If flying longer than 50s - the bonus is 0.

                    int totalPoints = basePoints + flapsBonus + timeBonus;

                    Player current = GameData.getInstance().getCurrentPlayer();
                    if (current != null) {
                        current.addMissionPoints(totalPoints);
                        GameData.getInstance().saveData();

                        System.out.println("MISJA UKOŃCZONA");
                        System.out.println("Baza: " + basePoints);
                        System.out.println("Bonus Klapy: " + flapsBonus);
                        System.out.println("Bonus Czas: " + timeBonus + " (Czas: " + elapsedSeconds + "s)");
                        System.out.println("RAZEM: " + totalPoints);
                    }
                }
            }
        }

        cameraX = Math.max(0, plane.x - 200);

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (missionActive && elapsed % 10 == 0 && elapsed != 0) {
            windX = (Math.sin(elapsed) * 40);
        } else {
            windX *= 0.95;
        }

        plane.fuel = Math.max(0, plane.fuel - 0.01 * plane.throttle * dt);
        if (plane.fuel <= 0)
            plane.throttle = Math.max(0, plane.throttle - 0.5 * dt);
    }

    private void render(GraphicsContext g) {
        g.setFill(Color.web("#4986c4"));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.save();
        g.translate(-cameraX, 0);

        g.setFill(Color.web("#266308"));
        g.fillRect(cameraX, HEIGHT - 80, WIDTH, 80);

        int startMarker = (int)(cameraX / 500) * 500;
        for (int i = startMarker; i < startMarker + WIDTH + 500; i += 500) {
            boolean onStart = (i >= 0 && i < RUNWAY_LENGTH);
            boolean onEnd = (i >= DESTINATION_X && i < DESTINATION_X + RUNWAY_LENGTH);
            if (!onStart && !onEnd) {
                g.setFill(Color.LIGHTGREEN);
                g.fillRect(i, HEIGHT - 80, 20, 80);
            }
        }

        drawRunway(g, 0);
        drawRunway(g, DESTINATION_X);

        g.setFill(Color.RED);
        g.fillRect(DESTINATION_X, HEIGHT - 150, 10, 70);
        g.fillPolygon(new double[]{DESTINATION_X, DESTINATION_X+50, DESTINATION_X},
                new double[]{HEIGHT-150, HEIGHT-135, HEIGHT-120}, 3);

        drawPlane(g);
        g.restore();

        drawHUD(g);
        drawMissionInfo(g);

        if (gameWon) {
            g.setFill(Color.rgb(0, 0, 0, 0.7));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setFill(Color.LIME);
            g.setFont(Font.font("Arial", 48));
            g.fillText("MISJA UKOŃCZONA!", 280, 300);
            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", 24));
            g.fillText("Punkty zapisane. ENTER = Restart", 320, 350);
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

    private void drawPlane(GraphicsContext g) {
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

    private void drawHUD(GraphicsContext g) {
        g.setFill(Color.rgb(0, 0, 0, 0.6));
        g.fillRoundRect(10, 10, 260, 140, 8, 8);

        g.setFill(Color.WHITE);
        g.setFont(Font.font(14));

        double dist = Math.max(0, DESTINATION_X - plane.x);

        g.fillText(String.format("Goal Dist: %.0f m", dist), 20, 34);
        g.fillText(String.format("Speed    : %.1f m/s", plane.getSpeed()), 20, 56);
        g.fillText(String.format("Heading  : %.0f°", plane.angle), 20, 78);
        g.fillText(String.format("Throttle : %.0f%%", plane.throttle * 100), 20, 100);

        if (plane.flapsExtended) {
            g.setFill(Color.ORANGE);
            g.fillText("FLAPS: EXTENDED", 20, 122);
        } else {
            g.setFill(Color.LIGHTGRAY);
            g.fillText("FLAPS: RETRACTED", 20, 122);
        }

        g.setFill(Color.WHITE);
        g.fillText("Fuel", 150, 56);
        g.setStroke(Color.GRAY);
        g.strokeRect(150, 62, 100, 14);
        g.setFill(Color.LIMEGREEN);
        g.fillRect(150, 62, plane.fuel, 14);
    }

    private void drawMissionInfo(GraphicsContext g) {
        if (gameWon) return;

        g.setFill(Color.rgb(255,255,255,0.9));
        g.setFont(Font.font(16));
        g.fillText(missionActive ? "Mission: LAND AT DESTINATION" : "Mission: INACTIVE (Press ENTER)", 300, 30);

        g.setFont(Font.font(12));
        g.fillText("Controls: W/S pitch, A/D throttle, F flaps", 300, 50);

        if (plane.fuel < 20) {
            g.setFill(Color.RED);
            g.fillText("WARNING!: Low fuel!", 300, 70);
        }
    }

    private static double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    private static class Plane {
        double x, y;
        double vx = 0, vy = 0;
        double angle = 0;
        double pitch = 0;
        double throttle = 0.2;
        double fuel = 100;
        boolean onGround = false;

        boolean flapsExtended = false;

        Plane(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void turn(double dAngle) {
            angle = (angle + dAngle) % 360;
        }

        void toggleFlaps() {
            flapsExtended = !flapsExtended;
        }

        double getSpeed() {
            return Math.hypot(vx, vy);
        }

        void updatePhysics(double dt, double windX, double windY) {
            double rad = Math.toRadians(angle);
            double thrust = 200 * throttle;
            double ax = Math.cos(rad) * thrust;
            double ay = Math.sin(rad) * thrust;

            double forwardSpeed = getSpeed();

            //FLAP PHYSICS
            //The basis of lift force:
            double lift = 50 * (forwardSpeed / 50.0) * (1 + pitch * 0.5);

            //If the flaps are extended:
            if (flapsExtended) {
                lift *= 1.5; //Increasing lift by 50%
            }

            double gravity = 9.81 * 20;

            //AIR RESISTANCE:
            double dragFactor = 0.5;
            if (flapsExtended) {
                dragFactor = 0.9; //Flaps significantly increase drag
            }

            double dragX = -vx * dragFactor;
            double dragY = -vy * dragFactor;

            vx += (ax + dragX + windX * 0.2) * dt;
            vy += ((-lift) + ay + dragY + gravity + windY * 0.2) * dt;

            x += vx * dt;
            y += vy * dt;

            if (x < 0) { x = 0; vx = 0; }
            pitch *= 0.96;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
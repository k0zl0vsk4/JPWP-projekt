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
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class FlightLab_Main extends Application {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;

    private MissionLogic missionLogic;
    private Plane plane;

    private javafx.scene.image.Image planeImage;
    private boolean up, down, left, right, quickClimb;

    private double windX = 0.0;
    private double windY = 0.0;

    private long startTime;
    private boolean missionActive = false;
    private boolean gameWon = false;
    private boolean gameLost = false;
    private String endMessage = ""; //Final message

    private double cameraX = 0;

    public FlightLab_Main() {
        this.missionLogic = new MissionLogic(MissionType.NORMAL);
    }

    public FlightLab_Main(MissionType missionType) {
        this.missionLogic = new MissionLogic(missionType);
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        try {
            planeImage = new javafx.scene.image.Image(getClass().getClassLoader().getResourceAsStream("images/plane_model.png"), 80, 40, true, true);
        } catch (Exception e) {
            System.out.println("Info: Brak pliku plane_model.png.");
        }

        plane = new Plane(0, 0);
        missionLogic.setupPlane(plane);

        if (missionLogic.getType() == MissionType.EMERGENCY) {
            missionActive = true;
            plane.throttle = 0.4;
        }

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(this::onKeyPressed);
        scene.setOnKeyReleased(this::onKeyReleased);

        primaryStage.setTitle("Flight Lab - " + missionLogic.getObjectiveText());
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

    private void update(double dt) {
        if (gameWon || gameLost) return;

        //Controls
        if (up) plane.turn(-60 * dt);
        if (down) plane.turn(60 * dt);
        if (left) plane.throttle = clamp(plane.throttle - 0.5 * dt, 0, 1);

        //For emergency mission throttle is blocked (limit 40%)
        if (right)
        {
            double maxThrottle = (missionLogic.getType() == MissionType.EMERGENCY) ? 0.4 : 1.0;
            plane.throttle = clamp(plane.throttle + 0.5 * dt, 0, maxThrottle);
        }
        if (quickClimb) plane.pitch += 20 * dt;

        //Physics
        plane.updatePhysics(dt, windX, windY, missionLogic.getType());

        //Collision with the ground
        if (plane.y > HEIGHT - 50) {
            plane.y = HEIGHT - 50;
            plane.vy = 0;
            plane.onGround = true;
            if (plane.throttle < 0.1) plane.vx *= 0.95; //Braking
        } else {
            plane.onGround = false;
        }

        if (plane.y < 20) {
            plane.y = 20;
            if (plane.vy < 0) plane.vy = 0;
        }

        if (plane.onGround && plane.getSpeed() < 5.0) {
            if (missionLogic.getType() == MissionType.PRECISION) {
                if (plane.x < 5200 || plane.x > 5300) {
                    handleGameOver("PUDŁO! Lądowanie poza strefą.");
                    return;
                }
            }

            int points = missionLogic.checkVictory(plane);
            if (points > 0) {
                handleVictory(points);
            }
            else if (plane.x > missionLogic.getDestX() + missionLogic.getRunwayLen()) {
                handleGameOver("KRAKSA! Wyjazd poza pas startowy.");
            }
        }

        if (missionLogic.getType() == MissionType.LOW_PASS && plane.onGround && plane.x > missionLogic.getDestX()) {
            handleGameOver("BŁĄD! Miałeś nie lądować!");
        }

        cameraX = Math.max(0, plane.x - 200);

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (missionActive && elapsed % 10 == 0 && elapsed != 0) {
            windX = (Math.sin(elapsed) * 40);
        } else {
            windX *= 0.95;
        }

        if (missionLogic.getType() != MissionType.EMERGENCY) {
            plane.fuel = Math.max(0, plane.fuel - 0.05 * plane.throttle * dt);
        }

        if (plane.fuel <= 0)
            plane.throttle = Math.max(0, plane.throttle - 0.5 * dt);
    }

    private void handleVictory(int points) {
        if (!gameWon && !gameLost) {
            gameWon = true;
            missionActive = false;

            if (plane.flapsExtended && missionLogic.getType() != MissionType.LOW_PASS) {
                points += 20;
            }

            Player current = GameData.getInstance().getCurrentPlayer();
            if (current != null) {
                current.addMissionPoints(points);
                GameData.getInstance().saveData();
                System.out.println("Zadanie wykonane! +" + points + " pkt");
            }
        }
    }

    private void handleGameOver(String reason) {
        if (!gameWon && !gameLost) {
            gameLost = true;
            missionActive = false;
            endMessage = reason;
        }
    }

    private void onKeyPressed(KeyEvent e) {
        KeyCode c = e.getCode();
        switch (c) {
            case W -> up = true;
            case S -> down = true;
            case A -> left = true;
            case D -> right = true;
            case SPACE -> quickClimb = true;
            case F -> plane.toggleFlaps();
            case ENTER -> {
                if (gameWon || gameLost) {
                    try { new MainMenu().start((Stage)((Scene)e.getSource()).getWindow()); }
                    catch (Exception ex) { ex.printStackTrace(); }
                } else {
                    missionActive = !missionActive;
                    if (missionActive) startTime = System.currentTimeMillis();
                }
            }
            case ESCAPE -> {
                try { new MainMenu().start((Stage)((Scene)e.getSource()).getWindow()); }
                catch (Exception ex) { ex.printStackTrace(); }
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

    private void render(GraphicsContext g) {
        g.setFill(Color.web("#4986c4"));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.save();
        g.translate(-cameraX, 0);

        g.setFill(Color.web("#266308"));
        g.fillRect(cameraX, HEIGHT - 80, WIDTH, 80);

        //Grass
        int startMarker = (int)(cameraX / 500) * 500;
        for (int i = startMarker; i < startMarker + WIDTH + 500; i += 500) {
            boolean onStart = (i >= 0 && i < missionLogic.getRunwayLen());
            boolean onEnd = (i >= missionLogic.getDestX() && i < missionLogic.getDestX() + missionLogic.getRunwayLen());
            if (!onStart && !onEnd) {
                g.setFill(Color.LIGHTGREEN);
                g.fillRect(i, HEIGHT - 80, 20, 80);
            }
        }

        drawRunway(g, 0);
        drawRunway(g, missionLogic.getDestX());

        //Yellow zone for precision mission
        if (missionLogic.getType() == MissionType.PRECISION) {
            g.setFill(Color.YELLOW);

            g.setGlobalAlpha(0.5);
            g.fillRect(5200, HEIGHT - 80, 100, 80);
            g.setGlobalAlpha(1.0);
        }

        g.setFill(Color.RED);
        g.fillRect(missionLogic.getDestX(), HEIGHT - 150, 10, 70);
        g.fillPolygon(new double[]{missionLogic.getDestX(), missionLogic.getDestX()+50, missionLogic.getDestX()},
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
            g.fillText("MISSION COMPLETE!", 280, 300);
            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", 24));
            g.fillText("Punkty zapisane. ENTER = Powrót", 320, 350);
        }

        if (gameLost) {
            g.setFill(Color.rgb(50, 0, 0, 0.8));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setFill(Color.RED);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            g.fillText("MISJA NIEUDANA!", WIDTH/2 - 200, 250);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", 28));
            g.fillText(endMessage, WIDTH/2 - 200, 320);

            g.setFont(Font.font("Arial", 20));
            g.fillText("Naciśnij ENTER, aby spróbować ponownie.", WIDTH/2 - 200, 400);
        }
    }

    private void drawRunway(GraphicsContext g, double startX) {
        g.setFill(Color.DARKGRAY);
        g.fillRect(startX, HEIGHT - 80, missionLogic.getRunwayLen(), 80);
        g.setFill(Color.WHITE);
        for (double j = startX; j < startX + missionLogic.getRunwayLen(); j += 100) {
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

        double dist = Math.max(0, missionLogic.getDestX() - plane.x);

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

        if (plane.fuel < 20) g.setFill(Color.RED);
        else g.setFill(Color.LIMEGREEN);

        g.fillRect(150, 62, plane.fuel, 14);
    }

    private void drawMissionInfo(GraphicsContext g) {
        if (gameWon || gameLost) return;

        g.setFill(Color.rgb(255,255,255,0.9));
        g.setFont(Font.font(16));
        g.fillText("Misja: " + missionLogic.getObjectiveText(), 300, 30);

        g.setFont(Font.font(12));
        g.fillText("Sterowanie: W/S (kąt), A/D (gaz), F (klapy)", 300, 50);

        if (plane.fuel < 20) {
            g.setFill(Color.RED);
            g.fillText("WARNING!: Low fuel!", 300, 70);
        }
    }

    private static double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    public static class Plane {
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

        double getSpeed() { return Math.hypot(vx, vy); }

        void updatePhysics(double dt, double windX, double windY, MissionType mission) {
            double rad = Math.toRadians(angle);
            double maxThrust = (mission == MissionType.EMERGENCY) ? 120 : 200;

            double thrust = maxThrust * throttle;
            double ax = Math.cos(rad) * thrust;
            double ay = Math.sin(rad) * thrust;

            double forwardSpeed = getSpeed();
            double lift = 50 * (forwardSpeed / 50.0) * (1 + pitch * 0.5);
            if (flapsExtended) lift *= 1.5;

            double gravity = 9.81 * 20;
            double dragFactor = flapsExtended ? 0.9 : 0.5;

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
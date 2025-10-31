package com.example.flightlab;

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

    private Plane plane;
    private boolean up, down, left, right, quickClimb;

    private double windX = 0.0;
    private double windY = 0.0;

    private long startTime;
    private boolean missionActive = false;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        plane = new Plane(WIDTH * 0.1, HEIGHT * 0.6);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(this::onKeyPressed);
        scene.setOnKeyReleased(this::onKeyReleased);

        primaryStage.setTitle("Flight Lab - prototype");
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
                missionActive = !missionActive;
                if (missionActive) startTime = System.currentTimeMillis();
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

    private void update(double dt) {
        if (up) plane.throttle = clamp(plane.throttle + 0.8 * dt, 0, 1);
        if (down) plane.throttle = clamp(plane.throttle - 1.2 * dt, 0, 1);

        if (left) plane.turn(-60 * dt);
        if (right) plane.turn(60 * dt);

        if (quickClimb) plane.pitch += 20 * dt;

        plane.updatePhysics(dt, windX, windY);

        if (plane.y > HEIGHT - 20) {
            plane.y = HEIGHT - 20;
            plane.vy = 0;
            plane.onGround = true;
        } else {
            plane.onGround = false;
        }

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

        g.setFill(Color.web("#266308")); // horizon
        g.fillRect(0, HEIGHT - 80, WIDTH, 80);

        g.setFill(Color.DARKGRAY);
        g.fillRect(150, HEIGHT -80, WIDTH, 80); //runway

        drawPlane(g);
        drawHUD(g);
        drawMissionInfo(g);
    }

    private void drawPlane(GraphicsContext g) {
        g.save();
        g.translate(plane.x, plane.y);
        g.rotate(plane.angle);

        g.setFill(Color.DARKSLATEGRAY);
        g.fillRect(-30, -8, 60, 16); //body

        g.setFill(Color.LIGHTGRAY);
        g.fillPolygon(new double[]{-10, 40, 40, -10}, new double[]{-8, -22, 22, 8}, 4); //wings

        g.fillRect(-35, -6, 6, 12); //tail
        g.restore();
    }

    private void drawHUD(GraphicsContext g) {
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

    private void drawMissionInfo(GraphicsContext g) {
        g.setFill(Color.rgb(255,255,255,0.9));
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

    private static double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    private static class Plane {
        double x, y;
        double vx = 0, vy = 0;
        double angle = 0; // degrees (0 = rightwards)
        double pitch = 0; // small pitch variable for lift influence
        double throttle = 0.2; // 0..1
        double fuel = 100; // display width for simple bar (0..100)
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

            vx += (ax +dragX + windX * 0.2) * dt;
            vy += ((-lift) + ay +dragY + gravity + windY * 0.2) * dt;

            x += vx * dt;
            y += vy * dt;

            if (x < 0) {x = 0; vx = 0; }
            if (x > WIDTH) {x =WIDTH; vx = 0; }

            pitch *= 0.96;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
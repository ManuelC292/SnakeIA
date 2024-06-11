package fr.manu.snakejava;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGame extends Application {

    private static final int BLOCK_SIZE = 30;
    private static final int APP_WIDTH = 20 * BLOCK_SIZE;
    private static final int APP_HEIGHT = 20 * BLOCK_SIZE;

    private List<int[]> snake;
    private int[] food;
    private String direction = "RIGHT";
    private boolean running = false;
    private boolean gameOver = false;
    private Timeline timeline;
    private double speed = 200; // Initial speed in milliseconds
    private int score = 0; // Score variable

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Snake Game");

        Canvas canvas = new Canvas(APP_WIDTH, APP_HEIGHT + 40); // Increased height for score display
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new javafx.scene.Group(canvas), APP_WIDTH, APP_HEIGHT + 40);
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
            if (key.getCode() == KeyCode.UP && !direction.equals("DOWN")) direction = "UP";
            if (key.getCode() == KeyCode.DOWN && !direction.equals("UP")) direction = "DOWN";
            if (key.getCode() == KeyCode.LEFT && !direction.equals("RIGHT")) direction = "LEFT";
            if (key.getCode() == KeyCode.RIGHT && !direction.equals("LEFT")) direction = "RIGHT";
            if (key.getCode() == KeyCode.ENTER && gameOver) {
                restartGame(gc);
            }
        });

        startGame();

        timeline = new Timeline(new KeyFrame(Duration.millis(speed), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void startGame() {
        snake = new ArrayList<>();
        snake.add(new int[]{10, 10});
        snake.add(new int[]{10, 9});
        snake.add(new int[]{10, 8});
        spawnFood();
        direction = "RIGHT";
        running = true;
        gameOver = false;
        speed = 200; // Reset speed
        score = 0; // Reset score
    }

    private void restartGame(GraphicsContext gc) {
        startGame();
        timeline = new Timeline(new KeyFrame(Duration.millis(speed), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        run(gc);
    }

    private void run(GraphicsContext gc) {
        if (!running) {
            restartGame(gc);
            gc.setFill(Color.RED);
            gc.setFont(new Font(20));
            gc.fillText("Game Over - Press Enter to Restart", APP_WIDTH / 4, APP_HEIGHT / 2);
            timeline.stop();
            gameOver = true;
            return;
        }

        moveSnake();

        if (isColliding()) {
            running = false;
        }

        if (isEatingFood()) {
            snake.add(new int[]{-1, -1});
            spawnFood();
            speed = speed * 0.9; // Increase speed by 10%
            timeline.stop();
            timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(speed), e -> run(gc)));
            timeline.play();
            score++; // Increment score
        }

        draw(gc);
    }

    private void moveSnake() {
        for (int i = snake.size() - 1; i > 0; i--) {
            snake.set(i, snake.get(i - 1).clone());
        }

        switch (direction) {
            case "UP" -> snake.get(0)[1]--;
            case "DOWN" -> snake.get(0)[1]++;
            case "LEFT" -> snake.get(0)[0]--;
            case "RIGHT" -> snake.get(0)[0]++;
        }
    }

    private boolean isColliding() {
        int[] head = snake.get(0);
        for (int i = 1; i < snake.size(); i++) {
            if (head[0] == snake.get(i)[0] && head[1] == snake.get(i)[1]) {
                return true;
            }
        }
        return head[0] < 0 || head[1] < 0 || head[0] >= APP_WIDTH / BLOCK_SIZE || head[1] >= APP_HEIGHT / BLOCK_SIZE;
    }

    private boolean isEatingFood() {
        return snake.get(0)[0] == food[0] && snake.get(0)[1] == food[1];
    }

    private void spawnFood() {
        Random random = new Random();
        food = new int[]{random.nextInt(APP_WIDTH / BLOCK_SIZE), random.nextInt(APP_HEIGHT / BLOCK_SIZE)};
    }

    private void draw(GraphicsContext gc) {
        // Alternate background colors
        Color color1 = Color.web("#2ECC71");
        Color color2 = Color.web("#28B463");
        for (int y = 0; y < APP_HEIGHT / BLOCK_SIZE; y++) {
            for (int x = 0; x < APP_WIDTH / BLOCK_SIZE; x++) {
                if ((x + y) % 2 == 0) {
                    gc.setFill(color1);
                } else {
                    gc.setFill(color2);
                }
                gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }

        // Draw snake body
        gc.setFill(Color.BLUE);
        for (int i = 1; i < snake.size() - 1; i++) {
            int[] segment = snake.get(i);
            gc.fillOval(segment[0] * BLOCK_SIZE, segment[1] * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
        }

        // Draw snake head
        gc.setFill(Color.DARKBLUE);
        int[] head = snake.get(0);
        double startAngle;
        switch (direction) {
            case "UP" -> startAngle = 135;
            case "DOWN" -> startAngle = 315;
            case "LEFT" -> startAngle = 225;
            case "RIGHT" -> startAngle = 45;
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
        gc.fillArc(head[0] * BLOCK_SIZE, head[1] * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1, startAngle, 270, ArcType.ROUND);

        // Draw snake tail
        gc.setFill(Color.LIGHTBLUE);
        double finishAngle;
        switch(direction){
            case "UP" -> finishAngle = 315;
            case "DOWN" -> finishAngle = 135;
            case "LEFT" -> finishAngle = 45;
            case "RIGHT" -> finishAngle = 225;
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
        int[] tail = snake.get(snake.size() - 1);
        gc.fillArc(tail[0] * BLOCK_SIZE, tail[1] * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1, finishAngle, 270, ArcType.ROUND);

        // Draw food
        //gc.setFill(Color.RED);
        //gc.fillOval(food[0] * BLOCK_SIZE, food[1] * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);

        // Draw apple
        gc.setFill(Color.RED);
        gc.fillOval(food[0] * BLOCK_SIZE, food[1] * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        // Draw apple stem (leaf)
        gc.setFill(Color.GREEN);
        double[] leafX = {food[0] * BLOCK_SIZE + BLOCK_SIZE / 2, food[0] * BLOCK_SIZE + BLOCK_SIZE * 0.6, food[0] * BLOCK_SIZE + BLOCK_SIZE / 2};
        double[] leafY = {food[1] * BLOCK_SIZE + BLOCK_SIZE * 0.2, food[1] * BLOCK_SIZE - BLOCK_SIZE * 0.4, food[1] * BLOCK_SIZE - BLOCK_SIZE * 0.8};
        gc.fillPolygon(leafX, leafY, 3);

        // Clear old speed text
        gc.setFill(Color.WHITE);
        gc.fillRect(0, APP_HEIGHT, 200, 40); // Clear the area where the speed text is displayed

        // Draw speed
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(20));
        gc.fillText("Speed: " + (int)(1000 / speed), 10, APP_HEIGHT + 30); // Convert speed to frames per second

        // Draw score in bottom right
        gc.setFill(Color.BLACK);
        gc.fillRect(APP_WIDTH - 120, APP_HEIGHT + 10, 100, 30); // Black rectangle background
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));
        gc.fillText("Score: " + score, APP_WIDTH - 110, APP_HEIGHT + 30); // Score text

    }
}



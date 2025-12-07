package org.example.javafx.ThirdTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Application {

    private Socket socket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;

    private Label amountLabel;
    private Label statusLabel;
    private HBox buttonPane;
    private boolean currTurn = false;

    private int currAmount = 37;

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("Third Task");

        amountLabel = new Label("Спичек на столе: 37");
        amountLabel.setFont(new Font("Times New Roman", 20));

        statusLabel = new Label("Ожидание игроков");
        statusLabel.setFont(new Font("Times New Roman", 12));

        buttonPane = new HBox(10);
        buttonPane.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 5; i++) {
            int count = i;
            Button button = new Button(String.valueOf(count));
            button.setPrefSize(40, 40);
            button.setOnAction(e -> sendMove(count));
            buttonPane.getChildren().add(button);
        }
        buttonPane.setDisable(true);

        VBox pane = new VBox(20, amountLabel, buttonPane, statusLabel);
        pane.setAlignment(Pos.CENTER);

        Scene scene = new Scene(pane, 400, 300);
        stage.setScene(scene);
        stage.show();

        new Thread(this::connectToServer).start();
    }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 11888);
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message;
            while ((message = inputStream.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("NEW AMOUNT ")) {
                String count = message.split(" ")[2];
                currAmount = Integer.parseInt(count);
                amountLabel.setText("Спичек на столе: " + count);
            } else if (message.equals("ENABLE")) {
                currTurn = true;
                statusLabel.setText("Выберите количество спичек");
                buttonsState();
            } else if (message.equals("DISABLE")) {
                currTurn = false;
                buttonPane.setDisable(true);
                statusLabel.setText("Ход противника");
            } else if (message.equals("WIN")) {
                buttonPane.setDisable(true);
                statusLabel.setText("Вы победили");
                alert("Победа", "Вы взяли последнюю спичку и победили.");
            } else if (message.equals("LOSE")) {
                buttonPane.setDisable(true);
                statusLabel.setText("Вы проиграли");
                alert("Поражение", "Соперник взял последнюю спичку. Вы проиграли.");
            }
        });
    }

    // Не позволяем взять больше спичек, чем есть на столе
    private void buttonsState() {
        buttonPane.setDisable(false);

        for (Node node : buttonPane.getChildren()) {
            if (node instanceof Button button) {

                int buttonValue = Integer.parseInt(button.getText());
                button.setDisable(buttonValue > currAmount);

            }
        }
    }

    private void alert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void sendMove(int count) {
        if (currTurn) {
            outputStream.println("MOVE " + count);
            buttonPane.setDisable(true);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
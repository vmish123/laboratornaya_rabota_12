package org.example.javafx.SecondTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Application {

    private ProgressBar progressBar;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonPause;
    private boolean isPaused = false;

    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private Socket socket;

    @Override
    public void start(Stage stage) throws Exception {

        //Присоединение к серверу
        connectToServer();

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);

        buttonStart = new Button("Старт");
        buttonStop = new Button("Стоп");
        buttonPause = new Button("Пауза");

        buttonPause.setDisable(true);
        buttonStop.setDisable(true);

        buttonStart.setOnAction(event -> startProcess());
        buttonStop.setOnAction(event -> stopProcess());
        buttonPause.setOnAction(event -> pauseProcess());

        HBox pane = new HBox(10, buttonStart, buttonPause, buttonStop);
        pane.setAlignment(Pos.CENTER);

        VBox mainPane = new VBox(20, progressBar, pane);
        mainPane.setPadding(new Insets(20));

        Scene scene = new Scene(mainPane, 400, 150);
        stage.setScene(scene);
        stage.setTitle("Second Task");
        stage.show();
    }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 11888);
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Запуск потока слушателя сообщений от сервера
            Thread listenerThread = new Thread(this::listenToServer);
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод, слушающий обновления от сервера
    private void listenToServer() {
        try {
            String message;
            while ((message = inputStream.readLine()) != null) {
                double progress = Double.parseDouble(message);
                Platform.runLater(() -> progressBar.setProgress(progress));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startProcess() {
        if (outputStream != null) {

            progressBar.setProgress(0);

            buttonPause.setDisable(false);
            buttonStop.setDisable(false);
            buttonPause.setText("Пауза");
            isPaused = false;

            outputStream.println("START");
        }
    }

    private void pauseProcess() {
        if (outputStream != null) {
            if (!isPaused) {
                outputStream.println("PAUSE");
                buttonPause.setText("Продолжить");
                isPaused = true;
            } else {
                outputStream.println("RESUME");
                buttonPause.setText("Пауза");
                isPaused = false;
            }
        }
    }

    private void stopProcess() {
        if (outputStream != null) {

            progressBar.setProgress(0);
            outputStream.println("STOP");

            buttonPause.setText("Пауза");
            buttonPause.setDisable(true);
            buttonStop.setDisable(true);
            isPaused = false;
        }
    }

    @Override
    public void stop() throws Exception {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        super.stop();
    }
}
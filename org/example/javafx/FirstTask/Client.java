package org.example.javafx.FirstTask;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// Калькулятор. Клиентский класс.

public class Client extends Application {

    private double currentValue = 0.0;
    private String currentOp = "";
    private boolean startNewNumber = true;
    
    @Override
    public void start(Stage stage) throws Exception {
        TextField display = new TextField();
        display.setEditable(false);
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPrefHeight(50);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        String[] buttons = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
        };

        int row = 0;
        int col = 0;
        for (String button : buttons) {
            Button btn = new Button(button);
            btn.setPrefSize(60, 40);

            btn.setOnAction(e -> {
                if ("0123456789.".contains(button)) {
                    if (startNewNumber) {
                        display.setText(button.equals(".") ? "0." : button);
                        startNewNumber = false;
                    }
                    else {
                        if (button.equals(".") && display.getText().contains(".")) { return; }
                        display.setText(display.getText() + button);

                    }
                }
                else if ("+-*/".contains(button)) {
                    try {
                        compute(display);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    currentOp = button;
                    startNewNumber = true;
                }
                else if (button.equals("=")) {
                    try {
                        compute(display);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    currentOp = "";
                    startNewNumber = true;
                }
            });

            grid.add(btn, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }

        VBox pane = new VBox(10, display, grid);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setTitle("First Task");
        stage.show();
    }

    private void compute(TextField display) throws IOException {

        if (currentOp.isEmpty()) {
            try {
                currentValue = Double.parseDouble(display.getText());
            } catch (Exception e) {
                currentValue = 0.0;
            }
            return;
        }

        double secondValue = Double.parseDouble(display.getText());

        try (Socket socket = new Socket("127.0.0.1", 11888)) {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            outputStream.writeDouble(currentValue);
            outputStream.writeDouble(secondValue);
            outputStream.writeUTF(currentOp);
            outputStream.flush();

            String response = inputStream.readUTF();
            if (response.equals("Success")) {
                double result = inputStream.readDouble();
                currentValue = result;
                display.setText(String.valueOf(result));
            }
            else if (response.equals("Zero Division")) {
                display.setText("Деление на ноль невозможно");
                currentValue = 0.0;
                currentOp = "";
                startNewNumber = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package org.example.javafx.FirstTask;


// Калькулятор. Серверный класс.

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(11888)) {
            while (true) {

                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                // Работа в многопоточном режиме для постоянной готовности обработки запросов нескольких клиентов
                Thread thread = new Thread(() -> {
                    try {
                        calcLogic(socket);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    } finally {
                        try {
                            if (!socket.isClosed()) {
                                socket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void calcLogic(Socket socket) throws IOException {

        // Считывание входящего и исходящего потоков
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            // Считывание клиентского ввода
            double num1 = inputStream.readDouble();
            double num2 = inputStream.readDouble();
            String operation = inputStream.readUTF();

            System.out.println("Operation: " + num1 + " " + operation + " " + num2);

            // Вычисление значения
            double result = 0;
            boolean zeroDivision = false;

            switch (operation) {
                case "+": {
                    result = num1 + num2;
                    break;
                }
                case "-": {
                    result = num1 - num2;
                    break;
                }
                case "*": {
                    result = num1 * num2;
                    break;
                }
                case "/": {
                    if (num2 != 0)
                        result = num1 / num2;
                    else
                        zeroDivision = true;
                    break;
                }
            }

            // Отправка ответа
            if (zeroDivision) {
                outputStream.writeUTF("Zero Division");
                System.out.println("Zero Division error sent to " + socket.getInetAddress());
            } else {
                outputStream.writeUTF("Success");
                outputStream.writeDouble(result);
                System.out.println("Result: " + result + " " + " sent to " + socket.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
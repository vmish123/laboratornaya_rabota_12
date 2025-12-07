package org.example.javafx.SecondTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// Processbar. Серверный класс.

public class Server {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(11888)) {
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Обработчик клиента
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private ProgressThread current;
        private Thread progressThread;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                String message;

                while ((message = in.readLine()) != null) {
                    System.out.println("Command given: " + message);
                    switch (message) {
                        case "START": {
                            stopProgress();
                            current = new ProgressThread(out);
                            progressThread = new Thread(current);
                            progressThread.start();
                            break;
                        }
                        case "STOP": {
                            stopProgress();
                            break;
                        }
                        case "PAUSE": {
                            if (current != null) current.pause();
                            break;
                        }
                        case "RESUME": {
                            if (current != null) current.resume();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stopProgress();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void stopProgress() {
            if (current != null) {
                current.stop();
            }
            current = null;
        }
    }

    // Реализация работы progressbar
    private static class ProgressThread implements Runnable {
        private final PrintWriter out;
        private volatile boolean running = true;
        private volatile boolean paused = false;
        private final Object monitor = new Object();

        public ProgressThread(PrintWriter out) {
            this.out = out;
        }

        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                if (!running) break;

                synchronized (monitor) {
                    while (paused) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                if (!running) break;

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                double progress = i / 1000.0;

                // Отправка прогресса клиенту
                out.println(progress);
            }
            if (running) {
                out.println(1.0);
            }
        }

        public void stop() {
            running = false;
            resume();
        }

        public void pause() {
            paused = true;
        }

        public void resume() {
            synchronized (monitor) {
                paused = false;
                monitor.notify();
            }
        }
    }

}

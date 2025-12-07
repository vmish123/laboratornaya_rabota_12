package org.example.javafx.ThirdTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int amount = 37;
    private int amountLeft = amount;
    private int currentPlayer = 0;

    // Список игроков
    private List<ClientHandler> clients = new ArrayList<ClientHandler>();

    // Обработчик отдельного игрока
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter outputStream;
        private BufferedReader inputStream;
        private Server server;
        private int id;

        public ClientHandler(Socket socket, Server server, int id) {
            this.socket = socket;
            this.server = server;
            this.id = id;
            try {
                outputStream = new PrintWriter(socket.getOutputStream(), true);
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = inputStream.readLine()) != null) {
                    if (inputLine.startsWith("MOVE")) {
                        int x = Integer.parseInt(inputLine.split(" ")[1]);
                        server.makeMove(x, this);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void sendMessage(String message) {
            outputStream.println(message);
        }
    }

    public void Start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(11888)) {
            // Проверка на количество подключенных игроков
            int id = 0;
            while (clients.size() < 2) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this, id);
                id++;

                clients.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();

                System.out.println("Client " + socket.getInetAddress() + " connected");
            }
            
            System.out.println("Both players are connected. Starting the game...");
            
            sendAll("NEW AMOUNT " + amountLeft);
            changeTurn();
        }
    }

    // Метод обработки хода
    public synchronized void makeMove(int takenAmount, ClientHandler client) {

        if (client != clients.get(currentPlayer)) return;

        amountLeft -= takenAmount;
        sendAll("NEW AMOUNT " + amountLeft);

        // Проверка условия победы
        if (amountLeft <= 0) {
            client.sendMessage("WIN");
            clients.get((currentPlayer + 1) % 2).sendMessage("LOSE");
            return;
        }

        currentPlayer = (currentPlayer + 1) % 2;
        changeTurn();
    }

    private void changeTurn() {
        clients.get(currentPlayer).sendMessage("ENABLE");
        clients.get((currentPlayer + 1) % 2).sendMessage("DISABLE");
    }

    private void sendAll(String message) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().Start();
    }
}
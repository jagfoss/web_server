package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int SERVER_PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int THREAD_POOL_TERMINATION_TIMEOUT_SECONDS = 10;
    private static final boolean HTTP_KEEP_ALIVE = true;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(Level.INFO, "Shutdown detected, triggered thread pool closure ...");
            threadPool.shutdown();
            try {
                logger.log(Level.INFO, "Waiting for closing thread pool...");
                if (!threadPool.awaitTermination(THREAD_POOL_TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    logger.log(Level.INFO, "Forcefully closing thread pool...");
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            logger.log(Level.INFO, "Thread pool closed.");
            logger.log(Level.INFO, "Server stopped.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            logger.log(Level.INFO, "Server started. Visit: http://localhost:%s".formatted(SERVER_PORT));
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (HTTP_KEEP_ALIVE) {
                    threadPool.submit(new ClientHandler(clientSocket, new KeepAliveHttpClientHandlerImpl()));
                } else {
                    threadPool.submit(new ClientHandler(clientSocket, new HttpClientHandlerImpl()));
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while accepting socket", e);
        }
    }
}

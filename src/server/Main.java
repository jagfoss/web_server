package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the multithreaded HTTP server.
 * <p>
 * This class initializes a {@link ServerSocket} bound to a configurable port
 * and uses a fixed-size {@link ExecutorService} thread pool to handle incoming
 * client connections. Each connection is delegated to a {@link ClientHandler},
 * which processes the request using either a basic {@link HttpClientHandlerImpl}
 * or a {@link KeepAliveHttpClientHandlerImpl}, depending on the server
 * configuration (keep-alive enabled or disabled).
 * </p>
 *
 * <p>
 * A JVM shutdown hook ensures that the thread pool is properly shut down when
 * the server is stopped. If threads do not terminate gracefully within a
 * configured timeout, the thread pool is forcefully shut down.
 * </p>
 *
 * <p>
 * Features:
 * <ul>
 *   <li>Configurable server port</li>
 *   <li>Configurable fixed-size thread pool</li>
 *   <li>Graceful and forced shutdown of the thread pool</li>
 *   <li>HTTP/1.1 keep-alive support (toggle via {@code HTTP_KEEP_ALIVE})</li>
 *   <li>Error handling and logging for client connections</li>
 * </ul>
 * </p>
 */

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int SERVER_PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int THREAD_POOL_TERMINATION_TIMEOUT_SECONDS = 10;
    private static final boolean HTTP_KEEP_ALIVE = false;

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
                try {
                    if (HTTP_KEEP_ALIVE) {
                        threadPool.submit(new ClientHandler(clientSocket, new KeepAliveHttpClientHandlerImpl()));
                    } else {
                        threadPool.submit(new ClientHandler(clientSocket, new HttpClientHandlerImpl()));
                    }
                } catch (RejectedExecutionException rejectedExecutionException) {
                    logger.log(Level.SEVERE, "Failed to submit client handler to thread pool", rejectedExecutionException);
                    // In case socket is not closed, we try to close it here.
                    try {
                        clientSocket.close();
                    } catch (IOException ioException) {
                        logger.log(Level.WARNING, "Error closing client socket after rejection", ioException);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while accepting socket", e);
        }
    }
}

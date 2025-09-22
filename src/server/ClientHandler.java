package server;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A worker responsible for handling a single client connection.
 * <p>
 * Each {@code ClientHandler} is executed in its own thread (via {@link Runnable})
 * and delegates the actual HTTP request/response processing to a provided
 * {@link HttpClientHandler} implementation.
 * <p>
 * Typical responsibilities include:
 * <ul>
 *   <li>Wrapping a {@link Socket} connection for a client</li>
 *   <li>Invoking {@link HttpClientHandler#handleClient(Socket)} to process the request</li>
 * </ul>
 * <p>
 * This design separates connection/thread management from the HTTP
 * protocol logic, allowing different {@code HttpClientHandler}
 * implementations (e.g., basic or keep-alive) to be plugged in.
 */


public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final HttpClientHandler httpClientHandler;

    public ClientHandler(final Socket socket, final HttpClientHandler httpClientHandler) {
        Objects.requireNonNull(socket, "socket must not be null");
        Objects.requireNonNull(httpClientHandler, "httpClientHandler must not be null");

        this.socket = socket;
        this.httpClientHandler = httpClientHandler;
    }

    @Override
    public void run() {
        try {
            httpClientHandler.handleClient(socket);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while handling a client request", e);
        }
    }
}

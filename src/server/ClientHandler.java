package server;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

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

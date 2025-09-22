package server;

import java.io.IOException;
import java.net.Socket;

/**
 * Defines the contract for handling an individual HTTP client connection.
 * <p>
 * Implementations of this interface are responsible for:
 * <ul>
 *   <li>Reading the HTTP request from the given {@link Socket}</li>
 *   <li>Processing the request (e.g., parsing headers, handling methods like GET/PUT)</li>
 *   <li>Sending back the appropriate HTTP response</li>
 * </ul>
 * <p>
 * The {@code handleClient} method is invoked per accepted socket connection.
 */

public interface HttpClientHandler {
    void handleClient(final Socket socket) throws IOException;
}

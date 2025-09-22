package server;

import java.io.IOException;
import java.net.Socket;

public interface HttpClientHandler {
    void handleClient(final Socket socket) throws IOException;
}

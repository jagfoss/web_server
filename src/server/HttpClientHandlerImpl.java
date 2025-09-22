package server;

import common.Constants;
import common.Utility;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClientHandlerImpl implements HttpClientHandler {
    private static final Logger logger = Logger.getLogger(HttpClientHandlerImpl.class.getName());

    @Override
    public void handleClient(final Socket socket) throws IOException {
        Objects.requireNonNull(socket, "socket must not be null");

        try (socket; BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             OutputStream out = socket.getOutputStream()) {
            String httpRequestLine = "";
            try {
                httpRequestLine = in.readLine();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading request", e);
                return;
            }

            logger.log(Level.INFO, "Request: %s".formatted(httpRequestLine));
            processRequest(httpRequestLine, out);
        } finally {
            logger.log(Level.INFO, "Socket closed");
        }
    }

    private void processRequest(final String httpRequestLine, final OutputStream out) {
        Objects.requireNonNull(out, "out must not be null");

        try {
            if (httpRequestLine == null || httpRequestLine.isEmpty() || !Utility.isHttpRequestLineValid(httpRequestLine)) {
                logger.log(Level.WARNING, "Bad request line: %s".formatted(httpRequestLine));
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_BAD_REQUEST, Constants.HTTP_BAD_REQUEST_MESSAGE);
                return;
            }

            String[] httpRequestLineTokens = httpRequestLine.trim().split("\\s+");
            final String httpMethod = httpRequestLineTokens[0];
            String fileRequested = httpRequestLineTokens[1];

            if (httpMethod.equals(Constants.HTTP_METHOD_PUT)) {
                // We don't persist data, just return OK
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_OK, Constants.HTTP_OK_MESSAGE);
                return;
            }

            // We support only GET and PUT
            if (!httpMethod.equals(Constants.HTTP_METHOD_GET)) {
                logger.log(Level.WARNING, "Disallowed http method: %s".formatted(httpMethod));
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_BAD_METHOD, Constants.HTTP_BAD_METHOD_MESSAGE);
                return;
            }

            if (fileRequested.equals("/")) fileRequested = "/index.html";

            final File file = new File(Constants.WEB_ROOT, fileRequested);
            if (!file.exists() || file.isDirectory()) {
                logger.log(Level.WARNING, "Requested file: %s not found".formatted(fileRequested));
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_NOT_FOUND, Constants.HTTP_NOT_FOUND_MESSAGE);
                return;
            }

            final byte[] fileData = Utility.readFileData(file);
            final String contentType = Utility.getContentType(fileRequested);
            Utility.sendResponseWithData(out, HttpURLConnection.HTTP_OK, Constants.HTTP_OK_MESSAGE, contentType, fileData, false);
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Error processing request", exception);
            // After handling all client errors, we get here means a server. So, we send 5xx.
            try {
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_INTERNAL_ERROR, Constants.HTTP_INTERNAL_ERROR_MESSAGE);
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Error processing request", ioException);
            }
        }
    }
}

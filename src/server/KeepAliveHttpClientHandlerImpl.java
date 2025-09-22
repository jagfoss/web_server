package server;

import common.Constants;
import common.Utility;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


import static common.Constants.HTTP_OK_MESSAGE;
import static common.Constants.SOCKET_TIMEOUT_MILLI_SECONDS;

/**
 * KeepAlive version of the HttpClientHandler supporting HTTP GET and PUT.
 * This implementation uses blocking I/O. It can be improved using non-blocking I/O to scale better
 * with many concurrent connections.
 * <p>
 * Parts of the KeepAlive implementation of this code were generated with the help of OpenAI's ChatGPT.
 */
public class KeepAliveHttpClientHandlerImpl implements HttpClientHandler {
    private static final Logger logger = Logger.getLogger(KeepAliveHttpClientHandlerImpl.class.getName());

    @Override
    public void handleClient(final Socket socket) throws IOException {
        Objects.requireNonNull(socket, "socket must not be null");

        try (socket; BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); OutputStream out = socket.getOutputStream()) {

            socket.setSoTimeout(SOCKET_TIMEOUT_MILLI_SECONDS);
            boolean keepAlive = true;

            while (keepAlive) {
                String httpRequestLine;
                try {
                    httpRequestLine = in.readLine();
                } catch (java.net.SocketTimeoutException e) {
                    // No request within timeout, we can consider it as idle.
                    continue;
                } catch (IOException e) {
                    System.out.println("Error reading request: " + e.getMessage());
                    break;
                }

                logger.log(Level.INFO, "Request: %s".formatted(httpRequestLine));

                final Map<String, String> headers = getHeaders(in);
                final String connectionHeaderValue = headers.getOrDefault("connection", "close");
                keepAlive = connectionHeaderValue.equalsIgnoreCase("keep-alive");

                final int contentLength = getContentLength(headers);
                processRequest(httpRequestLine, in, contentLength, out, keepAlive);
            }

        } finally {
            logger.log(Level.INFO, "Socket closed");
        }
    }


    private void processRequest(final String httpRequestLine, final BufferedReader in, final int contentLength, final OutputStream out, final boolean keepAlive) {
        Objects.requireNonNull(in, "in must not be null");
        Objects.requireNonNull(out, "out must not be null");

        try {
            if (httpRequestLine == null || httpRequestLine.isEmpty() || !Utility.isHttpRequestLineValid(httpRequestLine)) {
                logger.log(Level.WARNING, "Bad request line: %s".formatted(httpRequestLine));
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_BAD_REQUEST, Constants.HTTP_BAD_REQUEST_MESSAGE);
                return;
            }

            if (contentLength < 0) {
                logger.log(Level.WARNING, "Bad http content length: %s".formatted(httpRequestLine));
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_BAD_REQUEST, Constants.HTTP_BAD_REQUEST_MESSAGE);
                return;
            }

            String[] httpRequestLineTokens = httpRequestLine.trim().split("\\s+");
            final String httpMethod = httpRequestLineTokens[0];
            String fileRequested = httpRequestLineTokens[1];

            // We need to clear the buffer, otherwise we may process the next request with old data.
            if (httpMethod.equals(Constants.HTTP_METHOD_PUT)) {
                clearBuffer(in, contentLength);
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_OK, Constants.HTTP_OK_MESSAGE);
                logger.log(Level.INFO, "HTTP PUT response");
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
            Utility.sendResponseWithData(out, HttpURLConnection.HTTP_OK, HTTP_OK_MESSAGE, contentType, fileData, keepAlive);
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Error processing request", exception);
            // After handling all client errors, we are getting here means a server error. So, we send 5xx.
            try {
                Utility.sendResponseWithoutData(out, HttpURLConnection.HTTP_INTERNAL_ERROR, Constants.HTTP_INTERNAL_ERROR_MESSAGE);
            } catch (IOException ioException) {
                logger.log(Level.SEVERE, "Error processing request", ioException);
            }
        }
    }

    private Map<String, String> getHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            String[] parts = line.split(":", 2);
            headers.put(parts[0].trim().toLowerCase(), parts[1].trim());
        }
        return headers;
    }

    private int getContentLength(Map<String, String> headers) {
        int contentLength = 0;
        if (headers.containsKey("content-length")) {
            try {
                contentLength = Integer.parseInt(headers.get("content-length"));
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid content length", e);
            }
        }
        return contentLength;
    }

    private void clearBuffer(final BufferedReader in, final int contentLength) throws IOException {
        char[] body = new char[contentLength];
        int read = 0;
        while (read < contentLength) {
            int r = in.read(body, read, contentLength - read);
            if (r == -1) break;
            read += r;
        }
    }
}

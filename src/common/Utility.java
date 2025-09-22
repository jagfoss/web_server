package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Parts of the OutputStream writing implementation of this code were generated with the help of OpenAI's ChatGPT.
 */
public class Utility {

    public static boolean isHttpRequestLineValid(final String httpRequestLine) {
        String[] requestLineTokens = httpRequestLine.trim().split("\\s+");
        return requestLineTokens.length >= 2;
    }

    public static byte[] readFileData(final File file) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        try (FileInputStream fis = new FileInputStream(file)) {
            final byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        }
    }

    public static void sendResponseWithoutData(final OutputStream out, final int statusCode, final String message) throws IOException {
        //TODO: Sanity check parameters
        out.write(("HTTP/1.1 " + statusCode + " " + message + "\r\n").getBytes());
        out.write(("Content-Type: text/html\r\n").getBytes());
        out.write(("Content-Length: 0\r\n").getBytes());
        out.write("\r\n".getBytes());
        out.flush();
    }

    public static void sendResponseWithData(final OutputStream out, final int statusCode, final String message, final String contentType, final byte[] fileData, final boolean keepAlive) throws IOException {
        //TODO: Sanity check parameters
        out.write(("HTTP/1.1 " + statusCode + " " + message + "\r\n").getBytes());
        out.write(("Content-Type: " + contentType + "\r\n").getBytes());
        out.write(("Content-Length: " + fileData.length + "\r\n").getBytes());
        out.write(("Connection: " + (keepAlive ? "keep-alive" : "close") + "\r\n").getBytes());
        out.write("\r\n".getBytes());
        out.write(fileData);
        out.flush();
    }

    public static String getContentType(final String fileRequested) {
        Objects.requireNonNull(fileRequested, "fileRequested must not be null");
        String fileExtension = "";
        final int dotIndex = fileRequested.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExtension = fileRequested.substring(dotIndex + 1).toLowerCase();
        }

        return switch (fileExtension) {
            case "html" -> "text/html";
            case "json" -> "application/json";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            default -> throw new IllegalArgumentException("Unsupported file requested: " + fileRequested);
        };
    }
}

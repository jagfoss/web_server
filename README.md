## Overview

### This project contains:
- A Java multi-threaded HTTP server with HTTP/1.1 keep-alive support.
- A house automation front-end implemented in HTML, CSS, and jQuery, fetching component states via HTTP.

## Project Structure

    web_server/
    ├─ src/          # Java HTTP server source code
    └─ www/          # Frontend assets
    ├─ index.html
    ├─ style.css
        ├─ js/        # JavaScript files
        └─ data/      # JSON files

## Running the Server and using the App

- Open the project in IntelliJ (or any other IDE). In IntelliJ, the easiest way is to open the project directly from the Git URL.
- Run `src/server/Main.java` to start the HTTP server (default port: 8000).
- Open a browser and navigate to: `http://localhost:8000/index.html`

## Extensibility

- KeepAlive support can be enabled/disabled by the HTTP_KEEP_ALIVE flag in Main.java (default: disabled).
- See comments in www/js/app.js for adding new UI components
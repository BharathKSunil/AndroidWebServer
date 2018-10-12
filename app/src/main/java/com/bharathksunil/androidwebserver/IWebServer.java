package com.bharathksunil.androidwebserver;

import com.bharathksunil.androidwebserver.exception.WebServerException;
import com.bharathksunil.androidwebserver.model.WebServerConfig;

/**
 * This interface represents the functionalities offered by the web server
 */
public interface IWebServer {
    /**
     * Call this method to start the server
     *
     * @throws WebServerException if the server could not be started
     */
    void startServer() throws WebServerException;

    /**
     * Call this method to stop the server
     */
    void stopServer();

    /**
     * Call this method to check the status of the server
     *
     * @return true, if the server is running
     */
    boolean isRunning();

    /**
     * Call this method to get the current server configurations
     *
     * @return the currently running web server config
     */
    WebServerConfig getWebServerConfig();
}

package com.bharathksunil.androidwebserver.model;

import io.reactivex.annotations.NonNull;

/**
 * This models the web server configurations
 */
public final class WebServerConfig {
    /**
     * The Ip address of the WebServer
     */
    @NonNull
    private final String ipAddress;
    /**
     * The port on which the server is listening to.
     */
    @NonNull
    private final String portNumber;
    /**
     * Run the Server in the background
     */
    private final boolean runInBackground;

    /**
     * Using this constructor will result in default configurations for fields other than the ones
     * passed in the params
     *
     * @param ipAddress  the Ip address of the device
     * @param portNumber the port to which the server must receive request
     */
    public WebServerConfig(@NonNull String ipAddress,
                           @NonNull String portNumber) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.runInBackground = false;
    }

    /**
     * @param ipAddress       the Ip address of the device
     * @param portNumber      the port to which the server must receive request
     * @param runInBackground if the server must run in the background
     */
    public WebServerConfig(@NonNull String ipAddress,
                           @NonNull String portNumber,
                           boolean runInBackground) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.runInBackground = runInBackground;
    }

    /**
     * @return the IP address to the WebServer
     */
    @NonNull
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return the Port number the WebServer is listening to
     */
    @NonNull
    public String getPortNumber() {
        return portNumber;
    }

    /**
     * Can the app run in background
     *
     * @return true if the app can run in background;
     */
    public boolean isRunInBackground() {
        return runInBackground;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WebServerConfig
                && ((WebServerConfig) obj).ipAddress.equals(this.ipAddress)
                && ((WebServerConfig) obj).portNumber.equals(this.portNumber)
                && ((WebServerConfig) obj).runInBackground == this.runInBackground;
    }
}

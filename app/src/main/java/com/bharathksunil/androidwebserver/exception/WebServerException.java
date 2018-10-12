package com.bharathksunil.androidwebserver.exception;

/**
 * This Exception class encloses all the exceptions that could occur on a local web server.
 */
public class WebServerException extends Exception {
    /**
     * This enum stores the different types of exceptions that could occur. Use the {@link #getType()}
     * call on the {@link WebServerException} object to get the type.
     */
    public enum Type {
        /**
         * This type of exception occurs when the port number passed is invalid
         */
        INVALID_PORT,
        /**
         * This type of exception occurs when the port passed is already being used by some other process
         */
        PORT_IN_USE,
        /**
         * This type of exception occurs when the Configuration passed is invalid or null
         */
        INVALID_CONFIG,
        /**
         * This type of exception occurs when the WiFi is turned off or is not On
         */
        WIFI_OFF,
        /**
         * This type of exception occurs when cause of it is unknown
         */
        UNKNOWN
    }

    /**
     * Stores the {@link Type} of exception
     */
    private final Type mType;

    /**
     * Default Constructor
     *
     * @param type pass the type of {@link WebServerException } that occurred
     */
    public WebServerException(Type type) {
        super(type.toString());
        mType = type;
    }

    /**
     * Call this method to get the {@link Type} of the error.
     *
     * @return the {@link Type} of Exception
     */
    public Type getType() {
        return mType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WebServerException
                && ((WebServerException) obj).getType().equals(this.getType());
    }
}

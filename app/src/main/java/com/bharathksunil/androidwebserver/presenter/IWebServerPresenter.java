package com.bharathksunil.androidwebserver.presenter;

import com.bharathksunil.androidwebserver.exception.WebServerException;
import com.bharathksunil.androidwebserver.model.WebServerConfig;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * This is the presenter for the WebSever. Use this to start and stop local web servers and
 * change their {@link WebServerConfig}.
 * <p>
 * <h3>How to Use:</h3><br/>
 * <ol>
 * <li>Implement the {@link View} interface in your view and call {@link #setView(View)}.</li>
 * <li>Call the {@link #onWiFiConnected(WebServerConfig)}</li> to send the connected wifi network. If it is
 * not passed then the server will not start
 * <li>Call the {@link #startServer()} to start the server.</li>
 * <li>Call the {@link #stopServer()} to stop the server.</li>
 * <li>Call {@link #onWiFiConnected(WebServerConfig)} and {@link #onWiFiDisconnected()} to tell the
 * presenter about any state changes in the wifi</li>
 * <li>To Disconnect to the presenter call {@link #setView(View)} with parameter null</li>
 * </ol>
 * </p>
 * <h3>How it works</h3><br/>
 * On passing the {@link View} instance to the {@link #setView(View)} command the presenter checks
 * if the server is running, if it is running then the it will call the {@link View#onServerStarted(WebServerConfig)}
 * method, else will call the {@link View#onServerStopped()}.<br/>
 * On passing null to the {@link #setView(View)} the presenter will stop the server but will not call
 * the {@link View#onServerStopped()}.
 */
public interface IWebServerPresenter {
    /**
     * Call this method to pass the {@link View} instance or null to disconnect with the view.
     * This also checks if the server is already running.
     *
     * @param view the {@link View} to which the presenter has to interact
     */
    void setView(@Nullable View view);

    /**
     * Call this method to star the local web server
     */
    void startServer();

    /**
     * Call this method to stop the server
     */
    void stopServer();

    /**
     * Call this method when the wifi is connected
     *
     * @param webServerConfig the new config with new IP address
     */
    void onWiFiConnected(@NonNull WebServerConfig webServerConfig);

    /**
     * Call this method to tell the presenter that the wifi has been disconnected
     */
    void onWiFiDisconnected();

    /**
     * This is the interface that lets the presenter interact with the view.
     */
    interface View {
        /**
         * This method is called whenever a server is started.
         *
         * @param webServerConfig the started web server configurations
         */
        void onServerStarted(@NonNull WebServerConfig webServerConfig);

        /**
         * This method is called whenever a server is stopped
         */
        void onServerStopped();

        /**
         * This method is called whenever there was an error with the server
         * @param exception       the exception that occured
         */
        void onError(@NonNull WebServerException exception);

        /**
         * This method is called to monitor the wifi status. Set a listener for monitoring the WiFi
         * state in this method. Call {@link #onWiFiConnected(WebServerConfig)} and {@link #onWiFiDisconnected()}
         * when the wifi is connected to a new network or gets disconnected respectively
         */
        void listenToWiFiStatus();
    }

}

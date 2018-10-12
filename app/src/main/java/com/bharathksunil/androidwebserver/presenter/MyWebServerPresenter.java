package com.bharathksunil.androidwebserver.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bharathksunil.androidwebserver.IWebServer;
import com.bharathksunil.androidwebserver.exception.WebServerException;
import com.bharathksunil.androidwebserver.model.WebServerConfig;

public final class MyWebServerPresenter implements IWebServerPresenter {
    /**
     * The current instance of the {@link com.bharathksunil.androidwebserver.presenter.IWebServerPresenter.View}
     * It is null when view is detached
     */
    @Nullable
    private View mView;
    /**
     * The instance of the web server
     */
    @NonNull
    private IWebServer mWebServer;
    /**
     * The Ip of active wifi
     * It is null when not connected to a wifi network
     */
    @Nullable
    private String wifiIp;

    public MyWebServerPresenter(@NonNull IWebServer mWebServer) {
        this.mWebServer = mWebServer;
    }

    //region Overridden Methods: IWebServerPresenter
    @Override
    public void setView(@Nullable View view) {
        this.mView = view;
        if (mView == null) {
            if (isServerRunning() && !mWebServer.getWebServerConfig().isRunInBackground())
                //noinspection ConstantConditions
                mWebServer.stopServer();
            return;
        }
        mView.listenToWiFiStatus();
        if (isServerRunning())
            mView.onServerStarted(mWebServer.getWebServerConfig());
        else
            mView.onServerStopped();
    }

    @Override
    public void startServer() {
        if (mView == null)
            return;
        WebServerConfig webServerConfig = mWebServer.getWebServerConfig();
        try {
            canStartServer(webServerConfig);    //check if server can be started with this config
            //if the server was already running, then send its config
            if (isServerRunning()) {
                mView.onServerStarted(mWebServer.getWebServerConfig());
                return;
            }//else start the server
            mWebServer.startServer();
            mView.onServerStarted(webServerConfig); //notify the view
        } catch (NumberFormatException e) { //thrown by MyWebServer() if invalid integer port
            mView.onError(new WebServerException(WebServerException.Type.INVALID_PORT));
        } catch (WebServerException e) { //thrown by canStartServer()
            mView.onError(e);
        }
    }

    @Override
    public void stopServer() {
        if (mView == null)
            return;
        //if the server is already stopped
        if (!isServerRunning()) {
            mView.onServerStopped();
            return;
        }
        //else stop the server
        //noinspection ConstantConditions as already checked in isServerRunning()
        mWebServer.stopServer();
        mView.onServerStopped();
    }

    @Override
    public void onWiFiConnected(WebServerConfig webServerConfig) {
        this.wifiIp = webServerConfig != null ? webServerConfig.getIpAddress() : null;
    }

    @Override
    public void onWiFiDisconnected() {
        this.wifiIp = null;
        //Tell the view that the device must be connected to a wifi
        if (mView != null)
            mView.onError(new WebServerException(WebServerException.Type.WIFI_OFF));
        //if the server is running then stop it.
        if (isServerRunning())
            //noinspection ConstantConditions as already checked in isServerRunning()
            mWebServer.stopServer();
    }
    //endregion

    /**
     * Call this method to check the status of the server
     *
     * @return true, if the server is running
     */
    private boolean isServerRunning() {
        return mWebServer.isRunning();
    }

    /**
     * This method checks if the server can be started with the parameters passed and also check
     * if the wifi is connected.
     *
     * @param webServerConfig the server config passed to presenter
     * @throws WebServerException is thrown if the server cannot be started
     */
    private void canStartServer(WebServerConfig webServerConfig) throws WebServerException {
        if (wifiIp == null || wifiIp.isEmpty())
            throw new WebServerException(WebServerException.Type.WIFI_OFF);
        if (webServerConfig == null)
            throw new WebServerException(WebServerException.Type.INVALID_CONFIG);
        if (webServerConfig.getPortNumber() == null || webServerConfig.getPortNumber().isEmpty())
            throw new WebServerException(WebServerException.Type.INVALID_PORT);
        try {
            int portNumber;
            portNumber = Integer.parseInt(webServerConfig.getPortNumber());
            if (portNumber <= 0)
                throw new WebServerException(WebServerException.Type.INVALID_PORT);
        } catch (NumberFormatException e) {
            throw new WebServerException(WebServerException.Type.INVALID_PORT);
        }

    }
}
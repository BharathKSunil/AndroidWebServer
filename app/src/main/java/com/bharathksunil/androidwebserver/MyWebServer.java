package com.bharathksunil.androidwebserver;

import com.bharathksunil.androidwebserver.exception.WebServerException;
import com.bharathksunil.androidwebserver.model.WebServerConfig;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import io.reactivex.annotations.NonNull;

public class MyWebServer extends NanoHTTPD implements IWebServer {

    /**
     * Stores the {@link WebServerConfig} for the currently running server
     */
    @NonNull
    private WebServerConfig mWebServerConfig;

    @SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
    public MyWebServer(@NonNull WebServerConfig webServerConfig) throws NumberFormatException {
        super(Integer.parseInt(webServerConfig.getPortNumber()));
        this.mWebServerConfig = webServerConfig;
    }

    @Override
    public Response serve(IHTTPSession session) {
        return newFixedLengthResponse("Hello World");
    }

    //region Overridden Methods: IWebServer
    @Override
    public void startServer() throws WebServerException {
        try {
            this.start(-1, true);
        } catch (IOException e) {
            throw new WebServerException(WebServerException.Type.PORT_IN_USE);
        }
    }

    @Override
    public void stopServer() {
        this.stop();
    }

    @Override
    public boolean isRunning() {
        return this.isAlive();
    }

    @Override
    public WebServerConfig getWebServerConfig() {
        return this.mWebServerConfig;
    }
    //endregion
}

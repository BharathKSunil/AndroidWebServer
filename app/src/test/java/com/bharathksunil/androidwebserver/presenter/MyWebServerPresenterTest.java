package com.bharathksunil.androidwebserver.presenter;

import com.bharathksunil.androidwebserver.IWebServer;
import com.bharathksunil.androidwebserver.exception.WebServerException;
import com.bharathksunil.androidwebserver.model.WebServerConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MyWebServerPresenterTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    //region Mock Objects
    @Mock
    private IWebServer mMockWebServer;
    @Mock
    private IWebServerPresenter.View mMockView;
    //endregion
    private WebServerConfig webServerConfig;

    @Before
    public void setup() {
        webServerConfig = new WebServerConfig(
                "192.168.1.1",
                "8080",
                true
        );
    }

    //region Basic Start and Stop Server Tests
    @Test
    public void startAStoppedServerTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(false);//set as stopped
        when(mMockWebServer.getWebServerConfig()).thenReturn(webServerConfig);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);

        webServerPresenter.onWiFiConnected(webServerConfig);
        webServerPresenter.startServer();

        //region Verify
        verify(mMockWebServer, times(1)).startServer();
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, never()).onError(any(WebServerException.class));
        verify(mMockView, times(1)).onServerStopped();//called when setView()
        verify(mMockView, times(1)).listenToWiFiStatus();
        verify(mMockView, times(1)).onServerStarted(webServerConfig);
        //endregion
    }

    @Test
    public void startAStartedServerTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(true);//set as started
        when(mMockWebServer.getWebServerConfig()).thenReturn(webServerConfig);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);

        webServerPresenter.onWiFiConnected(webServerConfig);
        webServerPresenter.startServer();

        //region Verify
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, never()).onError(any(WebServerException.class));
        verify(mMockView, never()).onServerStopped();
        verify(mMockView, times(1)).listenToWiFiStatus();
        verify(mMockView, times(2)).onServerStarted(webServerConfig);
        //endregion
    }

    @Test
    public void stopARunningServerTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(true); //set as running
        when(mMockWebServer.getWebServerConfig()).thenReturn(webServerConfig);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(webServerConfig);
        webServerPresenter.stopServer();

        //region Verify
        verify(mMockWebServer, times(1)).stopServer();
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, never()).onError(any(WebServerException.class));
        verify(mMockView, times(1)).onServerStarted(webServerConfig);//called when setView()
        verify(mMockView, times(1)).listenToWiFiStatus();
        verify(mMockView, times(1)).onServerStopped();
        //endregion
    }

    @Test
    public void stopAStoppedServerTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(false); //set as stopped

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(webServerConfig);
        webServerPresenter.stopServer();

        //region Verify
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, never()).getWebServerConfig(); //as server is not running
        verify(mMockView, never()).onError(any(WebServerException.class));
        verify(mMockView, never()).onServerStarted(webServerConfig);
        verify(mMockView, times(1)).listenToWiFiStatus();
        verify(mMockView, times(2)).onServerStopped();
        //endregion
    }
    //endregion

    //region Basic WiFi Related Tests
    @Test
    public void wifiNotConnectedButStartServerTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(false);
        when(mMockWebServer.getWebServerConfig()).thenReturn(webServerConfig);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        //onError is called here first as this is called with set view
        webServerPresenter.onWiFiDisconnected();
        //onError is called here the next time
        webServerPresenter.startServer();

        //region Verify
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, never()).stopServer();
        verify(mMockView, times(2))
                .onError(new WebServerException(WebServerException.Type.WIFI_OFF));
        verify(mMockView, never()).onServerStarted(webServerConfig);
        verify(mMockView, times(1)).onServerStopped();//once during setView
        verify(mMockView, times(1)).onServerStopped();
        //endregion
    }

    @Test
    public void wifiSwitchedOffWhileServerRunningTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(true); //initially set server running
        when(mMockWebServer.getWebServerConfig()).thenReturn(webServerConfig);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(webServerConfig); //initially connected
        webServerPresenter.startServer();

        //verify that the server has started
        verify(mMockWebServer, never()).startServer(); //as server is already running
        verify(mMockView, times(2)).onServerStarted(webServerConfig);

        webServerPresenter.onWiFiDisconnected();//simulate wifi gets disconnected

        //region Verify
        verify(mMockWebServer, times(1)).stopServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, never()).onServerStopped();
        verify(mMockView, times(2)).onServerStarted(webServerConfig);
        verify(mMockView, times(1))
                .onError(new WebServerException(WebServerException.Type.WIFI_OFF));
        verify(mMockView, times(1)).listenToWiFiStatus();
        //endregion
    }

    @Test
    public void wifiSwitchedOffWhileServerStoppedTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(false); //set server stopped

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(webServerConfig); //initially connected


        verify(mMockWebServer, never()).startServer(); //as server is stopped
        verify(mMockView, never()).onServerStarted(webServerConfig);
        verify(mMockView, times(1)).onServerStopped();
        verify(mMockView, never()).onError(any(WebServerException.class));

        webServerPresenter.onWiFiDisconnected();//simulate wifi gets disconnected

        //region Verify
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, never()).getWebServerConfig();
        verify(mMockView, times(1)).onServerStopped();
        verify(mMockView, never()).onServerStarted(webServerConfig);
        verify(mMockView, times(1))
                .onError(new WebServerException(WebServerException.Type.WIFI_OFF));
        verify(mMockView, times(1)).listenToWiFiStatus();
        //endregion
    }
    //endregion

    //region Passing Invalid Data Tests
    @Test
    public void startServerWithNullWifiSSIDTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(false);//set as stopped
        when(mMockWebServer.getWebServerConfig()).thenReturn(webServerConfig);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);

        webServerPresenter.onWiFiConnected(null);
        webServerPresenter.startServer();

        //region Verify
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, times(1))
                .onError(new WebServerException(WebServerException.Type.WIFI_OFF));
        verify(mMockView, times(1)).onServerStopped();//called when setView()
        verify(mMockView, times(1)).listenToWiFiStatus();
        verify(mMockView, never()).onServerStarted(webServerConfig);
        //endregion
    }

    @Test
    public void startServerWithInvalidPortNumberTest() throws WebServerException {
        WebServerConfig invalidPortConfig = new WebServerConfig(
                "something",
                "-50",
                true);
        when(mMockWebServer.isRunning()).thenReturn(false);
        when(mMockWebServer.getWebServerConfig()).thenReturn(invalidPortConfig);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(invalidPortConfig);
        webServerPresenter.startServer();

        //region Verify
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, times(1)).onServerStopped();
        verify(mMockView, never()).onServerStarted(invalidPortConfig);
        verify(mMockView, times(1))
                .onError(new WebServerException(WebServerException.Type.INVALID_PORT));
        verify(mMockView, times(1)).listenToWiFiStatus();
        //endregion
    }

    @Test
    public void startServerWithInvalidConfigTest() throws WebServerException {
        when(mMockWebServer.isRunning()).thenReturn(false);
        when(mMockWebServer.getWebServerConfig()).thenReturn(null);

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(webServerConfig);
        webServerPresenter.startServer();

        //region Verify
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, times(1)).onServerStopped();
        verify(mMockView, never()).onServerStarted(webServerConfig);
        verify(mMockView, times(1))
                .onError(new WebServerException(WebServerException.Type.INVALID_CONFIG));
        verify(mMockView, times(1)).listenToWiFiStatus();
        //endregion
    }
    //endregion

    //region When in Background Behaviour Test
    @Test
    public void isServerRunningWhenRunInBackgroundTrue() throws WebServerException {
        when(mMockWebServer.getWebServerConfig()).thenReturn(webServerConfig);
        when(mMockWebServer.isRunning()).thenReturn(true); //running initially

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(webServerConfig);

        verify(mMockView, times(1)).onServerStarted(webServerConfig);
        webServerPresenter.setView(null);   //simulate that the view is in the background

        //region Verify
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, never()).stopServer();
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, never()).onError(any(WebServerException.class));
        verify(mMockView, never()).onServerStopped();   //should not be called as view in background
        verify(mMockView, times(1)).listenToWiFiStatus();
        verify(mMockView, times(1)).onServerStarted(webServerConfig);
        //endregion
    }

    @Test
    public void isServerRunningWhenRunInBackgroundFalse() throws WebServerException {
        WebServerConfig myWebServerConfig = new WebServerConfig(
                "someIP",
                "8080",
                false
        );
        when(mMockWebServer.getWebServerConfig()).thenReturn(myWebServerConfig);
        when(mMockWebServer.isRunning()).thenReturn(true); //running initially

        IWebServerPresenter webServerPresenter = new MyWebServerPresenter(mMockWebServer);
        webServerPresenter.setView(mMockView);
        webServerPresenter.onWiFiConnected(myWebServerConfig);

        verify(mMockView, times(1)).onServerStarted(myWebServerConfig);
        webServerPresenter.setView(null);   //simulate that the view is in the background

        //region Verify
        verify(mMockWebServer, never()).startServer();
        verify(mMockWebServer, times(1)).stopServer(); //check if called
        verify(mMockWebServer, atLeastOnce()).isRunning();
        verify(mMockWebServer, atLeastOnce()).getWebServerConfig();
        verify(mMockView, never()).onError(any(WebServerException.class));
        verify(mMockView, never()).onServerStopped();   //should not be called as view in background
        verify(mMockView, times(1)).listenToWiFiStatus();
        verify(mMockView, times(1)).onServerStarted(myWebServerConfig);
        //endregion
    }
    //endregion
}
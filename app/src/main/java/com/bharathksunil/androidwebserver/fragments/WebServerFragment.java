package com.bharathksunil.androidwebserver.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import com.bharathksunil.androidwebserver.BuildConfig;
import com.bharathksunil.androidwebserver.MyWebServer;
import com.bharathksunil.androidwebserver.R;
import com.bharathksunil.androidwebserver.exception.WebServerException;
import com.bharathksunil.androidwebserver.model.WebServerConfig;
import com.bharathksunil.androidwebserver.presenter.IWebServerPresenter;
import com.bharathksunil.androidwebserver.presenter.MyWebServerPresenter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WebServerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class WebServerFragment extends Fragment implements IWebServerPresenter.View {

    //region CONSTANTS
    /**
     * The bundle key to store the port number
     */
    private static final String ARGUMENT_PORT_NUMBER = "ARGUMENT_PORT_NUMBER";
    private static final String ARGUMENT_RUN_IN_BACKGROUND = "ARGUMENT_RUN_IN_BACKGROUND";
    /**
     * The default port number
     */
    public static final String DEFAULT_PORT = "8080";
    public static final boolean DEFAULT_RUN_IN_BACKGROUND = true;
    /**
     * Log tag
     */
    public static final String TAG = WebServerFragment.class.getSimpleName();
    //endregion

    //region View Objects
    /**
     * The Server image {@link ImageSwitcher}
     */
    @BindView(R.id.is_main_server_status)
    ImageSwitcher mServerStatusImageSwitcher;
    /**
     * This Corresponds to the index of the Ip Address text view in {@link #mTextViewList}
     */
    public static final int INDEX_TV_IP_ADDRESS = 0;
    /**
     * This Corresponds to the index of the Server Status Message text view in {@link #mTextViewList}
     */
    public static final int INDEX_TV_SERVER_STATUS_MESSAGE = 1;
    /**
     * This Corresponds to the index of the Wifi SSID text view in {@link #mTextViewList}
     */
    public static final int INDEX_TV_WIFI_SSID = 2;
    /**
     * This Corresponds to the index of the App Version text view in {@link #mTextViewList}
     */
    public static final int INDEX_TV_APP_VERSION = 3;
    /**
     * The list of all text views in this fragment
     */
    @BindViews({R.id.tv_ip_address, R.id.tv_message, R.id.tv_wifi_ssid, R.id.tv_app_version})
    List<TextView> mTextViewList;
    private static final int IMG_RES_SERVER_ON = R.drawable.ic_web_server_on;
    private static final int IMG_RES_SERVER_OFF = R.drawable.ic_server_off;
    private static final int IMG_RES_SERVER_SWITCHING = R.drawable.ic_server_progress;
    private static final int IMG_RES_SERVER_ERROR = R.drawable.ic_server_error;
    //endregion

    @Nullable
    private OnFragmentInteractionListener mListener;
    /**
     * The presenter instance
     */
    private IWebServerPresenter mWebServerPresenter;
    /**
     * The wifi state changed broadcast receiver
     */
    @Nullable
    private WifiReceiver mWifiReceiver = null;
    /**
     * The port number fetched from the argument
     */
    private String mPortNumber;

    private boolean mRunInBackground;

    public WebServerFragment() {
        // Required empty public constructor
    }

    //region Overridden Methods: Fragments
    @SuppressWarnings("squid:S00112")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPortNumber = bundle.getString(ARGUMENT_PORT_NUMBER, DEFAULT_PORT);
            Log.d(TAG, "onCreateView: using port from argument: " + mPortNumber);
            mRunInBackground = bundle.getBoolean(ARGUMENT_RUN_IN_BACKGROUND, DEFAULT_RUN_IN_BACKGROUND);
            Log.d(TAG, "onCreateView: using run In background from argument: " + mRunInBackground);
        } else {
            mPortNumber = DEFAULT_PORT;
            Log.d(TAG, "onCreateView: using default port: " + mPortNumber);
            mRunInBackground = DEFAULT_RUN_IN_BACKGROUND;
            Log.d(TAG, "onCreateView: using default run in background: " + mRunInBackground);
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_server, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        mServerStatusImageSwitcher.setFactory(() -> {
            ImageView myView = new ImageView(requireActivity().getApplicationContext());
            myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            myView.setLayoutParams(new
                    ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return myView;
        });
        mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_SWITCHING);
        mWebServerPresenter = new MyWebServerPresenter(new MyWebServer(
                new WebServerConfig(getWiFiIp(), mPortNumber, mRunInBackground)
        ));
        mWebServerPresenter.setView(this);
        mTextViewList.get(INDEX_TV_APP_VERSION).setText(
                String.format(getString(R.string.format_app_version), BuildConfig.VERSION_NAME)
        );
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mWebServerPresenter.setView(null);
        mListener = null;
        if (mWifiReceiver != null)
            requireContext().unregisterReceiver(mWifiReceiver);
    }
    //endregion

    //region Overridden Methods: IWebServerPresenter.View
    @Override
    public void onServerStarted(WebServerConfig webServerConfig) {
        if (webServerConfig == null) {
            Log.e(TAG, "onServerStarted: webServerConfig is Null");
            return;
        }
        Log.i(TAG, "onServerStarted: " +
                "ipAddress: " + webServerConfig.getIpAddress() +
                "port: " + webServerConfig.getPortNumber());
        if (mListener != null)
            mListener.showPowerButton(false);
        mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_ON);
        mTextViewList.get(INDEX_TV_IP_ADDRESS).setText(getString(
                R.string.format_ip_address,
                webServerConfig.getIpAddress(),
                webServerConfig.getPortNumber())
        );
        mTextViewList.get(INDEX_TV_SERVER_STATUS_MESSAGE).setText(R.string.message_server_running);
    }

    @Override
    public void onServerStopped() {
        Log.i(TAG, "onServerStopped:");
        if (mListener != null)
            mListener.showPowerButton(true);
        mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_OFF);
        mTextViewList.get(INDEX_TV_IP_ADDRESS).setText(R.string.server_stopped);
        mTextViewList.get(INDEX_TV_SERVER_STATUS_MESSAGE).setText(R.string.message_server_stopped);
    }

    @Override
    public void onError(@NonNull WebServerException exception) {
        Log.e(TAG, "onError: " + exception.getType());
        if (mListener != null)
            mListener.showPowerButton(true);
        mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_ERROR);
        mTextViewList.get(INDEX_TV_IP_ADDRESS).setText(R.string.server_stopped);
        mTextViewList.get(INDEX_TV_SERVER_STATUS_MESSAGE).setText(getErrorMessage(exception));
    }

    @Override
    public void listenToWiFiStatus() {
        Log.d(TAG, "listenToWiFiStatus: called");
        if (getWiFiSSID() != null)
            mWebServerPresenter.onWiFiConnected(new WebServerConfig(
                    getWiFiIp(),
                    mPortNumber,
                    mRunInBackground
            ));
        else
            mWebServerPresenter.onWiFiDisconnected();
        updateWiFiStatusOnUI(getWiFiSSID());

        mWifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        requireContext().registerReceiver(mWifiReceiver, intentFilter);
    }
    //endregion

    /**
     * Call this factory method to create a new instance of this fragment
     *
     * @param portNumber the port the server must start
     * @return the new instance of {@link WebServerFragment}
     */
    public static WebServerFragment newInstance(@Nullable String portNumber, boolean runInBackground) {
        WebServerFragment webServerFragment = new WebServerFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_PORT_NUMBER, portNumber != null ? portNumber : DEFAULT_PORT);
        arguments.putBoolean(ARGUMENT_RUN_IN_BACKGROUND, runInBackground);
        webServerFragment.setArguments(arguments);
        return webServerFragment;
    }

    /**
     * This method is called by the activity to power on the server
     */
    public void powerOnWebServer() {
        Log.d(TAG, "powerOnWebServer: called");
        mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_SWITCHING);
        new Handler().postDelayed(() -> mWebServerPresenter.startServer(), 1000);

    }

    /**
     * This method is called by the activity to power off the server
     */
    public void powerOffWebServer() {
        Log.d(TAG, "powerOffWebServer: called");
        mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_SWITCHING);
        new Handler().postDelayed(() -> mWebServerPresenter.stopServer(), 1000);

    }

    /**
     * Call this method to get the Currently connected WiFi SSID
     *
     * @return Wifi SSID, null if wifi not connected
     */
    @Nullable
    private String getWiFiSSID() {
        WifiManager wifiManager = (WifiManager) requireActivity().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        String wifiSsid;
        if (wifiManager != null
                && wifiManager.isWifiEnabled()  //check if wifi hardware is enabled
                && wifiManager.getConnectionInfo() != null  //get the WifiInfo and check if null
                && wifiManager.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED
                && (wifiSsid = wifiManager.getConnectionInfo().getSSID()) != null)
            return wifiSsid;
        else
            return null;
    }

    /**
     * Call this method to get the WiFi Ip address
     *
     * @return the WiFi Ip Address, "" if unable to fetch IP address
     */
    @SuppressLint("DefaultLocale")
    private String getWiFiIp() {
        WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return "";
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return "";
        }

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException exception) {
            Log.e(TAG, "getWiFiIp: " + exception.getMessage());
        }
        return "";
    }

    /**
     * Call this method to get the error message from the {@link WebServerException.Type}
     *
     * @param exception the exception thrown
     * @return the localised string error message from the strings resource corresponding to the error
     */
    @NonNull
    private String getErrorMessage(WebServerException exception) {
        switch (exception.getType()) {
            case INVALID_PORT:
                return getString(R.string.error_invalid_port);
            case WIFI_OFF:
                return getString(R.string.error_wifi_off);
            case INVALID_CONFIG:
                return getString(R.string.error_invalid_config);
            case PORT_IN_USE:
                return String.format(getString(R.string.error_port_in_use), mPortNumber);
            case UNKNOWN:
            default:
                return getString(R.string.error_generic);
        }
    }

    /**
     * Call this method to update the wifi status on the UI
     *
     * @param ssid the ssid of the wifi, null if its disconnected
     */
    @SuppressWarnings("squid:S3398")
    private void updateWiFiStatusOnUI(@Nullable String ssid) {
        Log.d(TAG, "updateWiFiStatusOnUI: ssid : " + ssid);
        if (ssid == null || ssid.equals("<unknown ssid>")) {
            mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_ERROR);
            mTextViewList.get(INDEX_TV_WIFI_SSID).setText(R.string.wifi_disconnected);
            mTextViewList.get(INDEX_TV_WIFI_SSID).setCompoundDrawablesWithIntrinsicBounds(
                    requireContext().getDrawable(R.drawable.ic_signal_wifi_off),
                    null,
                    null,
                    null
            );
            mTextViewList.get(INDEX_TV_SERVER_STATUS_MESSAGE).setText(R.string.error_wifi_off);
        } else {
            mServerStatusImageSwitcher.setImageResource(IMG_RES_SERVER_OFF);
            mTextViewList.get(INDEX_TV_WIFI_SSID).setText(ssid.replaceAll("\"", ""));
            mTextViewList.get(INDEX_TV_WIFI_SSID).setCompoundDrawablesWithIntrinsicBounds(
                    requireContext().getDrawable(R.drawable.ic_signal_wifi_connected),
                    null,
                    null,
                    null
            );
            mTextViewList.get(INDEX_TV_SERVER_STATUS_MESSAGE).setText(R.string.message_server_stopped);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        /**
         * This method is called when the power button the activity must be enabled
         *
         * @param isPowerOn true if power On button must be enabled
         */
        void showPowerButton(boolean isPowerOn);
    }

    /**
     * This is a broadcast receiver which receives wifi changes.
     */
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (!WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action))
                return;
            if (mWebServerPresenter == null)
                return;
            String wifiSsid;
            if ((wifiSsid = getWiFiSSID()) != null && !wifiSsid.replaceAll("\"", "")
                    .equals("<unknown ssid>")) {
                Log.d(TAG, "onReceive: Wifi Connected:- " + wifiSsid);
                WebServerConfig newWebServerConfig = new WebServerConfig(
                        getWiFiIp(),
                        mPortNumber,
                        mRunInBackground
                );
                mWebServerPresenter.onWiFiConnected(newWebServerConfig);
            } else {
                Log.d(TAG, "onReceive: Wifi disconnected! ");
                mWebServerPresenter.onWiFiDisconnected();
            }
            updateWiFiStatusOnUI(wifiSsid);
        }
    }
}

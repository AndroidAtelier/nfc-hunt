package com.sqisland.nfc.hunt;

import android.util.Log;

import com.sqisland.nfc.hunt.retrofit.CloseSessionResponse;
import com.sqisland.nfc.hunt.retrofit.DeviceList;
import com.sqisland.nfc.hunt.retrofit.GenericBooleanResponse;
import com.sqisland.nfc.hunt.retrofit.HarmanService;
import com.sqisland.nfc.hunt.retrofit.Session;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brenda on 8/1/15.
 */
public class Harman {
    private String sessionID = "";
    private String deviceID  = "";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("http://10.0.1.3:8080/v1")
            .build();

    HarmanService service = restAdapter.create(HarmanService.class);

    public void initSession() {
      Log.e(MainActivity.TAG, "initSession");
        service.getSession(100, new Callback<Session>() {
            @Override
            public void success(Session session, Response response) {
                Log.e(MainActivity.TAG, session.SessionID);
                sessionID = session.SessionID;
                deviceID = "163093349480624";
                addDeviceToSession();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(MainActivity.TAG, error.getMessage());
            }
        });
    }

    private String getSessionID() {
        return sessionID;
    }

    private String getDeviceID() {
        return deviceID;
    }

    public void closeSession() {
        service.closeSession(getSessionID(), new Callback<CloseSessionResponse>() {
            @Override
            public void success(CloseSessionResponse closeSessionResponse, Response response) {
                Log.e(MainActivity.TAG, closeSessionResponse.Result);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(MainActivity.TAG, error.getMessage());
            }
        });
    }

    public void getDeviceList() {
        service.getDevices(getSessionID(), new Callback<DeviceList>() {
            @Override
            public void success(DeviceList deviceList, Response response) {
                if (deviceList != null) {
                    deviceID = "163093349480624"; // hard coding it because right now it's too much work to sort out multiple devices and pick the right one
                    Log.e(MainActivity.TAG, "fetched device " + deviceID);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(MainActivity.TAG, error.getMessage());
            }
        });
    }

    public void addDeviceToSession() {
        service.addDeviceToSession(getSessionID(), getDeviceID(), new Callback<GenericBooleanResponse>() {
            @Override
            public void success(GenericBooleanResponse genericBooleanResponse, Response response) {
                Log.e(MainActivity.TAG, "session add result = " + genericBooleanResponse.Result);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(MainActivity.TAG, error.getMessage());
            }
        });
    }

    public void playFromUrl(final String url) {
        service.playFromUrl(getSessionID(), url, new Callback<GenericBooleanResponse>() {
            @Override
            public void success(GenericBooleanResponse genericBooleanResponse, Response response) {
                Log.e(MainActivity.TAG, "playing from " + url);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(MainActivity.TAG, error.getMessage());
            }
        });
    }

    public void playFromUrlToAllConnectedSpeakers(final String url) {
      Log.e(MainActivity.TAG, "Playing to all: " + url + " with sessionID " + getSessionID() + ", deviceID " + getDeviceID());
      service.playFromUrlToAllConnectedSpeakers(getSessionID(), url, new Callback<GenericBooleanResponse>() {
            @Override
            public void success(GenericBooleanResponse genericBooleanResponse, Response response) {
                Log.e(MainActivity.TAG, "playing from " + url + " to all connected speakers");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(MainActivity.TAG, error.getMessage());
            }
        });
    }

    public void playFromUrlToSelectedSpeaker(final String url) {
      Log.e(MainActivity.TAG, "Playing to specific: " + url + " with sessionID " + getSessionID() + ", deviceID " + getDeviceID());
        service.playFromUrlToASelectedSpeaker(getSessionID(), url, getDeviceID(), new Callback<GenericBooleanResponse>() {
            @Override
            public void success(GenericBooleanResponse genericBooleanResponse, Response response) {
                Log.e(MainActivity.TAG, "playing from " + url + " to a specific speaker ");
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}

package com.sqisland.nfc.hunt.retrofit;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by brenda on 8/1/15.
 */
public interface HarmanService {
    @GET("/init_session")
    void getSession(@Query("Priority") int priority, Callback<Session> cb);

    @GET("/close_session")
    void closeSession(@Query("SessionID") String sessionId, Callback<CloseSessionResponse> cb);

    @GET("/device_list")
    void getDevices(@Query("SessionID") String sessionId, Callback<DeviceList> cb);

    @GET("/add_device_to_session")
    void addDeviceToSession(@Query("SessionID") String sessionId, @Query("DeviceID") String deviceId, Callback<GenericBooleanResponse> cb);

    @GET("/play_web_media")
    void playFromUrl(@Query("SessionID") String sessionId, @Query("MediaUrl") String url, Callback<GenericBooleanResponse> cb);

    @GET("/play_web_media_party_mode")
    void playFromUrlToAllConnectedSpeakers(@Query("SessionID") String sessionId, @Query("MediaUrl") String url, Callback<GenericBooleanResponse> cb);

    @GET("/play_web_media_selected_speakers")
    void playFromUrlToASelectedSpeaker(@Query("SessionID") String sessionId,
                                       @Query("MediaUrl") String url,
                                       @Query("DeviceIDList") String deviceId,
                                       Callback<GenericBooleanResponse> cb);
}

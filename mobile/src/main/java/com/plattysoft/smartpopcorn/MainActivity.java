package com.plattysoft.smartpopcorn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {

    private IoTCarRemote mApiEndpoint;
    private static final String RASPBERRY_PI_ADDRESS = "192.168.0.180";

    public interface IoTCarRemote {
        @POST("command")
        Call<Void> sendCommand(@Body PopcornCommand command);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        mApiEndpoint = new Retrofit.Builder()
                .baseUrl("http://"+RASPBERRY_PI_ADDRESS+":8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(IoTCarRemote.class);
    }

    private void sendCommand(PopcornCommand value) {
        mApiEndpoint.sendCommand(value).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Mark the buttos as selected
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Notify the user TODO
            }
        });
    }

    public void startButtonClick(View view){
        sendCommand(PopcornCommand.START);
    }

    public void stopButtonClick(View view){
        sendCommand(PopcornCommand.CANCEL);
    }
}

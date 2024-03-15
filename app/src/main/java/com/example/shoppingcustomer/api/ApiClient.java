package com.example.shoppingcustomer.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://t5g6abszk1.execute-api.us-east-2.amazonaws.com/test/";
    private static ApiClient instance;
    private ApiService apiService;
    private static Retrofit retrofit = null;

    private static HttpLoggingInterceptor interceptor
            = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY); // for logging and debugging

    public static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build();

    public static Retrofit getClient() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }


}

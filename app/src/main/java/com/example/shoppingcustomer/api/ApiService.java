package com.example.shoppingcustomer.api;

import com.example.shoppingcustomer.model.DataBody;
import com.example.shoppingcustomer.model.Product;
import com.example.shoppingcustomer.model.ProductTestRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
   @POST("detect-url")
   Call<DataBody> detectObject(
           /* @Query("box_conf") float boxConfidence,
            @Query("annotate") int annotate,
            @Query("version") String version,
            @Query("return_name") int returnName,*/
            @Body RequestBody image
    );

    @Multipart
    @POST("detect-url")
    Call<DataBody> detectObjectPart(
            @Part MultipartBody.Part image
    );
}

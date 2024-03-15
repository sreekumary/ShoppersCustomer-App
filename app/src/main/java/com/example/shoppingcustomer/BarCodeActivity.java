package com.example.shoppingcustomer;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shoppingcustomer.api.ApiClient;
import com.example.shoppingcustomer.api.ApiService;
import com.example.shoppingcustomer.databinding.ActivityMainBinding;
import com.example.shoppingcustomer.model.DataBody;
import com.example.shoppingcustomer.model.Product;
import com.example.shoppingcustomer.viewmodel.BarcodeViewModel;
import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BarCodeActivity extends AppCompatActivity {

    private BarcodeViewModel viewModel;
    private ActivityMainBinding binding;

    private ResultAdapter adapter;

    List<Product> resultList =new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        viewModel = new ViewModelProvider(this).get(BarcodeViewModel.class);
        binding.setBarcodeViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setAdapter();
        getProductList();
        getImage();
        // Start camera
        viewModel.startCamera(this);

    }

    private void getImage(){
        viewModel.getScanImage().observe(this, new Observer<byte[]>(){
            @Override
            public void onChanged(byte[] byteImage) {
                Toast.makeText(BarCodeActivity.this, ""+byteImage, Toast.LENGTH_SHORT).show();

               // binding.imagCart.setImageBitmap(byteImage);
                byte[] imageByteArray = viewModel.getScanImage().getValue();
                if (imageByteArray!= null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                    binding.imagCart.setImageBitmap(bitmap);
                }
                 viewModel.callApi(imageByteArray);
            }
        });
    }


    private void setAdapter() {
        adapter=new ResultAdapter(resultList);
        binding.recyclerView2.setAdapter(adapter);
    }


    private void getProductList() {
        viewModel.getScanResultsLiveData().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> resultLis) {

                resultList.addAll(resultLis);
                adapter.notifyDataSetChanged();
                // adapter.setProductList(results);
            }
        });
    }

    public void callApi(byte[] imageByteArray) {


        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageByteArray);


        // Create an instance of the ApiService interface
        Call<DataBody> call = apiService.detectObject(requestBody);
        call.enqueue(new Callback<DataBody>() {
            @Override
            public void onResponse(@NonNull Call<DataBody> call, @NonNull Response<DataBody> response) {
                // Handle success response
                if (response.isSuccessful()) {
                    DataBody responseModel = response.body();
                    // Handle response data
                    Log.e("wwww", (String) responseModel.getInference().getLabels().get(0));
                } else {
                    // Handle error response
                    ResponseBody errorBody = response.errorBody();
                    Log.e("errorBody",response.toString());

                    // Handle error
                }
            }

            @Override
            public void onFailure(@NonNull Call<DataBody> call, Throwable t) {
                // Handle failure
                Log.e("Upload Error", t.getMessage());
            }
        });

    }

}

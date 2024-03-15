package com.example.shoppingcustomer.viewmodel;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.example.shoppingcustomer.R;
import com.example.shoppingcustomer.api.ApiClient;
import com.example.shoppingcustomer.api.ApiService;
import com.example.shoppingcustomer.model.DataBody;
import com.example.shoppingcustomer.model.Product;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BarcodeViewModel extends ViewModel {
    private static final String TAG = "MLKit Barcode";
    private static final int PERMISSION_CODE = 1001;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private MutableLiveData<InputImage> inputImages=new MutableLiveData<>();

    private MutableLiveData<byte[]> bitmapMutableLiveData=new MutableLiveData<>();

    private MutableLiveData<List<Product>> scanResultsLiveData = new MutableLiveData<>();
    //private List<String> scanResults = new ArrayList<>();
    private static final String BASE_URL = "https://t5g6abszk1.execute-api.us-east-2.amazonaws.com/test/";

    private PreviewView previewView;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private Preview previewUseCase;
    private ImageAnalysis analysisUseCase;

    private ImageView imageView;

    public LiveData<List<Product>> getScanResultsLiveData() {
        return scanResultsLiveData;
    }

    public void startCamera(Activity activity) {
        previewView = activity.findViewById(R.id.barcodeScannerView);
        if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            setupCamera(activity);
        } else {
            getPermissions(activity);
        }
    }

    private void getPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION}, PERMISSION_CODE);
    }

    public void setupCamera(Activity activity) {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(activity);

        int lensFacing = CameraSelector.LENS_FACING_BACK;
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindAllCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "cameraProviderFuture.addListener Error", e);
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {
        if (cameraProvider == null) {
            return;
        }

        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        builder.setTargetRotation(getRotation());

        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());

        try {
            cameraProvider
                    .bindToLifecycle((LifecycleOwner) previewView.getContext(), cameraSelector, previewUseCase);
        } catch (Exception e) {
            Log.e(TAG, "Error when bind preview", e);
        }
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }

        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }

        Executor cameraExecutor = Executors.newSingleThreadExecutor();

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        builder.setTargetRotation(getRotation());

        analysisUseCase = builder.build();
        analysisUseCase.setAnalyzer(cameraExecutor, this::analyze);

        try {
            cameraProvider
                    .bindToLifecycle((LifecycleOwner) previewView.getContext(), cameraSelector, analysisUseCase);
        } catch (Exception e) {
            Log.e(TAG, "Error when bind analysis", e);
        }
    }

    private int getRotation() throws NullPointerException {
        return previewView.getDisplay().getRotation();
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyze(@NonNull ImageProxy image) {
        if (image.getImage() == null) return;

        // Create a Handler to introduce delay
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            InputImage inputImage = InputImage.fromMediaImage(
                    image.getImage(),
                    image.getImageInfo().getRotationDegrees()
            );

            BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            // Barcodes detected, process each barcode
                            for (Barcode barcode : barcodes) {
                                String result = barcode.getDisplayValue();
                                List<Product> productList = new ArrayList<>();
                                productList.add(new Product(result, 4.99, 1));
                                scanResultsLiveData.postValue(productList);
                            }
                            image.close();
                        } else {

                            Bitmap bitmap = inputImageToBitmap(inputImage);
                            byte[] byteArray = bitmapToByteArray(bitmap);
                            bitmapMutableLiveData.postValue(byteArray);

                        }
                        image.close();

                    })
                    .addOnFailureListener(e -> {
                        //Log.e(TAG, "Barcode process failure", e);
                    })
                    .addOnCompleteListener(task -> image.close());
        }, 3000); // 3 seconds delay
    }

    public Bitmap inputImageToBitmap(InputImage inputImage) {
        // Get the media image from InputImage
        Image mediaImage = inputImage.getMediaImage();

        if (mediaImage == null) {
            // Handle the case where mediaImage is null
            return null;
        }

        // Get the rotation degrees from inputImage
        int rotationDegrees = inputImage.getRotationDegrees();

        // Get the image format from mediaImage
        int imageFormat = mediaImage.getFormat();

        Bitmap bitmap = null;

        // Convert mediaImage to Bitmap
        if (imageFormat == ImageFormat.YUV_420_888 || imageFormat == ImageFormat.NV21) {
            bitmap = convertYuvToBitmap(mediaImage, rotationDegrees);
        } else if (imageFormat == ImageFormat.JPEG || imageFormat == ImageFormat.RGB_565 || imageFormat == ImageFormat.YUV_420_888) {
            bitmap = convertImageToBitmap(mediaImage);
        }

        // Display the bitmap in ImageView
        if (bitmap != null) {
            // Convert Bitmap to byte array
            byte[] byteArray = bitmapToByteArray(bitmap);
            // Post the bitmap and byte array
            bitmapMutableLiveData.postValue(byteArray);
           // byteArrayLiveData.postValue(byteArray);
        }
        return bitmap;
    }



    private Bitmap convertYuvToBitmap(Image image, int rotationDegrees) {
        // Get the image dimensions
        int width = image.getWidth();
        int height = image.getHeight();

        // Get the YUV data from the image
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer bufferY = planes[0].getBuffer(); // Y plane
        ByteBuffer bufferU = planes[1].getBuffer(); // U plane
        ByteBuffer bufferV = planes[2].getBuffer(); // V plane

        int strideY = planes[0].getRowStride();
        int strideU = planes[1].getRowStride();
        int strideV = planes[2].getRowStride();

        // Create byte arrays to hold the data
        byte[] dataY = new byte[bufferY.remaining()];
        byte[] dataU = new byte[bufferU.remaining()];
        byte[] dataV = new byte[bufferV.remaining()];

        // Copy data from buffers to arrays
        bufferY.get(dataY);
        bufferU.get(dataU);
        bufferV.get(dataV);

        // Create an array to hold the converted image data
        int[] imageData = new int[width * height];

        // Convert YUV to RGB
        YUVtoRGB(dataY, dataU, dataV, imageData, width, height, strideY, strideU, strideV);

        // Create a Bitmap from the RGB data
        Bitmap bitmap = Bitmap.createBitmap(imageData, width, height, Bitmap.Config.ARGB_8888);

        // Rotate the bitmap if necessary
        if (rotationDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        return bitmap;
    }

    private void YUVtoRGB(byte[] yData, byte[] uData, byte[] vData, int[] output, int width, int height, int strideY, int strideU, int strideV) {
       try{
           final int frameSize = width * height;
           int uvHeight = height / 2;
           int yIndex = 0;
           int uvIndex = 0;

           for (int j = 0; j < height; j++) {
               int yIndexEnd = yIndex + width;

               for (int i = yIndex, x = 0; i < yIndexEnd; i++, x++) {
                   int Y = yData[i] & 0xFF;
                   int uv = (j >> 1) * strideU + (x & ~1);

                   int U = (uData[uv] & 0xFF) - 128;
                   int V = (vData[uv] & 0xFF) - 128;

                   int Y1192 = 1192 * Y;
                   int R = (Y1192 + 1634 * V) >> 10;
                   int G = (Y1192 - 833 * V - 400 * U) >> 10;
                   int B = (Y1192 + 2066 * U) >> 10;

                   R = Math.max(0, Math.min(255, R));
                   G = Math.max(0, Math.min(255, G));
                   B = Math.max(0, Math.min(255, B));

                   output[i] = 0xFF000000 | (R << 16) | (G << 8) | B;
               }

               yIndex += strideY;
               if ((j & 1) == 0) {
                   uvIndex += strideV;
               }
           }
       }catch (Exception e){

       }


    }

    private Bitmap convertImageToBitmap(Image image) {
        // Get the image dimensions
        int width = image.getWidth();
        int height = image.getHeight();

        // Define a bitmap with the given dimensions and ARGB_8888 configuration
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Depending on the image format, you may need to extract pixel data from the image
        // and populate the bitmap accordingly.

        // For example, if the image format is JPEG, you might use something like:
        // ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        // byte[] bytes = new byte[buffer.remaining()];
        // buffer.get(bytes);
        // bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // If the image format is RGB_565 or YUV_420_888, you might use another approach
        // to extract and convert the pixel data to populate the bitmap.

        // You need to handle different image formats appropriately based on your requirements.

        return bitmap;
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }


public LiveData<byte[]> getScanImage() {
        return bitmapMutableLiveData;
    }


    public void callApi(byte[] imageByteArray) {


        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageByteArray.toString());


        // Create an instance of the ApiService interface
        Call<DataBody> call = apiService.detectObject(requestBody);
        call.enqueue(new Callback<DataBody>() {
            @Override
            public void onResponse(@NonNull Call<DataBody> call, @NonNull Response<DataBody> response) {
                // Handle success response
                if (response.isSuccessful()) {
                    DataBody responseModel = response.body();
                    // Handle response data
                    Log.e("wwww", (responseModel.toString()));
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


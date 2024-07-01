package com.example.monumentdetection1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.monumentdetection1.R;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button bCapture, bToggleCamera;
    private TextView tvResult;
    private static final String TAG = CameraActivity.class.getSimpleName();
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        bCapture = findViewById(R.id.bCapture);
        bToggleCamera = findViewById(R.id.bToggleCamera);
        tvResult = findViewById(R.id.tvResult);
        // Initialize TensorFlow Lite interpreter
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            Log.e(TAG, "Error loading TensorFlow Lite model: " + e.getMessage());
            e.printStackTrace();
        }

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cameraProviderListenableFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    startCamera(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, getExecutor());
        }

        bCapture.setOnClickListener(v -> {
            capturePhoto();
        });

        bToggleCamera.setOnClickListener(v -> {
            lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            cameraProviderListenableFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    startCamera(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, getExecutor());
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        try {
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, "model.tflite");
            tflite = new Interpreter(tfliteModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraProviderListenableFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    startCamera(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, getExecutor());
        }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCamera(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void capturePhoto() {
        long timestamp = System.currentTimeMillis();
        String photoFileName = timestamp + ".jpg";
        File photoFile = new File(getExternalFilesDir(null), photoFileName);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, getExecutor(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.d(TAG, "Photo saved successfully: " + photoFile.getAbsolutePath());
                Toast.makeText(CameraActivity.this, "Photo saved successfully.", Toast.LENGTH_SHORT).show();
                recognizeImage(photoFile.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.d(TAG, "Error saving photo: " + exception.getMessage());
                Toast.makeText(CameraActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recognizeImage(String imagePath) {
        if (tflite == null) {
            Toast.makeText(this, "TensorFlow Lite model is not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);

        ByteBuffer byteBuffer = tensorImage.getBuffer();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 1001}, DataType.FLOAT32);

        tflite.run(byteBuffer, outputBuffer.getBuffer().rewind());


        float[] outputArray = outputBuffer.getFloatArray();
        int maxIndex = 0;
        float maxScore = outputArray[0];
        for (int i = 1; i < outputArray.length; i++) {
            if (outputArray[i] > maxScore) {
                maxScore = outputArray[i];
                maxIndex = i;
            }
        }

        String result = "Recognized Object ID: " + maxIndex;
        tvResult.setText(result);
        Log.d(TAG, result);
    }
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("mobilenet_v2_1.0_224.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}

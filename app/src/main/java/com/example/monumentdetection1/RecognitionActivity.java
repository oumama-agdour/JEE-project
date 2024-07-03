package com.example.monumentdetection1;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class RecognitionActivity extends AppCompatActivity {
    private static final String TAG = RecognitionActivity.class.getSimpleName();
    private Interpreter tflite;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        tvResult = findViewById(R.id.tvResult);

        String imagePath = getIntent().getStringExtra("imagePath");

        // Initialize TensorFlow Lite interpreter
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            Log.e(TAG, "Error loading TensorFlow Lite model: " + e.getMessage());
            e.printStackTrace();
        }

        if (imagePath != null) {
            recognizeImage(imagePath);
        } else {
            Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show();
        }
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
        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("mobilenet_v1_1.0_224_quant.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}

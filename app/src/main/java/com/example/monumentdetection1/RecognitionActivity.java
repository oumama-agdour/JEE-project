package com.example.monumentdetection1;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RecognitionActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_CODE = 101;

    private Interpreter interpreter;
    private TextView textViewResult;
    private Map<String, String> landmarks;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        textViewResult = findViewById(R.id.textViewResult);
        imageView = findViewById(R.id.imageView);

        Button buttonCapture = findViewById(R.id.buttonCapture);
        buttonCapture.setOnClickListener(view -> captureImage());

        loadLandmarks();
        initializeInterpreter();

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    private void captureImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_CAPTURE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(imageBitmap);
                recognizeImage(imageBitmap);
            } else {
                Log.e("RecognitionActivity", "No data received from camera");
            }
        }
    }

    private void loadLandmarks() {
        landmarks = new HashMap<>();
        try {
            InputStream is = getAssets().open("landmarks_classifier_africa_V1_label_map.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 2) {
                    landmarks.put(tokens[0], tokens[1]);
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.e("RecognitionActivity", "Error loading landmarks", e);
        }
    }

    private void initializeInterpreter() {
        try {
            Interpreter.Options options = new Interpreter.Options();
            interpreter = new Interpreter(loadModelFile("1.tflite"), options);
        } catch (IOException e) {
            Log.e("RecognitionActivity", "Error initializing interpreter", e);
        }
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void recognizeImage(Bitmap bitmap) {
        if (interpreter == null) {
            Log.e("RecognitionActivity", "Interpreter is not initialized");
            return;
        }

        int modelInputSize = 321; // Assurez-vous que cela correspond à la taille attendue par le modèle

        // Redimensionnez l'image à la taille attendue par le modèle
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true);

        // Chargez l'image redimensionnée dans un TensorImage
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);

        // Obtenez le ByteBuffer de l'image normalisée
        ByteBuffer inputBuffer = tensorImage.getBuffer();

        // Assurez-vous que la taille du ByteBuffer correspond à la taille attendue
        int inputSize = modelInputSize * modelInputSize * 3;  // Assuming RGB image
        if (inputBuffer.capacity() != inputSize * 4) {  // 4 bytes per float
            Log.e("RecognitionActivity", "Input buffer size does not match expected input size");
            return;
        }

        // Normalisez les données d'entrée comme nécessaire
        float[] normalizedInput = new float[inputSize];
        for (int i = 0; i < inputSize; i++) {
            normalizedInput[i] = (inputBuffer.get(i)) / 255.0f;
        }

        // Créez un ByteBuffer pour les données d'entrée normalisées
        ByteBuffer normalizedInputBuffer = ByteBuffer.allocateDirect(inputSize * 4);  // 4 bytes per float
        normalizedInputBuffer.order(ByteOrder.nativeOrder());
        normalizedInputBuffer.asFloatBuffer().put(normalizedInput);

        // Obtenez les détails du tenseur de sortie pour comprendre sa forme et sa taille
        Tensor outputTensor = interpreter.getOutputTensor(0);
        int[] outputShape = outputTensor.shape();
        DataType outputDataType = outputTensor.dataType();

        // Allouez le TensorBuffer de sortie basé sur la forme du tenseur de sortie
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

        // Exécutez l'inférence avec les données d'entrée normalisées
        interpreter.run(normalizedInputBuffer, outputBuffer.getBuffer());

        // Traitez la sortie du modèle
        processModelOutput(outputBuffer.getFloatArray());
    }


    private void processModelOutput(float[] output) {
        // Find the index with the highest probability
        int maxIndex = -1;
        float maxProbability = -1;
        for (int i = 0; i < output.length; i++) {
            if (output[i] > maxProbability) {
                maxProbability = output[i];
                maxIndex = i;
            }
        }

        // Get the label corresponding to the highest probability
        String recognizedLabel = String.valueOf(maxIndex);
        String recognizedDescription = landmarks.getOrDefault(recognizedLabel, "Unknown");

        // Update UI with the recognized description
        runOnUiThread(() -> textViewResult.setText("Recognized: " + recognizedDescription));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera operations
                captureImage();
            } else {
                // Permission denied, handle accordingly (e.g., show a message or disable functionality)
                Log.w("RecognitionActivity", "Camera permission denied");
            }
        }
    }
}

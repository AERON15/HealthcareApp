package com.example.healthcareapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;

public class MainActivity extends AppCompatActivity {

    private HandwritingView handwritingView;
    private DigitalInkRecognizer recognizer;
    private TextView resultText;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable recognizeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handwritingView = findViewById(R.id.handwritingView);
        Button convertButton = findViewById(R.id.convertButton);

        resultText = new TextView(this);
        resultText.setTextSize(18f);
        ((android.widget.LinearLayout) findViewById(R.id.main)).addView(resultText);

        setupMLKit();

        // Manual button (optional)
        convertButton.setOnClickListener(v -> recognizeInk());
    }

    private void setupMLKit() {
        DigitalInkRecognitionModelIdentifier modelIdentifier;

        try {
            modelIdentifier =
                    DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        } catch (Exception e) {
            Toast.makeText(this, "Language not supported", Toast.LENGTH_LONG).show();
            return;
        }

        DigitalInkRecognitionModel model =
                DigitalInkRecognitionModel.builder(modelIdentifier).build();

        recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build()
        );

        RemoteModelManager.getInstance()
                .download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "ML Kit ready", Toast.LENGTH_SHORT).show()
                );

        // 🔥 REAL-TIME recognition while writing
        handwritingView.setOnInkChangedListener(() -> {
            if (recognizeRunnable != null) {
                handler.removeCallbacks(recognizeRunnable);
            }

            recognizeRunnable = this::recognizeInk;
            handler.postDelayed(recognizeRunnable, 500); // debounce
        });
    }

    private void recognizeInk() {
        if (recognizer == null) return;

        Ink ink = handwritingView.getInk();

        recognizer.recognize(ink)
                .addOnSuccessListener(result -> {
                    if (!result.getCandidates().isEmpty()) {
                        String text = result.getCandidates().get(0).getText();

                        // Update text
                        resultText.append(text + " ");

                        // ✅ CLEAR handwriting AFTER recognition
                        handwritingView.clear();
                    }
                })
                .addOnFailureListener(e -> {
                    // Do nothing (avoid clearing on failure)
                });
    }

}

package com.example.gpt_application;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String API_URL = "https://api.openai.com/v1/engines/text-davinci-002/completions";

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUIElements();
    }

    private void sendToGPT3(String userInput) {
        executor.execute(() -> {
            String result = performGPT3Request(userInput);
            runOnUiThread(() -> handleGPT3Response(result));
        });
    }

    private String performGPT3Request(String userInput) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);

            String input = "{\"prompt\":\"" + userInput + "\",\"max_tokens\":50}";
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: Unable to connect to the API.";
        }
    }

    private void handleGPT3Response(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            JSONArray choices = jsonResult.getJSONArray("choices");
            JSONObject choice = choices.getJSONObject(0);
            String generatedText = choice.getString("text");
            TextView responseView = findViewById(R.id.responseView);
            responseView.setText(generatedText.trim());
        } catch (JSONException e) {
            Log.e("GPT3Response", "JSONException: ", e);
            TextView responseView = findViewById(R.id.responseView);
            String errorMessage = getString(R.string.error_parsing_json, result);
            responseView.setText(errorMessage);
        }
    }

    private void setupUIElements() {
        final EditText userInput = findViewById(R.id.userInput);
        Button sendButton = findViewById(R.id.submitButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToGPT3(userInput.getText().toString());
            }
        });
    }
}


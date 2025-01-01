package com.search;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class HuggingFaceClient {

    private static final String API_URL = "https://api-inference.huggingface.co/models/sentence-transformers/all-MiniLM-L6-v2";
    private static final String API_KEY = "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"; 


    public static String generateEmbedding(String sourceSentence, String[] sentences) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Prepare JSON payload
            StringBuilder sentencesArrayJson = new StringBuilder("[");
            for (int i = 0; i < sentences.length; i++) {
                sentencesArrayJson.append("\"").append(sentences[i]).append("\"");
                if (i < sentences.length - 1) {
                    sentencesArrayJson.append(",");
                }
            }
            sentencesArrayJson.append("]");

            String payload = String.format("{\"inputs\": {\"source_sentence\": \"%s\", \"sentences\": %s}}",
                    sourceSentence, sentencesArrayJson.toString());

            // Write payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            // Read response
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return response.toString();
        } catch (Exception e) {
            System.out.println("\u001b[31mERROR:\u001b[0m HuggingFace API error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

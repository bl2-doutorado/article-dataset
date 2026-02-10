package com.canalbl2.planner.features.cloudcostclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class CloudCostClient {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final HttpClient httpClient =
      HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

  // private static final String url = "http://localhost:8183/milp";

  public static Map<String, Object> computePlacement(String url, Map<String, Object> configMap) {
    try {
      String jsonPayload = mapper.writeValueAsString(configMap);
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
              .build();
      System.out.println("🚀 Sending configuration to: " + url);

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        System.out.println("✅ Success! Code: " + response.statusCode());
        // System.out.println("Response: " + response.body());
      } else {
        System.err.println("❌ Error: " + response.statusCode());
        System.err.println("Details: " + response.body());
      }
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = response.body();

      Map<String, Object> map = mapper.readValue(jsonString, Map.class);
      // TODO: Save response
      return map;

    } catch (Exception e) {
      System.err.println("💥 Failed to connect: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }
}

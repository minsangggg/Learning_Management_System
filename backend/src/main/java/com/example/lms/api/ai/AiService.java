package com.example.lms.api.ai;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiService {
  private static final String MODEL_NAME = "gpt-4o-mini";
  private static final URI OPENAI_URI = URI.create("https://api.openai.com/v1/responses");

  private final AiRepository repository;
  private final ObjectMapper mapper;
  private final HttpClient httpClient;

  public AiService(AiRepository repository, ObjectMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  public AiResult generateSummary(long userId, String inputText) {
    String prompt = "Summarize the following text in 5 bullet points:\n\n" + inputText;
    return callOpenAi(userId, "summary", prompt);
  }

  public AiResult generateQuiz(long userId, String inputText) {
    String prompt =
        "You are a quiz generator. Use ONLY the provided text.\n"
            + "Return JSON only (no markdown, no extra text) with this schema:\n"
            + "{\n"
            + "  \"questions\": [\n"
            + "    {\n"
            + "      \"id\": 1,\n"
            + "      \"type\": \"mcq\" | \"short\",\n"
            + "      \"question\": \"...\",\n"
            + "      \"choices\": [\"A\", \"B\", \"C\", \"D\"],\n"
            + "      \"answerIndex\": 0,\n"
            + "      \"answerText\": \"...\",\n"
            + "      \"explanation\": \"...\"\n"
            + "    }\n"
            + "  ]\n"
            + "}\n"
            + "Rules:\n"
            + "- Exactly 15 questions total.\n"
            + "- At least 8 questions must be \"mcq\" with exactly 4 choices.\n"
            + "- Remaining questions must be \"short\".\n"
            + "- For mcq: include choices and answerIndex (0-3). answerText must be null.\n"
            + "- For short: include answerText (short phrase). choices and answerIndex must be null.\n"
            + "- Provide a brief explanation for every question.\n\n"
            + "Text:\n"
            + inputText;
    return callOpenAi(userId, "quiz", prompt);
  }

  private AiResult callOpenAi(long userId, String requestType, String prompt) {
    String apiKey = resolveApiKey();
    if (apiKey == null || apiKey.isBlank()) {
      throw new ApiException(ErrorCode.EXTERNAL_API_ERROR, "OPENAI_API_KEY is not set");
    }

    String payload =
        "{\"model\":\""
            + MODEL_NAME
            + "\",\"input\":"
            + mapper.valueToTree(prompt).toString()
            + "}";

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(OPENAI_URI)
            .timeout(Duration.ofSeconds(30))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();

    long start = System.nanoTime();
    String responseBody = "";
    String status = "ERROR";
    int statusCode = 500;
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      statusCode = response.statusCode();
      responseBody = response.body();
      status = statusCode >= 200 && statusCode < 300 ? "OK" : "ERROR";
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      responseBody = ex.getMessage();
      status = "ERROR";
    }
    long latencyMs = (System.nanoTime() - start) / 1_000_000;

    repository.insert(userId, MODEL_NAME, requestType + ":" + prompt, responseBody, latencyMs, status);

    if (!"OK".equals(status)) {
      throw new ApiException(ErrorCode.EXTERNAL_API_ERROR, "OpenAI request failed: " + statusCode);
    }

    String outputText = extractOutputText(responseBody);
    return new AiResult(outputText, status, latencyMs);
  }

  private String resolveApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey != null && !apiKey.isBlank()) {
      return apiKey;
    }
    return loadApiKeyFromFiles();
  }

  private String loadApiKeyFromFiles() {
    List<Path> candidates =
        List.of(
            Path.of(".env"),
            Path.of(".env.sample"),
            Path.of("..", ".env"),
            Path.of("..", ".env.sample"));
    for (Path path : candidates) {
      if (!Files.exists(path)) {
        continue;
      }
      try {
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
          String trimmed = line.trim();
          if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            continue;
          }
          int eq = trimmed.indexOf('=');
          if (eq <= 0) {
            continue;
          }
          String key = trimmed.substring(0, eq).trim();
          if (!"OPENAI_API_KEY".equals(key)) {
            continue;
          }
          String value = trimmed.substring(eq + 1).trim();
          if ((value.startsWith("\"") && value.endsWith("\""))
              || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1).trim();
          }
          if (!value.isBlank()) {
            return value;
          }
        }
      } catch (IOException ignored) {
        // Fall through to next candidate.
      }
    }
    return null;
  }

  private String extractOutputText(String responseBody) {
    try {
      JsonNode root = mapper.readTree(responseBody);
      StringBuilder sb = new StringBuilder();
      JsonNode output = root.path("output");
      if (output.isArray()) {
        for (JsonNode item : output) {
          JsonNode content = item.path("content");
          if (content.isArray()) {
            for (JsonNode part : content) {
              String text = part.path("text").asText();
              if (!text.isBlank()) {
                if (sb.length() > 0) {
                  sb.append("\n");
                }
                sb.append(text);
              }
            }
          }
        }
      }
      String combined = sb.toString();
      return combined.isBlank() ? responseBody : combined;
    } catch (Exception ex) {
      return responseBody;
    }
  }

  public record AiResult(String output, String status, long latencyMs) {}
}

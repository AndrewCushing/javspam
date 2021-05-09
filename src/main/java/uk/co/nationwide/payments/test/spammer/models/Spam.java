package uk.co.nationwide.payments.test.spammer.models;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class Spam {

  private final String method;

  private final String host;

  private final String path;

  private final long spamCount;

  private final int expectedStatusCode;

  private String contentType = "";

  private String payload = "";

  private long delayMillis = 1000;

  private Map<String, String> headers = new HashMap<>();

}

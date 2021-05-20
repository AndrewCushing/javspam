package uk.co.nationwide.payments.test.spammer.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import uk.co.nationwide.payments.test.spammer.exceptions.SpamException;
import uk.co.nationwide.payments.test.spammer.factories.ClientFactory;
import uk.co.nationwide.payments.test.spammer.models.Spam;

@Slf4j
@Service
public class SpamService implements Spammer {

  private final Random rand;
  private final Jackson2JsonEncoder jsonEncoder;
  private final ClientFactory clientFactory;

  @Autowired
  public SpamService(ClientFactory clientFactory) {
    Random tmpRand;
    this.clientFactory = clientFactory;
    this.jsonEncoder = new Jackson2JsonEncoder();
    try {
      tmpRand = SecureRandom.getInstanceStrong();
    } catch (NoSuchAlgorithmException e) {
      tmpRand = new Random();
    }
    rand = tmpRand;
  }

  public void startSpamming(Spam spam) throws IOException, SpamException {
    HttpRequest postRequest = createRequest(spam);

    testFirstCall(spam, postRequest);

    for (int i = 1; i < spam.getSpamCount(); i++) {
      CompletableFuture.runAsync(() -> {
        try {
          makeNormalCall(spam, postRequest);
        } catch (IOException e) {
          log.error(e.toString());
        }
      }, CompletableFuture.delayedExecutor(i * spam.getDelayMillis(), TimeUnit.MILLISECONDS));
    }
  }

  private void testFirstCall(Spam spam, HttpRequest request) throws IOException, SpamException {
    var response = clientFactory.getClient().execute(HttpHost.create(spam.getHost()), request);
    HttpEntity entity = response.getEntity();

    if (spam.getExpectedStatusCode() == -1 ||
        response.getStatusLine().getStatusCode() != spam.getExpectedStatusCode()) {
      throw new SpamException(String
          .format(
              "Response from first call had status code of %s %s, which was not the expected %s",
              response.getStatusLine().getStatusCode(),
              response.getStatusLine().getReasonPhrase(),
              spam.getExpectedStatusCode()));
    }

    if (entity != null) {
      try (InputStream entityStream = entity.getContent()) {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(entityStream));
        System.out.println("Content in response from test request: " +
            reader.lines().reduce("", (s, s2) -> s + s2));
      }
    }
  }

  private void makeNormalCall(Spam spam, HttpRequest request) throws IOException {
    HttpResponse response = clientFactory.getClient()
        .execute(HttpHost.create(spam.getHost()), request);

    HttpEntity entity = response.getEntity();

    if (entity != null) {

      try (InputStream entityStream = entity.getContent()) {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(entityStream));
        reader.lines();

      }
    }
  }

  private HttpRequest createRequest(Spam spam)
      throws SpamException, UnsupportedEncodingException, JsonProcessingException {
    BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(spam.getMethod(),
        spam.getHost() + spam.getPath());
    if (!spam.getStringPayload().equals("")) {
      if (spam.getContentType().equals("")) {
        throw new SpamException("If providing a payload, contentType cannot be null");
      }

      request.setHeader("Content-Type", spam.getContentType());
      request.setEntity(new StringEntity(getPayload(spam)));
    }

    for (var kvp : spam.getHeaders().entrySet()) {
      request.setHeader(kvp.getKey(), kvp.getValue());
    }
    return request;
  }

  private String getPayload(Spam spam) throws JsonProcessingException {
    String payload = "";
    ObjectMapper objectMapper = jsonEncoder.getObjectMapper();
    if (spam.getObjectPayload() == null) {
      payload = objectMapper.writeValueAsString(spam.getStringPayload());
    } else {
      payload = objectMapper.writeValueAsString(spam.getObjectPayload());
    }

    payload = payload.replace("{{ UUID }}", UUID.randomUUID().toString());
    payload = payload.replace("{{ BOOL }}", String.valueOf(rand.nextBoolean()));

    if (spam.isUrlEncodePayload()) {
      payload = URLEncoder.encode(payload, StandardCharsets.US_ASCII);
    }

    log.debug("Payload to send: {}", payload);

    return payload;
  }
}

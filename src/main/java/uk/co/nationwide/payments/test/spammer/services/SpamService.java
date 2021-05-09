package uk.co.nationwide.payments.test.spammer.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import uk.co.nationwide.payments.test.spammer.exceptions.SpamException;
import uk.co.nationwide.payments.test.spammer.models.Spam;

@Slf4j
@Service
public class SpamService {

  private final HttpClient client;
  private final Jackson2JsonEncoder jsonEncoder;

  @Autowired
  public SpamService(HttpClient client) {
    this.client = client;
    this.jsonEncoder = new Jackson2JsonEncoder();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> client.getConnectionManager().shutdown()));
  }

  public void startSpamming(Spam spam) throws IOException, URISyntaxException, SpamException {
    HttpRequest postRequest = createRequest(spam);

    testFirstCall(spam, postRequest);

    for (int i = 1 ; i < spam.getSpamCount() ; i++) {
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
    var response = client.execute(HttpHost.create(spam.getHost()), request);
    HttpEntity entity = response.getEntity();

    if (entity != null) {

      try (InputStream entityStream = entity.getContent()) {
        if (spam.getExpectedStatusCode() == -1 ||
            response.getStatusLine().getStatusCode() != spam.getExpectedStatusCode()) {
          throw new SpamException(String
              .format(
                  "Response from first call had status code of %s %s, which was not the expected %s",
                  response.getStatusLine().getStatusCode(),
                  response.getStatusLine().getReasonPhrase(),
                  spam.getExpectedStatusCode()));
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(entityStream));
        System.out.println("Content in response from test request: " +
            reader.lines().reduce("", (s, s2) -> s + s2));
      }
    }
  }

  private void makeNormalCall(Spam spam, HttpRequest request) throws IOException {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpResponse response = client.execute(HttpHost.create(spam.getHost()), request);

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
    if (!spam.getPayload().equals("")) {
      if (spam.getContentType().equals("")) {
        throw new SpamException("If providing a payload, contentType cannot be null");
      }

      request.setHeader("Content-Type", spam.getContentType());
      request.setEntity(
          new StringEntity(jsonEncoder.getObjectMapper().writeValueAsString(spam.getPayload())));
    }

    for (var kvp : spam.getHeaders().entrySet()) {
      request.setHeader(kvp.getKey(), kvp.getValue());
    }
    return request;
  }


}
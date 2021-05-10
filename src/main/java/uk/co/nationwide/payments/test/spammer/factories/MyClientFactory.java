package uk.co.nationwide.payments.test.spammer.factories;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Service;

@Service
public class MyClientFactory implements ClientFactory {

  @Override
  public HttpClient getClient() {
    DefaultHttpClient client = new DefaultHttpClient();
    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> client.getConnectionManager().shutdown()));
    return client;
  }
}

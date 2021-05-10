package uk.co.nationwide.payments.test.spammer.fakes;

import org.apache.http.client.HttpClient;
import uk.co.nationwide.payments.test.spammer.factories.ClientFactory;

public class FakeClientFactory implements ClientFactory {

  private final HttpClient client;

  public FakeClientFactory(HttpClient client) {
    this.client = client;
  }

  @Override
  public HttpClient getClient() {
    return client;
  }
}

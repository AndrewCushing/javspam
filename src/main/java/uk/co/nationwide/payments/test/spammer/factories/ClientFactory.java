package uk.co.nationwide.payments.test.spammer.factories;

import org.apache.http.client.HttpClient;

public interface ClientFactory {

  HttpClient getClient();

}

package uk.co.nationwide.payments.test.spammer.services;

import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.nationwide.payments.test.spammer.exceptions.SpamException;
import uk.co.nationwide.payments.test.spammer.fakes.FakeClientFactory;
import uk.co.nationwide.payments.test.spammer.models.Spam;

class SpamServiceTests {

  private AutoCloseable closeable;

  @Mock
  private HttpClient httpClient;

  @Mock
  private ClientConnectionManager connectionManager;

  @Test
  void testCallFails_wrongStatusCode_exceptionReThrown() throws IOException {
    Spam spam = new Spam("GET", "http://localhost:8080", "", 50, 123);
    SpamService service = new SpamService(new FakeClientFactory(httpClient));
    BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 51, "Blah"));
    when(httpClient.execute(any(HttpHost.class), any(HttpRequest.class))).thenReturn(response);
    when(httpClient.getConnectionManager()).thenReturn(connectionManager);

    assertThrows(SpamException.class, () -> service.startSpamming(spam));
    verify(httpClient, times(1)).execute(any(HttpHost.class), any(HttpRequest.class));
  }

  @Test
  void testCallSucceeds_makesAllCalls()
      throws IOException, InterruptedException, ExecutionException {
    Spam spam = new Spam("GET", "http://localhost:8080", "", 50, 123);
    spam.setDelayMillis(1);
    SpamService service = new SpamService(new FakeClientFactory(httpClient));
    BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 123, "Blah"));
    when(httpClient.execute(any(HttpHost.class), any(HttpRequest.class))).thenReturn(response);
    when(httpClient.getConnectionManager()).thenReturn(connectionManager);

    assertDoesNotThrow(() -> service.startSpamming(spam));
    CompletableFuture<Void> future = runAsync(() -> {
      try {
        verify(httpClient, times(50)).execute(any(HttpHost.class), any(HttpRequest.class));
      } catch (IOException e) {
        fail();
      }
    }, delayedExecutor(50, TimeUnit.MILLISECONDS));
    future.get();
  }

  @BeforeEach
  public void openMocks() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void releaseMocks() throws Exception {
    closeable.close();
  }

}

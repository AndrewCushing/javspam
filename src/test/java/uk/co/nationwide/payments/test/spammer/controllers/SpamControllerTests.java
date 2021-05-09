package uk.co.nationwide.payments.test.spammer.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.co.nationwide.payments.test.spammer.fakes.FakeSpammer;
import uk.co.nationwide.payments.test.spammer.models.Spam;

public class SpamControllerTests {

  @Mock
  private HttpServletResponse response;

  private AutoCloseable closeable;

  @Test
  public void spamException() {
    FakeSpammer fakeSpammer = new FakeSpammer();
    fakeSpammer.throwSpam = true;
    SpamController controller = new SpamController(fakeSpammer);
    Spam spam = new Spam("GET", "bob", "", 1, 23);

    Map<String, String> result = controller.spam(response, spam);

    assertThat(result).containsKey("Exception");
    verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
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

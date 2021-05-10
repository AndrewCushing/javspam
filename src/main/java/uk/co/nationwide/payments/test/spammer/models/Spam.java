package uk.co.nationwide.payments.test.spammer.models;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class Spam {

  /**
   * Http method to use, such as GET, POST etc
   */
  private final String method;

  /**
   * Address to make the call to, including the scheme but not the path. For example, in the URL
   * http://localhost:8080/this/that?hello=world&you=bored the host would be http://localhost:8080
   */
  private final String host;

  /**
   * The bit to add to the host for the full URL. For example, in the URL
   * http://localhost:8080/this/that?hello=world&you=bored the path would be /this/that?hello=world&you=bored
   */
  private final String path;

  /**
   * How many times to make the call in total (1 test call will always be made)
   */
  private final long spamCount;

  /**
   * Http Status code which is expected. This is only verified in the first test call, then the
   * status code isn't inspected
   */
  private final int expectedStatusCode;

  /**
   * Content type of the body, if there is one, such as application/json
   */
  private String contentType = "";

  /**
   * Body of the Http request
   */
  private String payload = "";

  /**
   * How long to wait between sending each message
   */
  private long delayMillis = 1000;

  /**
   * Any additional headers to add
   */
  private Map<String, String> headers = new HashMap<>();

}

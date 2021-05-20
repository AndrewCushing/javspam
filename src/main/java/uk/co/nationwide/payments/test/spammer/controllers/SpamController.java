package uk.co.nationwide.payments.test.spammer.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nationwide.payments.test.spammer.exceptions.SpamException;
import uk.co.nationwide.payments.test.spammer.models.Spam;
import uk.co.nationwide.payments.test.spammer.services.Spammer;

@Slf4j
@RestController
public class SpamController {

  private final Spammer spammer;
  private static final Jackson2JsonEncoder encoder = new Jackson2JsonEncoder();

  @Autowired
  public SpamController(Spammer spammer) {
    this.spammer = spammer;
  }

  @PostMapping("/spam")
  public Map<String, String> spam(HttpServletResponse response, @RequestBody Spam spam) {
    HashMap<String, String> hashMap = new HashMap<>();
    String exception = "Exception";
    try {
      log.info(encoder.getObjectMapper().writeValueAsString(spam));
      spammer.startSpamming(spam);
    } catch (IOException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      hashMap.put("Reason", "Error when calling endpoint");
      hashMap.put(exception, e.toString());
      return hashMap;
    } catch (IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      hashMap.put("Reason", "Could not parse Url");
      hashMap.put(exception, e.toString());
      return hashMap;
    } catch (SpamException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      hashMap.put(exception, e.toString());
      return hashMap;
    }
    response.setStatus(200);
    return Map.of();
  }

}

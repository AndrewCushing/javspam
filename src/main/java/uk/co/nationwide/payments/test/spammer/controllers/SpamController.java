package uk.co.nationwide.payments.test.spammer.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nationwide.payments.test.spammer.exceptions.SpamException;
import uk.co.nationwide.payments.test.spammer.models.Spam;
import uk.co.nationwide.payments.test.spammer.services.SpamService;

@RestController
public class SpamController {

  private final SpamService spamService;

  @Autowired
  public SpamController(SpamService spamService) {
    this.spamService = spamService;
  }

  @PostMapping("/spam")
  public Map<String, String> spam(HttpServletResponse response, @RequestBody Spam spam) {
    try {
      spamService.startSpamming(spam);
    } catch (IOException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      HashMap<String, String> stringStringHashMap = new HashMap<>();
      stringStringHashMap.put("Reason", "Error when calling endpoint");
      stringStringHashMap.put("Exception message", e.getMessage());
      return stringStringHashMap;
    } catch (URISyntaxException | IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      HashMap<String, String> stringStringHashMap = new HashMap<>();
      stringStringHashMap.put("Reason", "Could not parse Url");
      stringStringHashMap.put("Exception message", e.getMessage());
      return stringStringHashMap;
    } catch (SpamException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      HashMap<String, String> stringStringHashMap = new HashMap<>();
      stringStringHashMap.put("Exception", e.toString());
      return stringStringHashMap;
    }
    response.setStatus(200);
    return Map.of();
  }

}

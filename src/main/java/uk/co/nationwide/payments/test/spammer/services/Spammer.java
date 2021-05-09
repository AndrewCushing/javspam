package uk.co.nationwide.payments.test.spammer.services;

import java.io.IOException;
import uk.co.nationwide.payments.test.spammer.exceptions.SpamException;
import uk.co.nationwide.payments.test.spammer.models.Spam;

public interface Spammer {

  void startSpamming(Spam spam) throws IOException, SpamException;

}

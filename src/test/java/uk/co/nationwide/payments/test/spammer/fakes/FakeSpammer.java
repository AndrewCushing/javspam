package uk.co.nationwide.payments.test.spammer.fakes;

import java.io.IOException;
import uk.co.nationwide.payments.test.spammer.exceptions.SpamException;
import uk.co.nationwide.payments.test.spammer.models.Spam;
import uk.co.nationwide.payments.test.spammer.services.Spammer;

public class FakeSpammer implements Spammer {

  public boolean throwIO = false;
  public boolean throwUriSyntax = false;
  public boolean throwSpam = false;

  @Override
  public void startSpamming(Spam spam) throws IOException, SpamException {
    if (throwIO)
      throw new IOException();

    if (throwSpam)
      throw new SpamException("spam");
  }
}

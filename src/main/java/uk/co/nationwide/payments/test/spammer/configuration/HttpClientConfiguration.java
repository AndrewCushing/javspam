package uk.co.nationwide.payments.test.spammer.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class HttpClientConfiguration {

  @Bean
  public HttpClient httpClient() {
    return new DefaultHttpClient();
  }

}

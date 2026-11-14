package steamLookUpSpecKit.steam;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SteamApiConfiguration {

	public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
	public static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(15);

	@Bean
	RestClient steamRestClient(RestClient.Builder builder) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(CONNECT_TIMEOUT)
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(RESPONSE_TIMEOUT);
		return builder.requestFactory(requestFactory).build();
	}

	@Bean
	Clock steamLookupClock() {
		return Clock.systemUTC();
	}
}

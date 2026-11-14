package steamLookUpSpecKit.steam;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SteamApiClientConfigurationTest {

	@Test
	void usesTheSpecifiedConnectionAndResponseTimeouts() {
		assertThat(SteamApiConfiguration.CONNECT_TIMEOUT).isEqualTo(Duration.ofSeconds(5));
		assertThat(SteamApiConfiguration.RESPONSE_TIMEOUT).isEqualTo(Duration.ofSeconds(15));
	}
}

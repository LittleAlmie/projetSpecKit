package steamLookUpSpecKit.steam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class SteamApiProperties {

	private final String apiKey;

	public SteamApiProperties(@Value("${STEAM_API_KEY:}") String apiKey) {
		this.apiKey = apiKey;
	}

	public String apiKey() {
		return apiKey;
	}

	public boolean isConfigured() {
		return apiKey != null && !apiKey.isBlank();
	}
}

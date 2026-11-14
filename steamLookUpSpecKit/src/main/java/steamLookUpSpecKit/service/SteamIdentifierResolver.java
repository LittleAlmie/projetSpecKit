package steamLookUpSpecKit.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import steamLookUpSpecKit.steam.SteamApiClient;

@Component
public final class SteamIdentifierResolver {

	private static final Pattern STEAM_ID64 = Pattern.compile("\\d{17}");
	private static final Pattern VANITY_NAME = Pattern.compile("[A-Za-z0-9_-]+");

	private final SteamApiClient steamApiClient;

	public SteamIdentifierResolver(SteamApiClient steamApiClient) {
		this.steamApiClient = steamApiClient;
	}

	public String resolve(String input) {
		String value = input == null ? "" : input.trim();
		if (STEAM_ID64.matcher(value).matches()) {
			return value;
		}
		String vanityName = extractVanityName(value);
		if (!VANITY_NAME.matcher(vanityName).matches()) {
			throw new IllegalArgumentException("Identifiant Steam invalide. Fournissez un SteamID64, un nom personnalisé ou une URL de profil.");
		}
		return steamApiClient.resolveVanityUrl(vanityName);
	}

	private static String extractVanityName(String value) {
		if (!value.contains("://")) {
			return value;
		}
		try {
			URI uri = new URI(value);
			if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
					|| !("steamcommunity.com".equalsIgnoreCase(uri.getHost())
					|| "www.steamcommunity.com".equalsIgnoreCase(uri.getHost()))) {
				throw new IllegalArgumentException("L’URL doit appartenir à steamcommunity.com.");
			}
			String[] segments = uri.getPath().split("/");
			if (segments.length != 3 || !"id".equals(segments[1]) || segments[2].isBlank()) {
				throw new IllegalArgumentException("L’URL doit utiliser le chemin steamcommunity.com/id/<nom>.");
			}
			return segments[2];
		}
		catch (URISyntaxException exception) {
			throw new IllegalArgumentException("L’URL du profil Steam est invalide.");
		}
	}
}

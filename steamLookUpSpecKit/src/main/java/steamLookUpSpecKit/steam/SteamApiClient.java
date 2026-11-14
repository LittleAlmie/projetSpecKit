package steamLookUpSpecKit.steam;

import java.net.URI;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import steamLookUpSpecKit.model.OwnedGame;
import steamLookUpSpecKit.model.RecentGame;
import steamLookUpSpecKit.model.SteamProfile;

@Component
public final class SteamApiClient {

	private static final String API_BASE_URL = "https://api.steampowered.com";

	private final RestClient restClient;
	private final SteamApiProperties properties;

	public SteamApiClient(RestClient steamRestClient, SteamApiProperties properties) {
		this.restClient = steamRestClient;
		this.properties = properties;
	}

	public String resolveVanityUrl(String vanityName) {
		JsonNode response = get("/ISteamUser/ResolveVanityURL/v1/",
				UriComponentsBuilder.newInstance().queryParam("vanityurl", vanityName));
		JsonNode result = response.path("success");
		JsonNode steamId = response.path("steamid");
		if (!result.isNumber() || result.intValue() != 1 || !steamId.isTextual()) {
			throw new SteamApiException("Steam n’a pas trouvé de compte correspondant à cet identifiant.");
		}
		return steamId.textValue();
	}

	public SteamProfile getPlayerSummary(String steamId64) {
		JsonNode response = get("/ISteamUser/GetPlayerSummaries/v2/",
				UriComponentsBuilder.newInstance().queryParam("steamids", steamId64));
		JsonNode players = response.path("players");
		if (!players.isArray() || players.isEmpty()) {
			throw new SteamApiException("Steam n’a pas renvoyé de profil pour cet identifiant.");
		}
		JsonNode player = players.get(0);
		String displayName = optionalText(player, "personaname");
		String profileUrl = optionalText(player, "profileurl");
		int visibilityValue = player.path("communityvisibilitystate").asInt(-1);
		SteamProfile.Visibility visibility = switch (visibilityValue) {
			case 1 -> SteamProfile.Visibility.PRIVATE;
			case 3 -> SteamProfile.Visibility.PUBLIC;
			default -> SteamProfile.Visibility.UNKNOWN;
		};
		return new SteamProfile(displayName, profileUrl, visibility);
	}

	public Optional<List<OwnedGame>> getOwnedGames(String steamId64) {
		JsonNode response = get("/IPlayerService/GetOwnedGames/v1/",
				UriComponentsBuilder.newInstance()
						.queryParam("steamid", steamId64)
						.queryParam("include_appinfo", true)
						.queryParam("include_played_free_games", false));
		JsonNode games = response.get("games");
		if (games == null || !games.isArray()) {
			return Optional.empty();
		}
		List<OwnedGame> result = new ArrayList<>();
		for (JsonNode game : games) {
			long appId = requiredNonNegativeLong(game, "appid");
			long playtime = requiredNonNegativeLong(game, "playtime_forever");
			Instant lastPlayed = optionalEpoch(game, "rtime_last_played");
			result.add(new OwnedGame(appId, optionalText(game, "name"), playtime, lastPlayed));
		}
		return Optional.of(List.copyOf(result));
	}

	public List<RecentGame> getRecentlyPlayedGames(String steamId64) {
		JsonNode response = get("/IPlayerService/GetRecentlyPlayedGames/v1/",
				UriComponentsBuilder.newInstance()
						.queryParam("steamid", steamId64)
						.queryParam("count", 0));
		JsonNode games = response.get("games");
		if (games == null) {
			if (response.path("total_count").asInt(-1) == 0) {
				return List.of();
			}
			throw new SteamApiException("Steam a renvoyé une activité récente incomplète.");
		}
		if (!games.isArray()) {
			throw new SteamApiException("Steam a renvoyé une activité récente inutilisable.");
		}
		List<RecentGame> result = new ArrayList<>();
		for (JsonNode game : games) {
			long appId = requiredNonNegativeLong(game, "appid");
			long playtime = requiredNonNegativeLong(game, "playtime_2weeks");
			Instant lastPlayed = optionalEpoch(game, "rtime_last_played");
			result.add(new RecentGame(appId, optionalText(game, "name"), playtime, lastPlayed));
		}
		return List.copyOf(result);
	}

	private JsonNode get(String path, UriComponentsBuilder parameters) {
		if (!properties.isConfigured()) {
			throw new SteamApiException("La clé Steam Web API n’est pas configurée.");
		}
		URI uri = UriComponentsBuilder.fromUriString(API_BASE_URL)
				.path(path)
				.queryParam("key", properties.apiKey())
				.queryParams(parameters.build().getQueryParams())
				.build()
				.encode()
				.toUri();
		try {
			JsonNode root = restClient.get().uri(uri).retrieve().body(JsonNode.class);
			if (root == null || !root.path("response").isObject()) {
				throw new SteamApiException("Steam a renvoyé une réponse vide ou invalide.");
			}
			return root.path("response");
		}
		catch (SteamApiException exception) {
			throw exception;
		}
		catch (RestClientResponseException exception) {
			HttpStatusCode status = exception.getStatusCode();
			if (status.value() == 401 || status.value() == 403) {
				throw new SteamApiException("Steam a refusé la clé API ou l’accès à cette requête.");
			}
			throw new SteamApiException("Steam a renvoyé une erreur HTTP (" + status.value() + ").");
		}
		catch (RestClientException exception) {
			throw new SteamApiException("La requête Steam a échoué (réseau ou délai dépassé).", exception);
		}
	}

	private static long requiredNonNegativeLong(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || !value.canConvertToLong() || value.longValue() < 0) {
			throw new SteamApiException("Steam a renvoyé une réponse de jeu incomplète ou invalide.");
		}
		return value.longValue();
	}

	private static String optionalText(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return value != null && value.isTextual() && !value.textValue().isBlank() ? value.textValue() : null;
	}

	private static Instant optionalEpoch(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || !value.canConvertToLong() || value.longValue() <= 0) {
			return null;
		}
		try {
			return Instant.ofEpochSecond(value.longValue());
		}
		catch (DateTimeException | ArithmeticException exception) {
			throw new SteamApiException("Steam a renvoyé une date de dernière partie invalide.", exception);
		}
	}
}

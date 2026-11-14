package steamLookUpSpecKit.steam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import steamLookUpSpecKit.model.OwnedGame;
import steamLookUpSpecKit.model.RecentGame;

class SteamApiClientTest {

	private static final String STEAM_ID = "76561198000000000";

	private MockRestServiceServer server;
	private SteamApiClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new SteamApiClient(builder.build(), new SteamApiProperties("test-key"));
	}

	@Test
	void resolvesIdentifierAndReadsProfileLibraryAndRecentGames() {
		server.expect(requestTo(org.hamcrest.Matchers.containsString("/ISteamUser/ResolveVanityURL/v1/")))
				.andExpect(method(HttpMethod.GET))
				.andExpect(queryParam("key", "test-key"))
				.andExpect(queryParam("vanityurl", "alice"))
				.andRespond(withSuccess("""
						{"response":{"success":1,"steamid":"%s"}}
						""".formatted(STEAM_ID), MediaType.APPLICATION_JSON));
		server.expect(requestTo(org.hamcrest.Matchers.containsString("/ISteamUser/GetPlayerSummaries/v2/")))
				.andExpect(method(HttpMethod.GET))
				.andExpect(queryParam("steamids", STEAM_ID))
				.andRespond(withSuccess("""
						{"response":{"players":[{"steamid":"%s","personaname":"Alice","profileurl":"https://steamcommunity.com/profiles/%s/","communityvisibilitystate":3}]}}
						""".formatted(STEAM_ID, STEAM_ID), MediaType.APPLICATION_JSON));
		server.expect(requestTo(org.hamcrest.Matchers.containsString("/IPlayerService/GetOwnedGames/v1/")))
				.andExpect(method(HttpMethod.GET))
				.andExpect(queryParam("steamid", STEAM_ID))
				.andExpect(queryParam("include_played_free_games", "false"))
				.andRespond(withSuccess("""
						{"response":{"game_count":1,"games":[{"appid":10,"name":"Game","playtime_forever":120,"rtime_last_played":1763164800}]}}
						""", MediaType.APPLICATION_JSON));
		server.expect(requestTo(org.hamcrest.Matchers.containsString("/IPlayerService/GetRecentlyPlayedGames/v1/")))
				.andExpect(method(HttpMethod.GET))
				.andExpect(queryParam("steamid", STEAM_ID))
				.andExpect(queryParam("count", "0"))
				.andRespond(withSuccess("""
						{"response":{"total_count":1,"games":[{"appid":10,"name":"Game","playtime_2weeks":60,"rtime_last_played":1763164800}]}}
						""", MediaType.APPLICATION_JSON));

		assertThat(client.resolveVanityUrl("alice")).isEqualTo(STEAM_ID);
		assertThat(client.getPlayerSummary(STEAM_ID).displayName()).isEqualTo("Alice");
		assertThat(client.getOwnedGames(STEAM_ID).orElseThrow())
				.extracting(OwnedGame::name)
				.containsExactly("Game");
		List<RecentGame> recentGames = client.getRecentlyPlayedGames(STEAM_ID);
		assertThat(recentGames).hasSize(1);
		assertThat(recentGames.getFirst().playtimeLast14DaysMinutes()).isEqualTo(60);

		server.verify();
	}

	@Test
	void returnsAnEmptyRecentListWhenSteamReportsNoRecentGames() {
		server.expect(requestTo(org.hamcrest.Matchers.containsString("/IPlayerService/GetRecentlyPlayedGames/v1/")))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("""
						{"response":{"total_count":0}}
						""", MediaType.APPLICATION_JSON));

		assertThat(client.getRecentlyPlayedGames(STEAM_ID)).isEmpty();
		server.verify();
	}

	@Test
	void neverIncludesTheApiKeyInErrors() {
		server.expect(requestTo(org.hamcrest.Matchers.containsString("key=test-key")))
				.andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
						.withStatus(HttpStatus.FORBIDDEN));

		assertThatThrownBy(() -> client.getPlayerSummary(STEAM_ID))
				.isInstanceOf(SteamApiException.class)
				.hasMessageNotContaining("test-key");
		server.verify();
	}
}

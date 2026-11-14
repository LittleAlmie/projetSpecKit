package steamLookUpSpecKit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import steamLookUpSpecKit.model.GameLibrary;
import steamLookUpSpecKit.model.LookupResult;
import steamLookUpSpecKit.model.LookupSection;
import steamLookUpSpecKit.model.OwnedGame;
import steamLookUpSpecKit.model.RecentGame;
import steamLookUpSpecKit.model.SteamProfile;
import steamLookUpSpecKit.steam.SteamApiClient;

class SteamLookupServiceTest {

	private static final String STEAM_ID = "76561198000000000";
	private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");

	@Test
	void buildsLibraryStatisticsAndRanksRecentGames() {
		SteamIdentifierResolver resolver = Mockito.mock(SteamIdentifierResolver.class);
		SteamApiClient client = Mockito.mock(SteamApiClient.class);
		when(resolver.resolve("alice")).thenReturn(STEAM_ID);
		when(client.getPlayerSummary(STEAM_ID)).thenReturn(
				new SteamProfile("Alice", "https://steamcommunity.com/profiles/" + STEAM_ID + "/", SteamProfile.Visibility.PUBLIC));

		List<OwnedGame> ownedGames = new ArrayList<>();
		for (int index = 1; index <= 12; index++) {
			ownedGames.add(new OwnedGame(index, "Game " + index, index * 60L,
					Instant.parse("2026-11-10T00:00:00Z")));
		}
		ownedGames.add(new OwnedGame(20, "Never played", 0, null));
		when(client.getOwnedGames(STEAM_ID)).thenReturn(Optional.of(ownedGames));
		when(client.getRecentlyPlayedGames(STEAM_ID)).thenReturn(List.of(
				new RecentGame(10, "Newest", 30, NOW.minusSeconds(3600)),
				new RecentGame(11, "Older", 60, NOW.minusSeconds(7200)),
				new RecentGame(12, "Unknown date", 15, null),
				new RecentGame(13, "Outside window", 90, NOW.minusSeconds(15L * 24 * 60 * 60))));

		SteamLookupService service = new SteamLookupService(resolver, client,
				Clock.fixed(NOW, ZoneOffset.UTC));
		LookupResult result = service.lookup("alice");

		assertThat(result.profile().status()).isEqualTo(LookupSection.Status.AVAILABLE);
		GameLibrary library = result.library().data();
		assertThat(library.ownedGameCount()).isEqualTo(13);
		assertThat(library.playedGameCount()).isEqualTo(12);
		assertThat(library.totalPlaytimeMinutes()).isEqualTo(78L * 60);
		assertThat(library.topPlayedGames()).hasSize(10);
		assertThat(library.topPlayedGames().getFirst().name()).isEqualTo("Game 12");
		assertThat(result.recentActivity().data())
				.extracting(RecentGame::name)
				.containsExactly("Newest", "Older", "Unknown date");
	}

	@Test
	void capsRecentActivityAtFiveGames() {
		SteamIdentifierResolver resolver = Mockito.mock(SteamIdentifierResolver.class);
		SteamApiClient client = Mockito.mock(SteamApiClient.class);
		when(resolver.resolve("alice")).thenReturn(STEAM_ID);
		when(client.getPlayerSummary(STEAM_ID)).thenReturn(
				new SteamProfile("Alice", "https://example.test/alice", SteamProfile.Visibility.PUBLIC));
		when(client.getOwnedGames(STEAM_ID)).thenReturn(Optional.of(List.of()));
		when(client.getRecentlyPlayedGames(STEAM_ID)).thenReturn(List.of(
				new RecentGame(1, "One", 1, NOW.minusSeconds(1)),
				new RecentGame(2, "Two", 1, NOW.minusSeconds(2)),
				new RecentGame(3, "Three", 1, NOW.minusSeconds(3)),
				new RecentGame(4, "Four", 1, NOW.minusSeconds(4)),
				new RecentGame(5, "Five", 1, NOW.minusSeconds(5)),
				new RecentGame(6, "Six", 1, NOW.minusSeconds(6))));

		LookupResult result = new SteamLookupService(resolver, client,
				Clock.fixed(NOW, ZoneOffset.UTC)).lookup("alice");

		assertThat(result.recentActivity().data()).hasSize(5);
		verify(client).getRecentlyPlayedGames(STEAM_ID);
	}
}

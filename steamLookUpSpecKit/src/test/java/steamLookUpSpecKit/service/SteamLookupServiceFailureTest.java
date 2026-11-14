package steamLookUpSpecKit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import steamLookUpSpecKit.model.LookupResult;
import steamLookUpSpecKit.model.LookupSection;
import steamLookUpSpecKit.model.OwnedGame;
import steamLookUpSpecKit.model.RecentGame;
import steamLookUpSpecKit.model.SteamProfile;
import steamLookUpSpecKit.steam.SteamApiClient;
import steamLookUpSpecKit.steam.SteamApiException;

class SteamLookupServiceFailureTest {

	private static final String STEAM_ID = "76561198000000000";
	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneOffset.UTC);

	@Test
	void reportsPrivateProfileAndHiddenLibraryWithoutTreatingThemAsFailures() {
		SteamIdentifierResolver resolver = Mockito.mock(SteamIdentifierResolver.class);
		SteamApiClient client = Mockito.mock(SteamApiClient.class);
		when(resolver.resolve("alice")).thenReturn(STEAM_ID);
		when(client.getPlayerSummary(STEAM_ID)).thenReturn(
				new SteamProfile("Alice", "https://example.test/alice", SteamProfile.Visibility.PRIVATE));
		when(client.getOwnedGames(STEAM_ID)).thenReturn(Optional.empty());
		when(client.getRecentlyPlayedGames(STEAM_ID)).thenReturn(List.of());

		LookupResult result = new SteamLookupService(resolver, client, CLOCK).lookup("alice");

		assertThat(result.profile().status()).isEqualTo(LookupSection.Status.PRIVATE);
		assertThat(result.library().status()).isEqualTo(LookupSection.Status.UNAVAILABLE_OR_HIDDEN);
		assertThat(result.recentActivity().status()).isEqualTo(LookupSection.Status.AVAILABLE);
		assertThat(result.hasFailedSections()).isFalse();
	}

	@Test
	void preservesAvailableSectionsWhenAnotherRequestFails() {
		SteamIdentifierResolver resolver = Mockito.mock(SteamIdentifierResolver.class);
		SteamApiClient client = Mockito.mock(SteamApiClient.class);
		when(resolver.resolve("alice")).thenReturn(STEAM_ID);
		when(client.getPlayerSummary(STEAM_ID)).thenThrow(new SteamApiException("Profil indisponible."));
		when(client.getOwnedGames(STEAM_ID)).thenReturn(Optional.of(
				List.of(new OwnedGame(10, "Game", 60, null))));
		when(client.getRecentlyPlayedGames(STEAM_ID)).thenReturn(List.of(
				new RecentGame(10, "Game", 30, CLOCK.instant())));

		LookupResult result = new SteamLookupService(resolver, client, CLOCK).lookup("alice");

		assertThat(result.profile().status()).isEqualTo(LookupSection.Status.FAILED);
		assertThat(result.library().status()).isEqualTo(LookupSection.Status.AVAILABLE);
		assertThat(result.recentActivity().status()).isEqualTo(LookupSection.Status.AVAILABLE);
		assertThat(result.hasFailedSections()).isTrue();
	}
}

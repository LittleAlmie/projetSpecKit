package steamLookUpSpecKit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import steamLookUpSpecKit.steam.SteamApiClient;

class SteamIdentifierResolverTest {

	private static final String STEAM_ID = "76561198000000000";

	@Test
	void acceptsSteamId64WithoutCallingTheApi() {
		SteamApiClient client = Mockito.mock(SteamApiClient.class);

		assertThat(new SteamIdentifierResolver(client).resolve(STEAM_ID)).isEqualTo(STEAM_ID);

		verify(client, Mockito.never()).resolveVanityUrl(Mockito.anyString());
	}

	@Test
	void resolvesVanityNameAndCustomProfileUrl() {
		SteamApiClient client = Mockito.mock(SteamApiClient.class);
		when(client.resolveVanityUrl("alice")).thenReturn(STEAM_ID);
		SteamIdentifierResolver resolver = new SteamIdentifierResolver(client);

		assertThat(resolver.resolve("alice")).isEqualTo(STEAM_ID);
		assertThat(resolver.resolve("https://steamcommunity.com/id/alice/")).isEqualTo(STEAM_ID);
		verify(client, Mockito.times(2)).resolveVanityUrl("alice");
	}

	@Test
	void rejectsUrlsOutsideSteamCommunity() {
		SteamIdentifierResolver resolver = new SteamIdentifierResolver(Mockito.mock(SteamApiClient.class));

		assertThatThrownBy(() -> resolver.resolve("https://example.test/id/alice"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}

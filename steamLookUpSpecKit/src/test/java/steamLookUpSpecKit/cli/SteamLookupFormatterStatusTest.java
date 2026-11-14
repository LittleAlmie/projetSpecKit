package steamLookUpSpecKit.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import steamLookUpSpecKit.model.LookupResult;
import steamLookUpSpecKit.model.LookupSection;
import steamLookUpSpecKit.model.RecentGame;

class SteamLookupFormatterStatusTest {

	@Test
	void printsPrivateHiddenAndFailedStatusesWithoutLeakingSecrets() {
		String output = new SteamLookupFormatter().format(new LookupResult(
				LookupSection.privateSection(null, "Le profil Steam est privé."),
				LookupSection.unavailableOrHidden("Les données sont absentes ou masquées."),
				LookupSection.failed("Steam n’a pas répondu.")));

		assertThat(output)
				.contains("Données privées.")
				.contains("Données absentes ou masquées.")
				.contains("Échec de consultation.")
				.doesNotContain("test-key");
	}

	@Test
	void putsRecentGamesWithoutDatesAfterDatedGames() {
		String output = new SteamLookupFormatter().format(new LookupResult(
				LookupSection.available(null),
				LookupSection.available(null),
				LookupSection.available(List.of(
						new RecentGame(1, "Dated", 30, Instant.parse("2026-11-15T11:00:00Z")),
						new RecentGame(2, "No date", 20, null)))));

		assertThat(output).contains("date indisponible");
		assertThat(output.indexOf("Dated")).isLessThan(output.indexOf("No date"));
	}
}

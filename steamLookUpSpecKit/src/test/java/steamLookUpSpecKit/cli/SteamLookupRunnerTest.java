package steamLookUpSpecKit.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;
import steamLookUpSpecKit.model.GameLibrary;
import steamLookUpSpecKit.model.LookupResult;
import steamLookUpSpecKit.model.LookupSection;
import steamLookUpSpecKit.model.OwnedGame;
import steamLookUpSpecKit.model.SteamProfile;
import steamLookUpSpecKit.service.SteamLookupService;
import steamLookUpSpecKit.steam.SteamApiProperties;

class SteamLookupRunnerTest {

	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;

	@AfterEach
	void restoreOutput() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	void printsAllThreeSectionsForASuccessfulLookup() throws Exception {
		SteamLookupService service = Mockito.mock(SteamLookupService.class);
		SteamLookupFormatter formatter = new SteamLookupFormatter();
		LookupExitCode exitCode = new LookupExitCode();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output));
		when(service.lookup("alice")).thenReturn(new LookupResult(
				LookupSection.available(new SteamProfile("Alice", "https://example.test/alice", SteamProfile.Visibility.PUBLIC)),
				LookupSection.available(new GameLibrary(1, 1, 60,
						List.of(new OwnedGame(10, "Game", 60, null)))),
				LookupSection.available(List.of())));

		SteamLookupRunner runner = new SteamLookupRunner(service, formatter,
				new SteamApiProperties("test-key"), exitCode);
		runner.run(new DefaultApplicationArguments("alice"));

		assertThat(output.toString()).contains("Profil", "Bibliothèque", "Activité récente", "Alice");
		assertThat(exitCode.getExitCode()).isZero();
	}

	@Test
	void printsAvailableSectionsAndSetsNonZeroExitCodeForPartialFailure() throws Exception {
		SteamLookupService service = Mockito.mock(SteamLookupService.class);
		LookupExitCode exitCode = new LookupExitCode();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteArrayOutputStream errors = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output));
		System.setErr(new PrintStream(errors));
		when(service.lookup("alice")).thenReturn(new LookupResult(
				LookupSection.failed("Profil non disponible."),
				LookupSection.available(new GameLibrary(0, 0, 0, List.of())),
				LookupSection.available(List.of())));

		SteamLookupRunner runner = new SteamLookupRunner(service, new SteamLookupFormatter(),
				new SteamApiProperties("test-key"), exitCode);
		runner.run(new DefaultApplicationArguments("alice"));

		assertThat(output.toString()).contains("Bibliothèque", "Activité récente", "Échec de consultation.");
		assertThat(errors.toString()).contains("une ou plusieurs sections");
		assertThat(exitCode.getExitCode()).isEqualTo(1);
	}
}

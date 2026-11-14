package steamLookUpSpecKit.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;
import steamLookUpSpecKit.service.SteamLookupService;
import steamLookUpSpecKit.steam.SteamApiProperties;

class SteamLookupRunnerValidationTest {

	private final PrintStream originalErr = System.err;

	@AfterEach
	void restoreErrorOutput() {
		System.setErr(originalErr);
	}

	@Test
	void rejectsMissingOrExtraArgumentsBeforeCallingSteam() throws Exception {
		SteamLookupService service = Mockito.mock(SteamLookupService.class);
		LookupExitCode exitCode = new LookupExitCode();
		ByteArrayOutputStream errors = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errors));
		SteamLookupRunner runner = new SteamLookupRunner(service, new SteamLookupFormatter(),
				new SteamApiProperties("test-key"), exitCode);

		runner.run(new DefaultApplicationArguments(new String[0]));
		runner.run(new DefaultApplicationArguments("one", "two"));

		assertThat(errors.toString()).contains("Usage");
		assertThat(exitCode.getExitCode()).isNotZero();
		verify(service, never()).lookup(Mockito.anyString());
	}

	@Test
	void explainsHowToConfigureAMissingApiKey() throws Exception {
		SteamLookupService service = Mockito.mock(SteamLookupService.class);
		LookupExitCode exitCode = new LookupExitCode();
		ByteArrayOutputStream errors = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errors));
		SteamLookupRunner runner = new SteamLookupRunner(service, new SteamLookupFormatter(),
				new SteamApiProperties(""), exitCode);

		runner.run(new DefaultApplicationArguments("alice"));

		assertThat(errors.toString()).contains("steamcommunity.com/dev/apikey", ".env");
		assertThat(exitCode.getExitCode()).isNotZero();
		verify(service, never()).lookup(Mockito.anyString());
	}

	@Test
	void reportsAnInvalidOrUnresolvedIdentifierWithANonZeroCode() throws Exception {
		SteamLookupService service = Mockito.mock(SteamLookupService.class);
		when(service.lookup("invalid")).thenThrow(new IllegalArgumentException("Identifiant Steam invalide."));
		LookupExitCode exitCode = new LookupExitCode();
		ByteArrayOutputStream errors = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errors));
		SteamLookupRunner runner = new SteamLookupRunner(service, new SteamLookupFormatter(),
				new SteamApiProperties("test-key"), exitCode);

		runner.run(new DefaultApplicationArguments("invalid"));

		assertThat(errors.toString()).contains("Identifiant Steam invalide.");
		assertThat(exitCode.getExitCode()).isNotZero();
	}
}

package steamLookUpSpecKit.cli;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import steamLookUpSpecKit.model.LookupResult;
import steamLookUpSpecKit.service.SteamLookupService;
import steamLookUpSpecKit.steam.SteamApiException;
import steamLookUpSpecKit.steam.SteamApiProperties;

@Component
public final class SteamLookupRunner implements ApplicationRunner {

	private static final String USAGE = "Usage: ./gradlew bootRun --args=\"<steamid64|nom-personnalisé|url-de-profil>\"";
	private static final String API_KEY_HELP = "Clé Steam Web API absente. Obtenez-en une sur "
			+ "https://steamcommunity.com/dev/apikey, puis ajoutez STEAM_API_KEY=<votre-clé> dans .env.";

	private final SteamLookupService lookupService;
	private final SteamLookupFormatter formatter;
	private final SteamApiProperties apiProperties;
	private final LookupExitCode exitCode;

	public SteamLookupRunner(SteamLookupService lookupService, SteamLookupFormatter formatter,
			SteamApiProperties apiProperties, LookupExitCode exitCode) {
		this.lookupService = lookupService;
		this.formatter = formatter;
		this.apiProperties = apiProperties;
		this.exitCode = exitCode;
	}

	@Override
	public void run(ApplicationArguments arguments) {
		if (arguments.getSourceArgs().length != 1 || arguments.getNonOptionArgs().size() != 1) {
			System.err.println(USAGE);
			exitCode.setExitCode(2);
			return;
		}
		if (!apiProperties.isConfigured()) {
			System.err.println(API_KEY_HELP);
			exitCode.setExitCode(2);
			return;
		}
		try {
			LookupResult result = lookupService.lookup(arguments.getNonOptionArgs().getFirst());
			System.out.print(formatter.format(result));
			if (result.hasFailedSections()) {
				System.err.println("Erreur : une ou plusieurs sections Steam n’ont pas pu être consultées.");
				exitCode.setExitCode(1);
			}
			else {
				exitCode.setExitCode(0);
			}
		}
		catch (IllegalArgumentException | SteamApiException exception) {
			System.err.println("Erreur : " + exception.getMessage());
			exitCode.setExitCode(1);
		}
	}
}

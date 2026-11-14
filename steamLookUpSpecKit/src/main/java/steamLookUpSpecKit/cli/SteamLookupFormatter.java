package steamLookUpSpecKit.cli;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import steamLookUpSpecKit.model.GameLibrary;
import steamLookUpSpecKit.model.LookupResult;
import steamLookUpSpecKit.model.LookupSection;
import steamLookUpSpecKit.model.OwnedGame;
import steamLookUpSpecKit.model.RecentGame;
import steamLookUpSpecKit.model.SteamProfile;

@Component
public final class SteamLookupFormatter {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
			.withZone(ZoneOffset.UTC);

	public String format(LookupResult result) {
		StringBuilder output = new StringBuilder();
		appendProfile(output, result.profile());
		appendLibrary(output, result.library());
		appendRecentActivity(output, result.recentActivity());
		return output.toString();
	}

	private static void appendProfile(StringBuilder output, LookupSection<SteamProfile> section) {
		output.append("Profil\n");
		if (section.status() == LookupSection.Status.AVAILABLE && section.data() != null) {
			output.append("  Nom : ").append(valueOrUnavailable(section.data().displayName())).append('\n');
			output.append("  URL : ").append(valueOrUnavailable(section.data().profileUrl())).append('\n');
		}
		else {
			appendStatus(output, section);
		}
	}

	private static void appendLibrary(StringBuilder output, LookupSection<GameLibrary> section) {
		output.append("Bibliothèque\n");
		if (section.status() != LookupSection.Status.AVAILABLE || section.data() == null) {
			appendStatus(output, section);
			return;
		}
		GameLibrary library = section.data();
		output.append("  Jeux possédés : ").append(library.ownedGameCount()).append('\n');
		output.append("  Jeux lancés : ").append(library.playedGameCount()).append('\n');
		output.append("  Temps total : ").append(hours(library.totalPlaytimeMinutes())).append(" h\n");
		output.append("  Top 10 :\n");
		int rank = 1;
		for (OwnedGame game : library.topPlayedGames()) {
			output.append("    ").append(rank++).append(". ")
					.append(gameName(game.name(), game.appId()))
					.append(" — ").append(hours(game.totalPlaytimeMinutes())).append(" h — ")
					.append(formatDate(game.lastPlayedAt())).append('\n');
		}
	}

	private static void appendRecentActivity(StringBuilder output, LookupSection<List<RecentGame>> section) {
		output.append("Activité récente\n");
		if (section.status() != LookupSection.Status.AVAILABLE || section.data() == null) {
			appendStatus(output, section);
			return;
		}
		if (section.data().isEmpty()) {
			output.append("  Aucun jeu joué durant les 14 derniers jours.\n");
			return;
		}
		for (RecentGame game : section.data()) {
			output.append("  ").append(gameName(game.name(), game.appId()))
					.append(" — ").append(hours(game.playtimeLast14DaysMinutes())).append(" h — ")
					.append(formatDate(game.lastPlayedAt())).append('\n');
		}
	}

	private static void appendStatus(StringBuilder output, LookupSection<?> section) {
		String status = switch (section.status()) {
			case AVAILABLE -> "Données indisponibles.";
			case PRIVATE -> "Données privées.";
			case UNAVAILABLE_OR_HIDDEN -> "Données absentes ou masquées.";
			case FAILED -> "Échec de consultation.";
		};
		output.append("  ").append(status);
		if (section.message() != null && !section.message().isBlank()) {
			output.append(' ').append(section.message());
		}
		output.append('\n');
	}

	private static String formatDate(java.time.Instant instant) {
		return instant == null ? "date indisponible" : DATE_FORMAT.format(instant);
	}

	private static String hours(long minutes) {
		DecimalFormat format = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.FRANCE));
		return format.format(minutes / 60.0);
	}

	private static String gameName(String name, long appId) {
		return name == null ? "Nom indisponible (AppID " + appId + ")" : name;
	}

	private static String valueOrUnavailable(String value) {
		return value == null ? "indisponible" : value;
	}
}

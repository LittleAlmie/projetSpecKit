package steamLookUpSpecKit.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import steamLookUpSpecKit.model.GameLibrary;
import steamLookUpSpecKit.model.LookupResult;
import steamLookUpSpecKit.model.LookupSection;
import steamLookUpSpecKit.model.OwnedGame;
import steamLookUpSpecKit.model.RecentGame;
import steamLookUpSpecKit.model.SteamProfile;
import steamLookUpSpecKit.steam.SteamApiClient;
import steamLookUpSpecKit.steam.SteamApiException;

@Service
public final class SteamLookupService {

	private static final Duration RECENT_WINDOW = Duration.ofDays(14);

	private final SteamIdentifierResolver identifierResolver;
	private final SteamApiClient steamApiClient;
	private final Clock clock;

	public SteamLookupService(SteamIdentifierResolver identifierResolver, SteamApiClient steamApiClient, Clock clock) {
		this.identifierResolver = identifierResolver;
		this.steamApiClient = steamApiClient;
		this.clock = clock;
	}

	public LookupResult lookup(String identifier) {
		String steamId64 = identifierResolver.resolve(identifier);
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			CompletableFuture<LookupSection<SteamProfile>> profile = CompletableFuture.supplyAsync(
					() -> loadProfile(steamId64), executor);
			CompletableFuture<LookupSection<GameLibrary>> library = CompletableFuture.supplyAsync(
					() -> loadLibrary(steamId64), executor);
			CompletableFuture<LookupSection<List<RecentGame>>> recent = CompletableFuture.supplyAsync(
					() -> loadRecentActivity(steamId64), executor);
			return new LookupResult(profile.join(), library.join(), recent.join());
		}
	}

	private LookupSection<SteamProfile> loadProfile(String steamId64) {
		return capture(() -> steamApiClient.getPlayerSummary(steamId64), profile -> {
			if (profile.visibility() == SteamProfile.Visibility.PRIVATE) {
				return LookupSection.privateSection(profile, "Le profil Steam est privé.");
			}
			return LookupSection.available(profile);
		});
	}

	private LookupSection<GameLibrary> loadLibrary(String steamId64) {
		return capture(() -> steamApiClient.getOwnedGames(steamId64), games -> {
			if (games.isEmpty()) {
				return LookupSection.unavailableOrHidden(
						"Les données de bibliothèque sont absentes ou masquées.");
			}
			return LookupSection.available(summarizeLibrary(games.get()));
		});
	}

	private LookupSection<List<RecentGame>> loadRecentActivity(String steamId64) {
		Instant now = clock.instant();
		Instant cutoff = now.minus(RECENT_WINDOW);
		return capture(() -> steamApiClient.getRecentlyPlayedGames(steamId64), games -> {
			List<RecentGame> recentGames = games.stream()
					.filter(game -> game.lastPlayedAt() == null
							|| (!game.lastPlayedAt().isBefore(cutoff) && !game.lastPlayedAt().isAfter(now)))
					.sorted(Comparator.comparing(RecentGame::lastPlayedAt,
							Comparator.nullsLast(Comparator.reverseOrder())))
					.limit(5)
					.toList();
			return LookupSection.available(recentGames);
		});
	}

	private static GameLibrary summarizeLibrary(List<OwnedGame> games) {
		long totalMinutes = games.stream().mapToLong(OwnedGame::totalPlaytimeMinutes).sum();
		int playedCount = (int) games.stream().filter(game -> game.totalPlaytimeMinutes() > 0).count();
		List<OwnedGame> topGames = games.stream()
				.sorted(Comparator.comparingLong(OwnedGame::totalPlaytimeMinutes).reversed()
						.thenComparing(game -> game.name() == null ? "" : game.name(), String.CASE_INSENSITIVE_ORDER)
						.thenComparingLong(OwnedGame::appId))
				.limit(10)
				.toList();
		return new GameLibrary(games.size(), playedCount, totalMinutes, topGames);
	}

	private static <T, R> LookupSection<R> capture(Supplier<T> request,
			java.util.function.Function<T, LookupSection<R>> map) {
		try {
			return map.apply(request.get());
		}
		catch (SteamApiException exception) {
			return LookupSection.failed(exception.getMessage());
		}
	}
}

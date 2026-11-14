package steamLookUpSpecKit.model;

import java.util.List;

public record GameLibrary(
		int ownedGameCount,
		int playedGameCount,
		long totalPlaytimeMinutes,
		List<OwnedGame> topPlayedGames) {

	public GameLibrary {
		topPlayedGames = List.copyOf(topPlayedGames);
	}
}

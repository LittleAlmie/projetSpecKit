package steamLookUpSpecKit.model;

import java.time.Instant;

public record OwnedGame(long appId, String name, long totalPlaytimeMinutes, Instant lastPlayedAt) {
}

package steamLookUpSpecKit.model;

import java.time.Instant;

public record RecentGame(long appId, String name, long playtimeLast14DaysMinutes, Instant lastPlayedAt) {
}

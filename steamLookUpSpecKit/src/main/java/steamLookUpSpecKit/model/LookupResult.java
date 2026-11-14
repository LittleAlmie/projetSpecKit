package steamLookUpSpecKit.model;

import java.util.List;

public record LookupResult(
		LookupSection<SteamProfile> profile,
		LookupSection<GameLibrary> library,
		LookupSection<List<RecentGame>> recentActivity) {

	public boolean hasFailedSections() {
		return profile.status() == LookupSection.Status.FAILED
				|| library.status() == LookupSection.Status.FAILED
				|| recentActivity.status() == LookupSection.Status.FAILED;
	}
}

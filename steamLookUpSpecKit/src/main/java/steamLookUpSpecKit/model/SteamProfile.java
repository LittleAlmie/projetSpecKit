package steamLookUpSpecKit.model;

public record SteamProfile(String displayName, String profileUrl, Visibility visibility) {

	public enum Visibility {
		PUBLIC,
		PRIVATE,
		UNKNOWN
	}
}

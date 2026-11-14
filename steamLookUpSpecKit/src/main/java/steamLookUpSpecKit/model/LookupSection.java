package steamLookUpSpecKit.model;

public record LookupSection<T>(Status status, T data, String message) {

	public enum Status {
		AVAILABLE,
		PRIVATE,
		UNAVAILABLE_OR_HIDDEN,
		FAILED
	}

	public static <T> LookupSection<T> available(T data) {
		return new LookupSection<>(Status.AVAILABLE, data, null);
	}

	public static <T> LookupSection<T> privateSection(T data, String message) {
		return new LookupSection<>(Status.PRIVATE, data, message);
	}

	public static <T> LookupSection<T> unavailableOrHidden(String message) {
		return new LookupSection<>(Status.UNAVAILABLE_OR_HIDDEN, null, message);
	}

	public static <T> LookupSection<T> failed(String message) {
		return new LookupSection<>(Status.FAILED, null, message);
	}
}

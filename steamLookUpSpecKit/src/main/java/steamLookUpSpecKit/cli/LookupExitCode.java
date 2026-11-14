package steamLookUpSpecKit.cli;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public final class LookupExitCode implements ExitCodeGenerator {

	private final AtomicInteger exitCode = new AtomicInteger();

	@Override
	public int getExitCode() {
		return exitCode.get();
	}

	public void setExitCode(int value) {
		exitCode.set(value);
	}
}

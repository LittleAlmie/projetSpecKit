package steamLookUpSpecKit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SteamLookUpSpecKitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SteamLookUpSpecKitApplication.class, args);
		System.exit(SpringApplication.exit(context));
	}

}

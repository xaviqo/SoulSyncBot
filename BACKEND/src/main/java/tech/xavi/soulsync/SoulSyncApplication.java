package tech.xavi.soulsync;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.xavi.soulsync.service.auth.AccountService;
import tech.xavi.soulsync.service.configuration.HealthService;
import tech.xavi.soulsync.service.rest.ConfigurationRestService;

import static tech.xavi.soulsync.configuration.constants.ConfigurationFinals.DEFAULT_ADMIN_PASS;
import static tech.xavi.soulsync.configuration.constants.ConfigurationFinals.DEFAULT_ADMIN_USER;

@EnableScheduling
@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class SoulSyncApplication implements CommandLineRunner {

	private static final boolean CREATE_ADMIN = true;
	private final HealthService healthService;
	private final AccountService accountService;
	private final ConfigurationRestService configurationRestService;

	public static void main(String[] args) {
		SpringApplication.run(SoulSyncApplication.class, args);
	}

	@Override
	public void run(String... args){
		if (CREATE_ADMIN){
			accountService.createAccount(
					DEFAULT_ADMIN_USER,
					DEFAULT_ADMIN_PASS
			);
		}
		printSplashScreen();
		configurationRestService.checkApisConfiguration(
				healthService.initLogCheck()
		);
	}
	private void printSplashScreen(){
		log.info("    ________  __  ____   _____  ___  _______");
		log.info("   / __/ __ \\/ / / / /  / __| \\/ / |/ / ___/");
		log.info("  _\\ \\/ /_/ / /_/ / /___\\ \\  \\  /    / /__ ");
		log.info(" /___/\\____/\\____/____/___/  /_/_/|_/\\___/  ");
		log.info("");
		log.info("[run] - Default Admin created: '{}' - '{}'",DEFAULT_ADMIN_USER,DEFAULT_ADMIN_PASS);
	}
}

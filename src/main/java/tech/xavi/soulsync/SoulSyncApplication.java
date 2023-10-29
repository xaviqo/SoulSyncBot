package tech.xavi.soulsync;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.xavi.soulsync.service.request.HealthService;

@EnableScheduling
@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class SoulSyncApplication implements CommandLineRunner {

	private final HealthService healthService;
	public static void main(String[] args) {
		SpringApplication.run(SoulSyncApplication.class, args);
	}

	@Override
	public void run(String... args){
		printSplashScreen();
		healthService.initLogCheck();
	}



	private void printSplashScreen(){
		log.info("");
		log.info("  █████   ▒█████   █▓   ██   █▓     ▓████▓  ▓██   ██▓ ██▓▄    █  ▄███▓▄  ");
		log.info("▒██    ▒ ▒██▒  ██▒ ██  ▓██▒▓██▒    ▒██    ▒ ▒██  ██▒ ██ ▀█   █ ▒██▀ ▀█  ");
		log.info("░ ▓██▄   ▒██░  ██▒▓██  ▒██░▒██░     ▒█▓▓▒▄   ▒██ ██░▓██  ▀█ ██▒▒▓█    ▄ ");
		log.info("  ▒   ██▒▒██   ██░▓▓█  ░██░▒██░         ▓██▒ ░ ▐██▓░▓██▒  ▐▌██▒▒▓▓▄ ▄██▒");
		log.info("▒██████▒▒░ ████▓▒░▒▒█████▓ ▓██████▒▒██████▒▒ ░ ██▒▓░▒██░   ▓██░▒ ▓███▀ ░");
		log.info("▒ ▒▓▒ ▒ ░░ ▒░▒░▒░ ░▒▓▒ ▒ ▒ ░ ▒░▓  ░▒ ▒▓▒ ▒ ░  ██▒▒▒ ░ ▒░   ▒ ▒ ░ ░▒ ▒  ░");
		log.info("░ ░▒  ░ ░  ░ ▒ ▒░ ░░▒░ ░ ░ ░ ░ ▒  ░░ ░▒  ░ ░▓██ ░▒░ ░ ░░   ░ ▒░  ░  ▒   ");
		log.info("░  ░  ░  ░ ░ ░ ▒   ░░░ ░ ░   ░ ░   ░  ░  ░  ▒ ▒ ░░     ░   ░ ░ ░        ");
		log.info("      ░      ░ ░     ░         ░  ░      ░  ░ ░              ░ ░ ░      ");
		log.info("                                            ░ ░                ░        ");
	}
}

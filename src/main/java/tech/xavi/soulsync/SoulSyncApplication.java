package tech.xavi.soulsync;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import tech.xavi.soulsync.service.MainService;

@EnableScheduling
@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class SoulSyncApplication implements CommandLineRunner {

	private final MainService mainService;

	public static void main(String[] args) {
		SpringApplication.run(SoulSyncApplication.class, args);
		System.setProperty("com.mashape.unirest.debug", "true");
	}

	@Override
	public void run(String... args){
		printSplashScreen();
		mainService.slskdHealthCheckOnInit();
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

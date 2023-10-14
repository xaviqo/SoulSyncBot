package tech.xavi.soulsync;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.xavi.soulsync.gateway.SlskdGateway;

@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class SoulSyncApplication implements CommandLineRunner {

	private final SlskdGateway slskdGateway;

	public static void main(String[] args) {
		SpringApplication.run(SoulSyncApplication.class, args);
		System.setProperty("com.mashape.unirest.debug", "true");
	}

	@Override
	public void run(String... args) throws Exception {
		//System.out.println(slskdGateway.getToken("slskd","slskd"));
		//System.out.println(authService.getSpotifyToken());
		//JsonDatabase<Person> personDb = new JsonDatabase<>("persons.json", Person.class);
	}

	private void printSplashScreen(){
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

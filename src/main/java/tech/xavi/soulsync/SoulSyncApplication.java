package tech.xavi.soulsync;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.xavi.soulsync.service.SpotifyPlaylistService;

@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class SoulSyncApplication implements CommandLineRunner {

	//private final SpotifyAuthService spotifyAuthService;
	private final SpotifyPlaylistService playlistScanService;

	public static void main(String[] args) {
		SpringApplication.run(SoulSyncApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//JsonDatabase<Person> personDb = new JsonDatabase<>("persons.json", Person.class);
		//System.out.println(spotifyAuthService.getToken());;
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

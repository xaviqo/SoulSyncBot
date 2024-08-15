package tech.xavi.soulsync;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
@RequiredArgsConstructor
public class SoulsyncApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SoulsyncApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	}

}

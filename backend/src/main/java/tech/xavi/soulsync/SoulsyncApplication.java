package tech.xavi.soulsync;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
@RequiredArgsConstructor
public class SoulsyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoulsyncApplication.class, args);
	}

}

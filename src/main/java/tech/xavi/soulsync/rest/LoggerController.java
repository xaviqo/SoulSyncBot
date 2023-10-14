package tech.xavi.soulsync.rest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggerController {

    @PostMapping("/")
    public ResponseEntity<String> changeLogLevel(@RequestParam String loggerName, @RequestParam String logLevel) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
        config.getLoggerConfig(loggerName).setLevel(Level.toLevel(logLevel));
        context.updateLoggers();
        return ResponseEntity.ok("{\n" +
                "  \"message\": \"Log level changed\",\n" +
                "  \"logLevel\": \""+logLevel+"\"\n" +
                "}");
    }
}
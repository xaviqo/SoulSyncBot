package tech.xavi.soulsync.rest;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggerController {
/*
    @PostMapping("/v1/logger")
    public ResponseEntity<String> changeLogLevel(@RequestParam String loggerName, @RequestParam String logLevel) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
        config.getLoggerConfig(loggerName).setLevel(Level.toLevel(logLevel));
        context.updateLoggers();
        return ResponseEntity.ok("{\n" +
                "  \"message\": \"Log level changed\",\n" +
                "  \"logLevel\": \""+logLevel+"\"\n" +
                "}");
    }*/
}
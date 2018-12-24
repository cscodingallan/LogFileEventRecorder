package com.cs.coding.assignment;

import com.cs.coding.assignment.persistence.EventDetailsPersistenceService;
import com.cs.coding.assignment.service.LogFileEventRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

@SpringBootApplication
@Slf4j
public class LogFileEventRecorderApp {

    public static void main(String[] args) {
        if (args.length != 1) {
            log.error("Expected a single program arg specifying the path to the json log file. Instead got: " + Arrays.toString(args));
            java.lang.System.exit(-1);
        }

        SpringApplication.run(LogFileEventRecorderApp.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

        return args -> {

            if (args.length != 1) {
                return;
            }

            log.info("Recorder started. Log file: " + args[0]);

            File jsonLogFile = new File(args[0]);
            try (FileInputStream fis = new FileInputStream(jsonLogFile)) {
                LogFileEventRecorder logFileEventRecorder = ctx.getBean(LogFileEventRecorder.class);
                logFileEventRecorder.recordLogFileEvents(fis);
            }

            log.info("Recorder completed. Log file: " + args[0]);

            log.debug("All EventDetails: " + Arrays.toString(ctx.getBean(EventDetailsPersistenceService.class).allEventDetails().toArray())); // todo debug logging config
        };
    }
}

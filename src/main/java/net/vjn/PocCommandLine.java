package net.vjn;

import lombok.extern.slf4j.Slf4j;
import net.vjn.service.PocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PocCommandLine implements CommandLineRunner {
    private final PocService testService;
    private final ApplicationContext context;

    @Autowired
    public PocCommandLine(PocService testService, ApplicationContext context) {
        this.testService = testService;
        this.context = context;
    }

    @Override
    public void run(final String... args) throws IOException, InterruptedException {
        log.info("START");

        final StringBuilder builder = new StringBuilder();
        testService.pocAction(builder);
        testService.pocAsyncBulk(builder, 1, 50, 20_000);
        testService.pocAsyncBulk(builder, 10, 50, 10_000);
        testService.pocAsyncBulk(builder, 10, 1000, 10_000);
        testService.pocAsyncBulk(builder, 100, 50, 10_000);
        testService.pocAsyncBulk(builder, 100, 1000, 10_000);
        testService.pocAsyncBulk(builder, 20, 100, 10_000);
        testService.pocAsyncBulk(builder, 2, 50, 10_000);

        log.info("END");
        log.info(builder.toString());

        SpringApplication.exit(context);
    }
}

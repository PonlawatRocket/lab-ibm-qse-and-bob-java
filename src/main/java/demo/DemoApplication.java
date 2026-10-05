package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NovaPay Mobile Banking – Main Application Entry Point
 *
 * This is an internal workshop demo application for IBM QSE + IBM Bob.
 * The main UI is a realistic mobile banking interface.
 * Operator/workshop details are at /demo.
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

  Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @RequestMapping("/")
  public void info() {
    logger.info("Endpoint was hit at " + System.currentTimeMillis());
  }

  @RequestMapping("/error")
  public void error() {
    logger.error("Error was logged at " + System.currentTimeMillis());
  }

}


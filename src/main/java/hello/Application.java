package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class Application {

  @Bean
  WebClient webClient() {
    return WebClient.create();
  }

  @Autowired WebClient webClient;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @RequestMapping("/")
  public Mono<String> index(@RequestHeader(name="Host") final String host) {
    // todo: protocol match
    String url = "https://" + host + "/hello";
    return webClient.get().uri(url).retrieve().bodyToMono(String.class).map(String::toUpperCase);
  }

  @GetMapping("/hello")
  public String hello() {
    return "hello, world";
  }

}


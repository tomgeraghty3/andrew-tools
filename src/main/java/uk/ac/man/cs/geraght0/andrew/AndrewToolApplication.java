package uk.ac.man.cs.geraght0.andrew;

import java.io.File;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.ui.UI;

@Slf4j
@SpringBootApplication
public class AndrewToolApplication {

  public static void main(String[] args) {
    try {
      Application.launch(UI.class, args);
    } catch (Exception e) {
      log.error("Couldn't start the Spring application context", e);
    }
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
    properties.setIgnoreResourceNotFound(true);

    File file = new File(Config.PROPERTIES_FILE);
    if (!file.exists()) {
      log.info("Properties file doesn't exist. Creating it with default values");
      new Config().save();
    }

    log.info("Loading config from: {}", Config.PROPERTIES_FILE);
    properties.setLocation(new FileSystemResource(Config.PROPERTIES_FILE));
    return properties;
  }

  @Bean
  WebClient webClient() {
    return WebClient.builder()
                    .build();
  }
}
package uk.ac.man.cs.geraght0.andrew;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.ui.UI;

@Slf4j
@Configuration
@SpringBootApplication
public class AndrewToolApplication {

  public static void main(String[] args) {
    try {
      Application.launch(UI.class, args);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Couldn't start the Spring application context");
    }
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
    properties.setIgnoreResourceNotFound(true);
    properties.setLocation(new FileSystemResource(Config.PROPERTIES_FILE));
    return properties;
  }
}
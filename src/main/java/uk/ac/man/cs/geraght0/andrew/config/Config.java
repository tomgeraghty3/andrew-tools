package uk.ac.man.cs.geraght0.andrew.config;

import static uk.ac.man.cs.geraght0.andrew.config.Config.PREFIX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.harawata.appdirs.AppDirsFactory;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DefaultPropertiesPersister;
import uk.ac.man.cs.geraght0.andrew.model.DirGroupOption;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(PREFIX)
public class Config {
  //Constants
  static final String PREFIX = "app";
  private static final String PROP_FILE_NAME = "app.properties";
  private static final String APP_AUTHOR = "GERAGHT0";
  private static final String APP_NAME = "ANDREW_TOOL";
  private static final String VERSION = "1";
  public static final String PROPERTIES_FILE = String.format("%s\\%s", AppDirsFactory.getInstance()
                                                                                     .getSiteDataDir(APP_NAME, VERSION, APP_AUTHOR), PROP_FILE_NAME);

  //Config
  private String lastInputDirectory;
  private String lastOutputDirectory;
  private DirGroupOption lastDirGroupOption;
  private String lastExtension;

  public void save() {
    try (FileOutputStream out = createWriterToFile()) {
      // create and set properties into properties object
      Properties props = new Properties();
      final String identifier = "get";
      List<Method> methods = Arrays.stream(Config.class.getDeclaredMethods())
                                   .filter(f -> f.getName()
                                                 .startsWith(identifier))
                                   .collect(Collectors.toList());
      for (Method method : methods) {
        char[] c = method.getName()
                         .replace(identifier, "")
                         .toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        String name = new String(c);
        Object value = method.invoke(this);
        String prop = String.format("%s.%s", PREFIX, name);
        log.info("Storing \"{}\" with value \"{}\"", name, value);
        props.setProperty(prop, value == null ? "" : value.toString());
      }
      // get or create the file

      // write into it
      DefaultPropertiesPersister p = new DefaultPropertiesPersister();
      p.store(props, out, "");
    } catch (Exception e) {
      log.error("Couldn't update properties file", e);
    }
  }

  private FileOutputStream createWriterToFile() throws IOException {
    File dir = new File(Config.PROPERTIES_FILE).getParentFile();
    if (!dir.exists()) {
      try {
        FileUtils.forceMkdir(dir);
        log.info("Created missing config directories at \"{}\"", dir.getAbsolutePath());
      } catch (IOException e) {
        log.error("Could not create directories at path \"{}\". Error: {}", dir.getAbsolutePath(), e.getMessage(), e);
        throw e;
      }
    }

    return new FileOutputStream(PROPERTIES_FILE);
  }
}
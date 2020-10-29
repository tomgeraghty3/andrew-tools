package uk.ac.man.cs.geraght0.andrew.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.DirGroupOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class Backend {
  private final Config config;

  @SneakyThrows
  public void process(final String inDir, final String outDir, final DirGroupOption option, final String extension) {
    //Validate
    File in = validateDir(inDir, "Input");
    File out = validateDir(outDir, "Output");
    String ext = validateExtension(extension);
    validateOption(option);

    log.info("All input valid - saving config");
    config.setLastInputDirectory(in.getAbsolutePath());
    config.setLastOutputDirectory(out.getAbsolutePath());
    config.setLastDirGroupOption(option);
    config.setLastExtension(ext);
    config.save();

    //Check directory isn't empty
    List<File> eligibleFiles = getEligibleFiles(in, ext);
    log.info("Found {} file(s) in {} ending with extension {}", eligibleFiles.size(), in.getAbsolutePath(), ext);
    proessFiles(eligibleFiles, option);

    Thread.sleep(5000);
  }

  private void proessFiles(final List<File> eligibleFiles, final DirGroupOption option) {

  }

  private List<File> getEligibleFiles(final File in, final String extension) {
    File[] files = in.listFiles(pathname -> pathname.getName().toLowerCase()
                                                    .endsWith(extension.toLowerCase()));
    if (files == null || files.length == 0) {
      throw new IllegalArgumentException(String.format("There are no files with extension \"%s\" in \"%s\"", extension, in.getAbsolutePath()));
    }

    return Arrays.asList(files);
  }

  private File validateDir(final String dir, final String desc) {
    File file = new File(dir);
    if (!file.exists()) {
      throw new IllegalArgumentException(String.format("The %s directory \"%s\" does not exist", desc, file.getAbsolutePath()));
    }

    return file;
  }

  private String validateExtension(final String extension) {
    if (!StringUtils.hasText(extension)) {
      throw new IllegalArgumentException(String.format("The extension \"%s\" is not valid", extension));
    }

    String ext = extension.trim();
    if (ext.startsWith(".")) {
      return ext.substring(1);
    } else {
      return ext;
    }
  }

  private void validateOption(final DirGroupOption option) {
    if (option == null) {
      throw new IllegalArgumentException("A file group option must be selected");
    }
  }
}
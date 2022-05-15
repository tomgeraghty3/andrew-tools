package uk.ac.man.cs.geraght0.andrew.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import com.iberdrola.dtp.util.SpCollectionUtils;
import com.iberdrola.dtp.util.constants.Separator;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.constans.Constants;
import uk.ac.man.cs.geraght0.andrew.model.DirGroupOption;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;
import uk.ac.man.cs.geraght0.andrew.service.strategy.DirGroupStrategy;

@Slf4j
@Service
@RequiredArgsConstructor
public class Backend {

  private final Config config;
  private final WebClient webClient;

  @SneakyThrows
  public List<FileResult> process(final String inDir, final String outDir, final DirGroupOption option, final String extension) {
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
    List<FileResult> results = deduceFileActions(eligibleFiles, out, option);
    log.info("Actions: {}", SpCollectionUtils.toString(results, false, Separator.NEWLINE));
    results = validateDirectorySizes(results, option);
    log.info("Validated: {}", SpCollectionUtils.toString(results, false, Separator.NEWLINE));

    doActions(results);
    log.info("Results: {}", SpCollectionUtils.toString(results, false, Separator.NEWLINE));

    Thread.sleep(500);

    return results;
  }

  private List<File> getEligibleFiles(final File in, final String extension) {
    File[] files = in.listFiles(pathname -> pathname.getName()
                                                    .toLowerCase()
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


  List<FileResult> deduceFileActions(final List<File> eligibleFiles, final File directory, final DirGroupOption option) {
    DirGroupStrategy strategy = option.getStrategySupplier()
                                      .get();
    Collections.sort(eligibleFiles);
    return eligibleFiles.stream()
                        .map(f -> strategy.toResult(f, directory))
                        .collect(Collectors.toList());
  }

  List<FileResult> validateDirectorySizes(final List<FileResult> results, DirGroupOption option) {
    DirGroupStrategy strategy = option.getStrategySupplier()
                                      .get();
    List<FileResult> in = results.stream()
                                 .filter(r -> r.getProblem() == null)
                                 .collect(Collectors.toList());
    final MultiValuedMap<File, FileResult> dirToFile = SpCollectionUtils.toMultiMap(in,
                                                                                    FileResult::getDestinedDirectory,
                                                                                    Function.identity());
    boolean changesMade = false;
    for (Entry<File, Collection<FileResult>> e : dirToFile.asMap()
                                                          .entrySet()) {
      if (e.getValue()
           .size() < 2) {
        e.getValue()
         .forEach(fr -> fr.setProblem(Constants.FILE_ALONE));
        changesMade = true;
      } else if (e.getValue()
                  .size() > 2) {
        e.getValue()
         .forEach(fr -> fr.setProblem(Constants.FILE_TOO_MANY));
        changesMade = true;
      }
    }

    if (changesMade) {
      strategy.retrospectiveCorrection(results);
    }

    return results;
  }

  private void doActions(final List<FileResult> results) {
    List<FileResult> actions = results.stream()
                                      .filter(r -> r.getProblem() == null)
                                      .collect(Collectors.toList());
    FileResult toDo;
    for (FileResult action : actions) {
      toDo = action;
      try {
        log.debug("Starting: {}", toDo);
        if (!toDo.getDestinedDirectory()
                 .exists()) {
          log.debug("\tCreating destination dir: {}", toDo.getDestinedDirectory()
                                                          .getAbsolutePath());
          FileUtils.forceMkdir(toDo.getDestinedDirectory());
        }

        FileUtils.moveFile(action.getFile(), new File(toDo.getDestinedDirectory(), action.getFile()
                                                                                         .getName()));
      } catch (Exception e) {
        toDo.setProblem("Unexpected error: " + e.getMessage());
        log.error("Error executing {}: {}", toDo, e.getMessage(), e);
      }
    }
  }

  public Optional<String> checkForNewerVersion() {
    if (!NumberUtils.isParsable(config.getVersion())) {
      log.info("The version is currently \"{}\" which is likely not a release. Skipping check for newer release version", config.getVersion());
    } else {
      final double thisVersion = Double.parseDouble(config.getVersion());
      try {
        String url = "https://api.github.com/repos/tomgeraghty3/andrew-tools/tags";
        log.info("Looking for a newer version (than {}) of the application by calling: {}", thisVersion, url);
        ArrayNode json = webClient.get()
                                  .uri(url)
                                  .retrieve()
                                  .bodyToMono(ArrayNode.class)
                                  .block();

        Optional<JsonNode> max = Streams.stream(json.elements())
                                        .max(Comparator.comparingDouble(node -> Double.parseDouble(node.get("name")
                                                                                                       .asText())));
        if (max.isPresent()) {
          final String version = max.get()
                                    .get("name")
                                    .asText();
          double latestVersion = Double.parseDouble(version);
          url = String.format("%s%s", "https://github.com/tomgeraghty3/andrew-tools/releases/tag/", version);
          if (latestVersion > thisVersion) {
            return Optional.of(url);
          }
        }
      } catch (Exception e) {
        log.error("Unable to determine if there was a new version. Continuing as if there isn't", e);
      }
    }

    return Optional.empty();
  }
}
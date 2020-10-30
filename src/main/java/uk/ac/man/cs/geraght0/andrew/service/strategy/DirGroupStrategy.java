package uk.ac.man.cs.geraght0.andrew.service.strategy;

import java.io.File;
import java.util.List;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;

public interface DirGroupStrategy {

  default boolean isFilenameValid(String filename) {
    return getNameBeforeUnderscore(filename) != null;
  }

  default String getNameBeforeUnderscore(String filename) {
    if (!filename.contains("_")) {
      return null;
    }

    int index = filename.indexOf('_');
    return filename.substring(0, index);
  }

  FileResult toResult(File file, File outputDirectory);

  void retrospectiveCorrection(List<FileResult> results);
}
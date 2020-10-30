package uk.ac.man.cs.geraght0.andrew.service.strategy;

import java.io.File;
import java.util.List;
import uk.ac.man.cs.geraght0.andrew.constans.Constants;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;

public class NameBeforeUnderscoreDirStrategy implements DirGroupStrategy {

  @Override
  public FileResult toResult(final File file, final File outputDirectory) {
    if (!isFilenameValid(file.getName())) {
      return new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    } else {
      String nameBeforeUnderscore = getNameBeforeUnderscore(file.getName());
      File destination = new File(outputDirectory, nameBeforeUnderscore);
      return new FileResult(file, destination, null);
    }
  }

  @Override
  public void retrospectiveCorrection(final List<FileResult> results) {
    //Nothing to do here
  }
}

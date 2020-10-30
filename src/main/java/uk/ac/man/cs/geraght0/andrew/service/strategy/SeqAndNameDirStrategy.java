package uk.ac.man.cs.geraght0.andrew.service.strategy;

import java.io.File;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;

public class SeqAndNameDirStrategy extends SequentialDirStrategy {

  @Override
  public FileResult toResult(final File file, final File outputDirectory) {
    FileResult superFileResult = super.toResult(file, outputDirectory);
    if (!isFilenameValid(file.getName())) {
      return superFileResult;
    } else {
      String number = superFileResult.getDestinedDirectory()
                                     .getName();
      String nameBeforeUnderscore = getNameBeforeUnderscore(file.getName());
      String newDirName = String.format("%s_%s", number, nameBeforeUnderscore);
      File destination = new File(outputDirectory, newDirName);
      return new FileResult(file, destination, null);
    }
  }

  @Override
  int getIndexOfDirectory(final File dir) {
    String[] parts = dir.getName()
                        .split("_");
    return Integer.parseInt(parts[0]);
  }

  @Override
  void setIndexOfDirectory(final FileResult fr, final int dirIndex) {
    String[] parts = fr.getDestinedDirectory()
                       .getName()
                       .split("_");
    final String nameAfterUnderscore = parts[1];
    String newDirName = String.format("%s_%s", dirIndex, nameAfterUnderscore);
    File newDir = new File(fr.getDestinedDirectory()
                             .getParentFile(), newDirName);
    fr.setDestinedDirectory(newDir);
  }
}
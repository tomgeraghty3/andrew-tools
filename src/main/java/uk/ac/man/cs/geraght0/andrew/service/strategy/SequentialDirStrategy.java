package uk.ac.man.cs.geraght0.andrew.service.strategy;

import java.io.File;
import java.util.List;
import uk.ac.man.cs.geraght0.andrew.constans.Constants;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;

public class SequentialDirStrategy implements DirGroupStrategy {

  private int currentNumber;
  private String lastNameBeforeUnderscore;

  @Override
  public FileResult toResult(final File file, final File outputDirectory) {
    if (!isFilenameValid(file.getName())) {
      return new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    }

    String nameBeforeUnderscore = getNameBeforeUnderscore(file.getName());
    if (lastNameBeforeUnderscore == null || !lastNameBeforeUnderscore.equals(nameBeforeUnderscore)) {
      currentNumber++;
    }
    lastNameBeforeUnderscore = nameBeforeUnderscore;
    File destination = new File(outputDirectory, Integer.toString(currentNumber));
    return new FileResult(file, destination, null);
  }

  @Override
  public void retrospectiveCorrection(final List<FileResult> results) {
    int currentIndex = 1;
    int lastIndex = -1;
    //Go over each item
    for (int i = 0; i < results.size(); i++) {
      final FileResult fr = results.get(i);

      //Only do logic if this item doesn't have a problem
      if (fr.getProblem() == null) {
        int dirIndex = getIndexOfDirectory(fr.getDestinedDirectory());

        //If we've changed index then increase currentIndex
        if (lastIndex != -1 && lastIndex != dirIndex) {
          currentIndex++;
        }

        //Check we're still sequential
        if (dirIndex == currentIndex) {
          lastIndex = dirIndex;
        } else {
          int toReduceBy = dirIndex - currentIndex;
          reduce(results, i, toReduceBy);
          lastIndex = (dirIndex - toReduceBy);
        }
      }
    }
  }

  int getIndexOfDirectory(File dir) {
    return Integer.parseInt(dir.getName());
  }

  private void reduce(final List<FileResult> results, final int start, final int toReduceBy) {
    for (int i = start; i < results.size(); i++) {
      final FileResult fr = results.get(i);
      int dirIndex = getIndexOfDirectory(fr.getDestinedDirectory());
      dirIndex = dirIndex - toReduceBy;
      setIndexOfDirectory(fr, dirIndex);
    }
  }

  void setIndexOfDirectory(final FileResult fr, final int dirIndex) {
    File newDir = new File(fr.getDestinedDirectory()
                             .getParentFile(), Integer.toString(dirIndex));
    fr.setDestinedDirectory(newDir);
  }
}

package uk.ac.man.cs.geraght0.andrew.model.ui;

import lombok.Value;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;

@Value
public class FileProblemTableItem {

  String file;
  String problem;

  public FileProblemTableItem(FileResult result) {
    this.file = result.getFile()
                      .getName();
    this.problem = result.getProblem();
  }
}

package uk.ac.man.cs.geraght0.andrew.model;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class FileResult implements Comparable<FileResult> {

  private File file;
  private File destinedDirectory;
  private String problem;

  @Override
  public int compareTo(final FileResult o) {
    return file.compareTo(o.getFile());
  }
}
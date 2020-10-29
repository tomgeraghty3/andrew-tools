package uk.ac.man.cs.geraght0.andrew.model;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

public enum DirGroupOption {
  SEQUENTIAL("Sequential"),
  BEFORE_UNDERSCORE("Filename before underscore"),
  SEQ_FN_BEFORE_UNDERSCORE("Sequential followed by filename before underscore");

  @Getter
  private final String friendlyStr;

  DirGroupOption(final String friendlyStr) {
    this.friendlyStr = friendlyStr;
  }

  public static Optional<DirGroupOption> parse(final String text) {
    return Arrays.stream(DirGroupOption.values())
                 .filter(o -> o.getFriendlyStr()
                               .equals(text))
                 .findFirst();
  }
}
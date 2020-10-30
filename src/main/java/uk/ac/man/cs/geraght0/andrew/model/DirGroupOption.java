package uk.ac.man.cs.geraght0.andrew.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Getter;
import uk.ac.man.cs.geraght0.andrew.service.strategy.DirGroupStrategy;
import uk.ac.man.cs.geraght0.andrew.service.strategy.NameBeforeUnderscoreDirStrategy;
import uk.ac.man.cs.geraght0.andrew.service.strategy.SeqAndNameDirStrategy;
import uk.ac.man.cs.geraght0.andrew.service.strategy.SequentialDirStrategy;

public enum DirGroupOption {
  SEQUENTIAL("Sequential", SequentialDirStrategy::new, true),
  BEFORE_UNDERSCORE("Filename before underscore", NameBeforeUnderscoreDirStrategy::new, false),
  SEQ_FN_BEFORE_UNDERSCORE("Sequential followed by filename before underscore", SeqAndNameDirStrategy::new, true);

  @Getter
  private final String friendlyStr;
  @Getter
  private final Supplier<DirGroupStrategy> strategySupplier;
  @Getter
  private final boolean requiresRetrospectiveCorrective;

  DirGroupOption(final String friendlyStr, final Supplier<DirGroupStrategy> strategySupplier, final boolean requiresRetrospectiveCorrective) {
    this.friendlyStr = friendlyStr;
    this.strategySupplier = strategySupplier;
    this.requiresRetrospectiveCorrective = requiresRetrospectiveCorrective;
  }

  public static Optional<DirGroupOption> parse(final String text) {
    return Arrays.stream(DirGroupOption.values())
                 .filter(o -> o.getFriendlyStr()
                               .equals(text))
                 .findFirst();
  }
}
package uk.ac.man.cs.geraght0.andrew.model.ui;

import java.util.Collection;
import java.util.Map;
import lombok.Value;

@Value
public class FileMovedTableItem {

  String left;
  Collection<String> right;

  public <C extends Collection<String>> FileMovedTableItem(Map.Entry<String, C> map) {
    this.left = map.getKey();
    this.right = map.getValue();
  }
}

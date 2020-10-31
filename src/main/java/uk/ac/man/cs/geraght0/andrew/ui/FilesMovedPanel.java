package uk.ac.man.cs.geraght0.andrew.ui;

import com.google.common.collect.Lists;
import com.iberdrola.dtp.util.SpArrayUtils;
import com.iberdrola.dtp.util.SpCollectionUtils;
import com.iberdrola.dtp.util.constants.Separator;
import com.sun.javafx.collections.ImmutableObservableList;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;
import uk.ac.man.cs.geraght0.andrew.model.ui.FileMovedTableItem;

public class FilesMovedPanel extends TitledPane {

  private CheckBox chkResults;
  private TableView<FileMovedTableItem> tbl;
  private TableColumn<FileMovedTableItem, String> firstCol;
  private TableColumn<FileMovedTableItem, String> secondCol;
  private Collection<FileResult> lastResults;

  public FilesMovedPanel() {
    setText("Folders and files paired");
    createContent();
    addListeners();

    VBox layout = new VBox(chkResults, tbl);
    setContent(layout);
  }

  private void createContent() {
    chkResults = new CheckBox("Group results by directory");
    chkResults.setDisable(true);
    tbl = new TableView<>();
    firstCol = new TableColumn<>();
    firstCol.setCellValueFactory(new PropertyValueFactory<>("left"));
    secondCol = new TableColumn<>();
    secondCol.setCellValueFactory(new PropertyValueFactory<>("right"));
    secondCol.setCellValueFactory(param -> {
      Collection<String> collection = param.getValue()
                                           .getRight();
      return new SimpleStringProperty(SpCollectionUtils.toString(collection, false, Separator.NEWLINE));
    });
    firstCol.prefWidthProperty()
            .bind(tbl.widthProperty()
                     .multiply(0.5));
    secondCol.prefWidthProperty()
             .bind(tbl.widthProperty()
                      .multiply(0.5));
    final TableColumn<FileMovedTableItem, String>[] columns = SpArrayUtils.of(firstCol, secondCol);
    tbl.getColumns()
       .setAll(columns);
  }

  private void addListeners() {
    chkResults.setOnMouseClicked(e -> setItems(null));
  }

  public void setItems(final Collection<FileResult> results) {
    final Collection<FileResult> toSet = results == null ? lastResults : results;
    chkResults.setDisable(toSet.isEmpty());
    setText("Folders and files paired (" + SpCollectionUtils.descCount(toSet, "file") + ")");
    final boolean directoryGrouping = chkResults.isSelected();
    final FileMovedTableItem[] items;
    if (directoryGrouping) {
      firstCol.setText("Directory");
      secondCol.setText("Files");
      MultiValuedMap<String, String> dirToFile = SpCollectionUtils.toOrderedMultiMap(toSet, this::getDestinationName, r -> r.getFile()
                                                                                                                            .getName());
      items = dirToFile.asMap()
                       .entrySet()
                       .stream()
                       .map(FileMovedTableItem::new)
                       .toArray(FileMovedTableItem[]::new);
    } else {
      firstCol.setText("File");
      secondCol.setText("Directory");
      items = toSet.stream()
                   .map(r -> {
                     Pair<String, List<String>> pair = Pair.of(r.getFile()
                                                                .getName(), Lists.newArrayList(getDestinationName(r)));
                     return new FileMovedTableItem(pair);
                   })
                   .toArray(FileMovedTableItem[]::new);
    }

    tbl.setItems(new ImmutableObservableList<>(items));
    lastResults = toSet;
  }

  private String getDestinationName(final FileResult r) {
    return r.getDestinedDirectory() == null ? "n/a" : r.getDestinedDirectory()
                                                       .getName();
  }

}
package uk.ac.man.cs.geraght0.andrew.ui;

import com.iberdrola.dtp.util.SpArrayUtils;
import com.iberdrola.dtp.util.SpCollectionUtils;
import com.sun.javafx.collections.ImmutableObservableList;
import java.util.Collection;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;
import uk.ac.man.cs.geraght0.andrew.model.ui.FileProblemTableItem;

public class FilesFailedPanel extends TitledPane {

  private TableView<FileProblemTableItem> tbl;

  public FilesFailedPanel() {
    setText("Files which had a problem");
    createContent();
    setContent(tbl);
  }

  private void createContent() {
    tbl = new TableView<>();
    final TableColumn<FileProblemTableItem, String> fileCol = new TableColumn<>("File");
    fileCol.setCellValueFactory(new PropertyValueFactory<>("file"));
    final TableColumn<FileProblemTableItem, String> errorCol = new TableColumn<>("Problem");
    errorCol.setCellValueFactory(new PropertyValueFactory<>("problem"));
    final TableColumn<FileProblemTableItem, String>[] columns = SpArrayUtils.of(fileCol, errorCol);
    tbl.getColumns()
       .setAll(columns);
    fileCol.prefWidthProperty()
           .bind(tbl.widthProperty()
                    .multiply(0.3));
    errorCol.prefWidthProperty()
            .bind(tbl.widthProperty()
                     .multiply(0.7));
  }

  public void setItems(final Collection<FileResult> results) {
    setText("Files which had a problem (" + SpCollectionUtils.descCount(results, "file") + ")");
    final FileProblemTableItem[] items = results.stream()
                                                .map(FileProblemTableItem::new)
                                                .toArray(FileProblemTableItem[]::new);
    tbl.setItems(new ImmutableObservableList<>(items));
  }

}
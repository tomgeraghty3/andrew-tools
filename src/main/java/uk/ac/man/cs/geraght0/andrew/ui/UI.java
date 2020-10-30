package uk.ac.man.cs.geraght0.andrew.ui;

import com.google.common.collect.Lists;
import com.iberdrola.dtp.util.SpCollectionUtils;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ResourceUtils;
import uk.ac.man.cs.geraght0.andrew.AndrewToolApplication;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.DirGroupOption;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;
import uk.ac.man.cs.geraght0.andrew.service.Backend;

@Slf4j
public class UI extends Application {

  private ConfigurableApplicationContext applicationContext;

  //UI Constants
  private static final int WIDTH_OVERALL = 1000;
  private static final int HEIGHT_LEFT_RIGHT = 600;
  private static final int HEIGHT_OVERALL = HEIGHT_LEFT_RIGHT + 100;
  private static final double MAX_TXT_HEIGHT = 40.0;
  private static final double MIN_TXT_WIDTH = WIDTH_OVERALL - 200.0;

  //UI Components
  private GridPane grid;
  private TextArea txtDirInput;
  private DirectoryChooser fcInput;
  private TextArea txtDirOutput;
  private DirectoryChooser fcOutput;
  private TextArea txtExtension;
  private ToggleGroup groupByOption;
  private Map<DirGroupOption, RadioButton> optionToButton;
  private ProgressBar pg;
  private Accordion accordion;
  private FilesMovedPanel filesMovedPanel;
  private FilesFailedPanel filesProblemPanel;

  //State
  private int currentRow;
  private Backend backend;
  private Config config;
  private ExecutorService executorService;
  private List<Button> selectButtons;


  @Override
  public void init() {
    String[] args = getParameters().getRaw()
                                   .toArray(new String[0]);
    this.applicationContext =
        new SpringApplicationBuilder()
            .sources(AndrewToolApplication.class)
            .run(args);
    backend = applicationContext.getBean(Backend.class);
    config = applicationContext.getBean(Config.class);
    executorService = Executors.newSingleThreadExecutor();
    selectButtons = new ArrayList<>();
  }

  @Override
  public void stop() {
    this.applicationContext.close();
    Platform.exit();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Scene scene = generateScene();
    URL resource = ResourceUtils.getURL("style.css");
    scene.getStylesheets()
         .addAll(resource.toExternalForm());

    if (config.getLastInputDirectory() != null) {
      txtDirInput.setText(config.getLastInputDirectory());
      File in = new File(config.getLastInputDirectory());
      if (in.exists()) {
        fcInput.setInitialDirectory(in);
      }
    }
    if (config.getLastOutputDirectory() != null) {
      txtDirOutput.setText(config.getLastOutputDirectory());
      File out = new File(config.getLastOutputDirectory());
      if (out.exists()) {
        fcOutput.setInitialDirectory(out);
      }
    }
    if (config.getLastExtension() != null) {
      txtExtension.setText(config.getLastExtension());
    }
    if (config.getLastDirGroupOption() != null) {
      final RadioButton radioButton = optionToButton.get(config.getLastDirGroupOption());
      radioButton.setSelected(true);
    }

    primaryStage.setTitle("Tools for Andrew Ward-Jones");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private Scene generateScene() {
    grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setAlignment(Pos.CENTER);
    grid.setPadding(new Insets(10, 10, 10, 10));

    //Row 1
    createDirInfo(true);
    //Row 2
    createDirInfo(false);
    //Row 3
    createRowThree();
    //Row 4
    createResultsAccordion();

    //Scaling
    grid.setMinSize(WIDTH_OVERALL, HEIGHT_OVERALL);
    StackPane root = new StackPane(grid);
    root.setAlignment(Pos.CENTER);
    NumberBinding maxScale = Bindings.min(root.widthProperty()
                                              .divide(WIDTH_OVERALL),
                                          root.heightProperty()
                                              .divide(HEIGHT_OVERALL));
    grid.scaleXProperty()
        .bind(maxScale);
    grid.scaleYProperty()
        .bind(maxScale);

    //Shortcuts
    return new Scene(root, WIDTH_OVERALL, HEIGHT_OVERALL);
  }

  private void createDirInfo(final boolean input) {
    //Create info label
    String lbl = (input ? "Input" : "Output") + " directory";
    Label lblInfo = new Label(lbl);

    //Create actual area to hold directory
    TextArea textArea = new TextArea();
    textArea.setEditable(false);
    textArea.setMinWidth(MIN_TXT_WIDTH);
    textArea.setMaxHeight(MAX_TXT_HEIGHT);
    final DirectoryChooser fc = new DirectoryChooser();
    if (input) {
      this.txtDirInput = textArea;
      this.fcInput = fc;
    } else {
      this.txtDirOutput = textArea;
      this.fcOutput = fc;
    }

    //Create Button
    final Button btnSelect = new Button("Select");
    Font font = new Font(13);
    btnSelect.setFont(font);
    btnSelect.setOnMouseClicked(e -> {
      File selected = fc.showDialog(null);
      if (selected != null) {
        textArea.setText(selected.getAbsolutePath());
        fc.setInitialDirectory(selected);
      }
    });
    selectButtons.add(btnSelect);

    //Create layout
    HBox layInput = new HBox(textArea, btnSelect);
    layInput.setSpacing(20);
    layInput.setAlignment(Pos.CENTER);

    //Overall layout
    VBox layDirInfo = new VBox(lblInfo, layInput);

    //Add to grid
    grid.add(layDirInfo, 0, currentRow);
    currentRow++;
  }

  private void createRowThree() {
    //Create info label
    Label lblInfo = new Label("Directory names:");
    //Create radio buttons
    groupByOption = new ToggleGroup();
    optionToButton = Arrays.stream(DirGroupOption.values())
                           .collect(Collectors.toMap(Function.identity(), fgo -> {
                             RadioButton rb = new RadioButton(fgo.getFriendlyStr());
                             rb.setToggleGroup(groupByOption);
                             return rb;
                           }, (u, v) -> {
                             throw new IllegalStateException(String.format("Duplicate key %s", u));
                           }, TreeMap::new));
    optionToButton.get(DirGroupOption.BEFORE_UNDERSCORE)
                  .setSelected(true);

    final HBox layOptions = new HBox(optionToButton.values()
                                                   .toArray(new Node[0]));
    layOptions.setSpacing(5);
    VBox layControls = new VBox(5, lblInfo, layOptions);
    layControls.setAlignment(Pos.CENTER_LEFT);

    //Create extension box
    txtExtension = new TextArea();
    txtExtension.setMaxSize(50, 10);
    txtExtension.setFont(new Font(13));
    txtExtension.setWrapText(true);
    txtExtension.textProperty()
                .addListener((observable, oldValue, newValue) -> {
                  if (newValue.length() > 4) {
                    txtExtension.setText(newValue.substring(0, 4));
                  }
                });
    lblInfo = new Label("File extension to look for:  ");
    lblInfo.setLabelFor(txtExtension);
    lblInfo.setAlignment(Pos.BOTTOM_RIGHT);
    HBox layExtension = new HBox(lblInfo, txtExtension);
    layExtension.setAlignment(Pos.CENTER);

    BorderPane borderPane = new BorderPane();
    borderPane.setMinWidth(MIN_TXT_WIDTH);
    borderPane.setLeft(layControls);
    borderPane.setRight(layExtension);

    final Button btnGo = new Button("Pair files\ninto folders");
    btnGo.setTextAlignment(TextAlignment.CENTER);
    btnGo.setOnMouseClicked(this::onGoClick);
    selectButtons.add(btnGo);
    HBox layRow = new HBox(borderPane, btnGo);
    layRow.setSpacing(20);
    layRow.setAlignment(Pos.CENTER);

    grid.add(layRow, 0, currentRow);
    currentRow++;

    pg = new ProgressBar();
    pg.setMinWidth(MIN_TXT_WIDTH);
    pg.setVisible(false);
    currentRow += 5;
    grid.add(pg, 0, currentRow);
  }

  private void createResultsAccordion() {
    filesMovedPanel = new FilesMovedPanel();
    filesProblemPanel = new FilesFailedPanel();
    filesMovedPanel.setDisable(true);
    filesProblemPanel.setDisable(true);
    accordion = new Accordion(filesMovedPanel, filesProblemPanel);
    accordion.expandedPaneProperty()
             .addListener((ObservableValue<? extends TitledPane> property, final TitledPane oldPane, final TitledPane newPane) -> {
               if (oldPane != null) {
                 oldPane.setCollapsible(true);
               }
               if (newPane != null) {
                 Platform.runLater(() -> newPane.setCollapsible(false));
               }
             });
    accordion.setExpandedPane(filesMovedPanel);
    grid.add(accordion, 0, currentRow);
    currentRow++;
  }

  private void onGoClick(final MouseEvent mouseEvent) {
    RadioButton btn = (RadioButton) groupByOption.getSelectedToggle();
    DirGroupOption option = DirGroupOption.parse(btn.getText())
                                          .orElseThrow(() -> new IllegalStateException("Internal error parsing back from radio buttons"));

    getComponentsToShowDuringProgress().forEach(n -> n.setVisible(true));
    getComponentsToHideDuringProgress().forEach(n -> n.setVisible(false));
    getComponentsToToggleDisableDuringProgress().forEach(b -> b.setDisable(true));
    filesMovedPanel.setDisable(false);
    filesProblemPanel.setDisable(false);
    executorService.submit(() -> {
      try {
        List<FileResult> results = backend.process(txtDirInput.getText(), txtDirOutput.getText(), option, txtExtension.getText());
        MultiValuedMap<Boolean, FileResult> problemToFileResult = SpCollectionUtils.toMultiMap(results, fr -> fr.getProblem() == null,
                                                                                               Function.identity());
        Collection<FileResult> problemFiles = problemToFileResult.get(false);
        Collection<FileResult> happyFiles = problemToFileResult.get(true);
        Platform.runLater(() ->
                              updateResults(happyFiles, problemFiles)
        );
      } catch (Exception e) {
        log.error("Uncaught error", e);
        Platform.runLater(() -> new Alert(AlertType.ERROR, e.getMessage()).showAndWait());
      } finally {
        getComponentsToShowDuringProgress().forEach(n -> n.setVisible(false));
        getComponentsToHideDuringProgress().forEach(n -> n.setVisible(true));
        getComponentsToToggleDisableDuringProgress().forEach(n -> n.setDisable(false));
      }
    });
  }

  private List<Node> getComponentsToToggleDisableDuringProgress() {
    List<Node> components = Lists.newArrayList(selectButtons);
    components.add(txtExtension);
    components.addAll(optionToButton.values());
    return components;
  }

  private List<Node> getComponentsToShowDuringProgress() {
    return Lists.newArrayList(pg);
  }

  private List<Node> getComponentsToHideDuringProgress() {
    return Lists.newArrayList(filesMovedPanel, filesProblemPanel);
  }


  private void updateResults(final Collection<FileResult> happyFiles, final Collection<FileResult> problemFiles) {
    filesMovedPanel.setItems(happyFiles);
    filesProblemPanel.setItems(problemFiles);
    if (happyFiles.isEmpty() && !problemFiles.isEmpty()) {
      accordion.setExpandedPane(filesProblemPanel);
    } else {
      accordion.setExpandedPane(filesMovedPanel);
    }
  }
}
package uk.ac.man.cs.geraght0.andrew.ui;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ResourceUtils;
import uk.ac.man.cs.geraght0.andrew.AndrewToolApplication;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.model.DirGroupOption;
import uk.ac.man.cs.geraght0.andrew.service.Backend;

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
  private TextArea txtDirOutput;
  private TextArea txtExtension;
  private Map<DirGroupOption, RadioButton> optionToButton;
  private ProgressBar pg;

  //State
  private int currentRow;
  private Backend backend;
  private Config config;
  private ExecutorService executorService;


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
    }
    if (config.getLastOutputDirectory() != null) {
      txtDirOutput.setText(config.getLastOutputDirectory());
    }
    if (config.getLastExtension() != null) {
      txtExtension.setText(config.getLastExtension());
    }
    if (config.getLastDirGroupOption() != null) {
      final RadioButton radioButton = optionToButton.get(config.getLastDirGroupOption());
      radioButton.setSelected(true);
    }

    primaryStage.setTitle("Transformation UI for Alterian based content");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private Scene generateScene() {
    grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setAlignment(Pos.CENTER);
    grid.setPadding(new Insets(10, 10, 10, 10));

    //Top
    createDirInfo(true);
    createDirInfo(false);

    //File selection
    createFileGroupSelection();

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
    if (input) {
      this.txtDirInput = textArea;
    } else {
      this.txtDirOutput = textArea;
    }

    //Create Button
    final DirectoryChooser fc = new DirectoryChooser();
    final Button btnSelect = new Button("Select");
    Font font = new Font(13);
    btnSelect.setFont(font);
    btnSelect.setOnMouseClicked(e -> {
      File selected = fc.showDialog(null);
      if (selected == null) {
        textArea.setText("");
      } else {
        textArea.setText(selected.getAbsolutePath());
      }
    });

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

  private void createFileGroupSelection() {
    //Create info label
    Label lblInfo = new Label("Group pairs of files by:");
    //Create radio buttons
    final ToggleGroup groupByOption = new ToggleGroup();
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

    Button btnGo = new Button("Pair files\ninto folders");
    btnGo.setTextAlignment(TextAlignment.CENTER);
    btnGo.setOnMouseClicked(event -> {
      RadioButton btn = (RadioButton) groupByOption.getSelectedToggle();
      DirGroupOption option = DirGroupOption.parse(btn.getText())
                                            .orElseThrow(() -> new IllegalStateException("Internal error parsing back from radio buttons"));

      pg.setVisible(true);
      btnGo.setDisable(true);
      executorService.submit(() -> {
        try {
          backend.process(txtDirInput.getText(), txtDirOutput.getText(), option, txtExtension.getText());
        } catch (Exception e) {
          Platform.runLater(() -> {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
          });
        } finally {
          pg.setVisible(false);
          btnGo.setDisable(false);
        }
      });
    });
    HBox layRow = new HBox(borderPane, btnGo);
    layRow.setSpacing(20);
    layRow.setAlignment(Pos.CENTER);

    grid.add(layRow, 0, currentRow);
    currentRow++;

    pg = new ProgressBar();
    pg.setMinWidth(MIN_TXT_WIDTH);
    pg.setVisible(false);
    grid.add(pg, 0, currentRow + 5);
  }

//  private Button generateButton() {
//    btnTransform = new Button("Generate");
//			btnTransform.setOnMouseClicked(e -> {
//				RadioButton btn = (RadioButton) groupSelection.getSelectedToggle();
//				TransformationOption transformationOption = SpEnumUtils.parseForExpected(TransformationOption.class, btn.getText());
//				log.info("Starting transformation for option {}", transformationOption);
//
//				if (StringUtils.isEmpty(txtInput.getText())) {
//					new Alert(Alert.AlertType.ERROR, "No input was provided").showAndWait();
//				} else {
//					try {
//						AbsLineTransformer<?, ?> transformer = transformationOption.createTransformer(applicationContext);
//						transformer.convertAllLines(txtInput.getText());
//						List<String> convertedWithWarnings = transformer.validateConvertedLines();
//						boolean permittedToContinue = true;
//						if (!convertedWithWarnings.isEmpty()) {
//							String txt = "Given the following warnings; do you want to continue?"
//													 + System.lineSeparator()
//													 + convertedWithWarnings.stream()
//																									.collect(Collectors.joining(System.lineSeparator()));
//							Alert alert = new Alert(Alert.AlertType.CONFIRMATION, txt, ButtonType.YES, ButtonType.NO);
//							Optional<ButtonType> bt = alert.showAndWait();
//							permittedToContinue = bt.isPresent() && bt.get()
//																												.equals(ButtonType.YES);
//						}
//
//						if (permittedToContinue) {
//							Object converted = transformer.getFinalFormat();
//							txtOutput.setText(converted.toString());
//						} else {
//							new Alert(Alert.AlertType.INFORMATION, "The conversion was cancelled").show();
//						}
//					} catch (Exception ex) {
//						new Alert(Alert.AlertType.ERROR, "An error occurred transforming input: " + ex.getMessage()).showAndWait();
//					}
//				}
//			});

//    return btnTransform;
//  }
}

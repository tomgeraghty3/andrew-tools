package uk.ac.man.cs.geraght0.andrew.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.constans.Constants;
import uk.ac.man.cs.geraght0.andrew.model.DirGroupOption;
import uk.ac.man.cs.geraght0.andrew.model.FileResult;

@ExtendWith(MockitoExtension.class)
class BackendTest {

  @Mock
  private Config mockConfig;
  @Mock
  private WebClient webClient;
  private Backend classUnderTest;

  @BeforeEach
  void before() {
    MockitoAnnotations.initMocks(this);
    classUnderTest = new Backend(mockConfig, webClient);
  }

  @Test
  void deduceFileActions_whenSeq_match() {
    final List<File> input = new ArrayList<>();
    final List<FileResult> expected = new ArrayList<>();
    final File out = new File("out");

    int count = 1;
    File file = new File("nounderscore");
    FileResult result = new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    input.add(file);
    expected.add(result);

    file = new File("123_001_20201030.mov");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(file);
    expected.add(result);
    file = new File("123_002_20201030.mov");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(file);
    expected.add(result);
    file = new File("123_003_20201030.mov");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(file);
    expected.add(result);

    file = new File("nounderscore2");
    result = new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    input.add(file);
    expected.add(result);

    count++;
    file = new File("124_001_20201030.mov");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(file);
    expected.add(result);
    file = new File("124_002_20201030.mov");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(file);
    expected.add(result);

    count++;
    file = new File("125_001_20201030.mov");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(file);
    expected.add(result);

    List<FileResult> actual = classUnderTest.deduceFileActions(input, out, DirGroupOption.SEQUENTIAL);
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void deduceFileActions_whenFilename_match() {
    final List<File> input = new ArrayList<>();
    final List<FileResult> expected = new ArrayList<>();
    final File out = new File("out");

    File file = new File("nounderscore");
    FileResult result = new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    input.add(file);
    expected.add(result);

    int prefix = 123;
    file = new File(prefix + "_001_20201030.mov");
    result = new FileResult(file, new File(out, "" + prefix), null);
    input.add(file);
    expected.add(result);
    file = new File(prefix + "_002_20201030.mov");
    result = new FileResult(file, new File(out, "" + prefix), null);
    input.add(file);
    expected.add(result);

    file = new File("nounderscore2");
    result = new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    input.add(file);
    expected.add(result);

    prefix++;
    file = new File(prefix + "_001_20201030.mov");
    result = new FileResult(file, new File(out, "" + prefix), null);
    input.add(file);
    expected.add(result);
    file = new File(prefix + "_002_20201030.mov");
    result = new FileResult(file, new File(out, "" + prefix), null);
    input.add(file);
    expected.add(result);

    List<FileResult> actual = classUnderTest.deduceFileActions(input, out, DirGroupOption.BEFORE_UNDERSCORE);
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void deduceFileActions_whenSeqAndFilename_match() {
    final List<File> input = new ArrayList<>();
    final List<FileResult> expected = new ArrayList<>();
    final File out = new File("out");

    int count = 1;
    File file = new File("nounderscore");
    FileResult result = new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    input.add(file);
    expected.add(result);

    int prefix = 123;
    file = new File(prefix + "_001_20201030.mov");
    result = new FileResult(file, new File(out, count + "_" + prefix), null);
    input.add(file);
    expected.add(result);
    file = new File(prefix + "_002_20201030.mov");
    result = new FileResult(file, new File(out, count + "_" + prefix), null);
    input.add(file);
    expected.add(result);
    file = new File(prefix + "_003_20201030.mov");
    result = new FileResult(file, new File(out, count + "_" + prefix), null);
    input.add(file);
    expected.add(result);

    file = new File("nounderscore2");
    result = new FileResult(file, null, Constants.NOT_RECOGNISED_FILENAME);
    input.add(file);
    expected.add(result);

    count++;
    prefix++;
    file = new File(prefix + "_001_20201030.mov");
    result = new FileResult(file, new File(out, count + "_" + prefix), null);
    input.add(file);
    expected.add(result);

    List<FileResult> actual = classUnderTest.deduceFileActions(input, out, DirGroupOption.SEQ_FN_BEFORE_UNDERSCORE);
    Collections.sort(expected);
    Collections.sort(actual);
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void testValidateDirSizes_whenDirWith1FileOnly_shouldChangeError() {
    final List<FileResult> input = new ArrayList<>();
    final List<FileResult> expected = new ArrayList<>();
    final File out = new File("out");

    int count = 1;
    File file = new File("file");
    FileResult result = new FileResult(file, new File(out, "" + count), null);
    input.add(result);
    FileResult eResult = result.toBuilder()
                               .problem(Constants.FILE_ALONE)
                               .build();
    expected.add(eResult);

    file = new File("invalid");
    result = new FileResult(file, new File(out, "" + count), Constants.NOT_RECOGNISED_FILENAME);
    input.add(result);
    expected.add(result);

    count++;
    file = new File("file2");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(result);
    eResult = result.toBuilder()
                    .problem(Constants.FILE_TOO_MANY)
                    .build();
    expected.add(eResult);
    file = new File("file3");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(result);
    eResult = result.toBuilder()
                    .problem(Constants.FILE_TOO_MANY)
                    .build();
    expected.add(eResult);
    file = new File("file4");
    result = new FileResult(file, new File(out, "" + count), null);
    input.add(result);
    eResult = result.toBuilder()
                    .problem(Constants.FILE_TOO_MANY)
                    .build();
    expected.add(eResult);

    List<FileResult> actual = classUnderTest.validateDirectorySizes(input, DirGroupOption.SEQUENTIAL);
    assertThat(actual).isEqualTo(expected);
  }
}
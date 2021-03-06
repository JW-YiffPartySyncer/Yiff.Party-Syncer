package Lib.mega.cmd;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import Lib.mega.MegaUtils;
import Lib.mega.error.MegaIOException;
import Lib.mega.error.MegaInvalidResponseException;

public class MegaCmdExport extends AbstractMegaCmdCallerWithParams<ExportInfo> {

  private final Optional<String> remotePath;

  private Optional<String> password;

  private boolean exportDeleted;

  private boolean listOnly;

  public MegaCmdExport(String remotePath) {
    this.remotePath = Optional.of(remotePath);
    this.password = Optional.empty();
  }

  @Override
  List<String> cmdParams() {
    final List<String> cmdParams = new LinkedList<>();

    remotePath
        .filter(x -> !listOnly)
        .ifPresent(x -> {
          cmdParams.add(exportDeleted ? "-d" : "-a");
          cmdParams.add("-f");
        });

    remotePath.ifPresent(cmdParams::add);

    password.ifPresent(p -> cmdParams.add(String.format("--password=%s", p)));

    return cmdParams;
  }

  @Override
  public String getCmd() {
    return "export";
  }

  @Override
  public ExportInfo call() {
    try {
      return MegaUtils.handleCmdWithOutput(executableCommandArray())
          .stream().findFirst()
          .map(ExportInfo::parseExportInfo)
          .orElseThrow(() -> new MegaInvalidResponseException(
              "Invalid response while exporting '%s'", remotePath
          ));
    } catch (IOException e) {
      throw new MegaIOException("Error while exporting " + remotePath);
    }
  }

  public List<ExportInfo> list() {
    justList();
    try {
      return MegaUtils.handleCmdWithOutput(executableCommandArray()).stream()
          .map((ExportInfo::parseExportListInfo))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new MegaIOException("Error while exporting " + remotePath);
    }
  }

  public MegaCmdExport enablePublicLink() {
    exportDeleted = false;
    return this;
  }

  public MegaCmdExport removePublicLink() {
    exportDeleted = true;
    return this;
  }

  public boolean isExportDeleted() {
    return exportDeleted;
  }

  public MegaCmdExport setPassword(String password) {
    this.password = Optional.of(password);
    return this;
  }

  public MegaCmdExport removePassword() {
    this.password = Optional.empty();
    return this;
  }

  protected MegaCmdExport justList() {
    listOnly = true;
    return this;
  }
}

package Lib.mega.cmd;

import java.util.Arrays;
import java.util.List;

import Lib.mega.MegaUtils;

public class MegaCmdCopy extends AbstractMegaCmdRunnerWithParams {

  private final String sourceRemotePath;

  private final String remoteTarget;

  public MegaCmdCopy(String sourceRemotePath, String remoteTarget) {
    this.sourceRemotePath = sourceRemotePath;
    this.remoteTarget = remoteTarget;
  }

  @Override
  List<String> cmdParams() {
    return Arrays.asList(sourceRemotePath, remoteTarget);
  }

  @Override
  public String getCmd() {
    return "cp";
  }

  public String getSourceRemotePath() {
    return sourceRemotePath;
  }

  public String getRemoteTarget() {
    return remoteTarget;
  }

  public boolean isRemoteTargetAUser() {
    return MegaUtils.isEmail(remoteTarget);
  }

  public boolean isRemoteTargetADirectory() {
    return MegaUtils.isDirectory(remoteTarget);
  }
}

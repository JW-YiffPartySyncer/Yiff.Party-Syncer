package Lib.mega;

import static Lib.mega.Mega.CMD_TTL_ENV_VAR;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import Lib.mega.error.MegaConfirmationRequiredException;
import Lib.mega.error.MegaIOException;
import Lib.mega.error.MegaInvalidEmailException;
import Lib.mega.error.MegaInvalidStateException;
import Lib.mega.error.MegaInvalidTypeException;
import Lib.mega.error.MegaLoginRequiredException;
import Lib.mega.error.MegaNodesNotFetchedException;
import Lib.mega.error.MegaOperationNotAllowedException;
import Lib.mega.error.MegaResourceAlreadyExistsException;
import Lib.mega.error.MegaResourceNotFoundException;
import Lib.mega.error.MegaUnexpectedFailureException;
import Lib.mega.error.MegaWrongArgumentsException;

public interface MegaUtils {

  DateTimeFormatter MEGA_FILE_DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("ddMMMyyyy HH:mm:ss", Locale.US);

  Pattern EMAIL_PATTERN = Pattern.compile(
      "^[\\w]+@[\\w]+\\.[a-zA-Z]{2,6}$",
      Pattern.CASE_INSENSITIVE
  );

  Pattern DIRECTORY_PATTERN = Pattern.compile(
      "[\\/]?[\\p{Alnum}]+(\\/[\\p{Alnum}]+)*",
      Pattern.CASE_INSENSITIVE
  );

  Integer MEGA_TTL = Optional.ofNullable(System.getenv(CMD_TTL_ENV_VAR))
      .map(Integer::new)
      .orElse(20000);

  static LocalDateTime parseFileDate(String dateStr) {
    return LocalDateTime.parse(dateStr, MEGA_FILE_DATE_TIME_FORMATTER);
  }

  static void handleResult(int code) {
    int posixExitStatus = Optional.ofNullable(code)
            .map(Math::abs)
            .orElse(-1);
    switch (posixExitStatus) {
      case 0:
        //Its Ok
        break;
      case 51:  //Win
      case 2:   //Unix
        throw new MegaWrongArgumentsException();
      case 12:
        throw new MegaResourceAlreadyExistsException();
      case 52:
        throw new MegaInvalidEmailException();
      case 53:
        throw new MegaResourceNotFoundException();
      case 54:
        throw new MegaInvalidStateException();
      case 55:
        throw new MegaInvalidTypeException();
      case 56:
        throw new MegaOperationNotAllowedException();
      case 57:
        throw new MegaLoginRequiredException();
      case 58:
        throw new MegaNodesNotFetchedException();
      case 59:
        throw new MegaUnexpectedFailureException();
      case 60:
        throw new MegaConfirmationRequiredException();
      default:
        throw new MegaUnexpectedFailureException(code);
    }
  }

  static String[] convertInstructionsToExecParams(String cmdInstructions){
    return cmdInstructions.split("\\s+");
  }

  static int execCmd(String... cmd)
      throws java.io.IOException, InterruptedException {
    final Process process = Runtime.getRuntime().exec(cmd);

    Boolean succeeded = process.waitFor(MEGA_TTL, TimeUnit.MILLISECONDS);

    return succeeded ? process.exitValue() : -1;
  }

  static List<String> handleCmdWithOutput(String... cmd)
      throws java.io.IOException {
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.redirectErrorStream(true);

    final Process process = pb.start();

    final Scanner inputScanner = new Scanner(process.getInputStream())
        .useDelimiter(System.getProperty("line.separator"));

    final List<String> result = new ArrayList<>();

    while (inputScanner.hasNext()) {
      result.add(inputScanner.next());
    }

    process.destroy();

    try {
      handleResult(process.waitFor());
    } catch (InterruptedException ex) {
      throw new MegaIOException(
          "The execution of '%s' was interrupted", cmd
      );
    }

    return result;
  }

  static String execCmdWithSingleOutput(String... cmd) throws IOException {
    try{
      return handleCmdWithOutput(cmd).get(0);
    }catch (IndexOutOfBoundsException ex){
      return "";
    }
  }

  static boolean isEmail(String token) {
    return EMAIL_PATTERN.matcher(token).find();
  }

  static boolean isDirectory(String token) {
    return !isEmail(token) && DIRECTORY_PATTERN.matcher(token).find();
  }
}

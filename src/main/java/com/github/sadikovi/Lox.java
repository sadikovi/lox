package com.github.sadikovi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Lox programming language entry point.
 */
public class Lox {
  private static boolean hadError;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  /** Executes a source file */
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    if (hadError) System.exit(64);
  }

  /** Runs REPL */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    while (true) {
      System.out.print("> ");
      run(reader.readLine());
      hadError = false;
    }
  }

  /** Runs command */
  private static void run(String command) {
    Scanner scanner = new Scanner(command);
    List<Token> tokens = scanner.getTokens();
    for (Token token : tokens) {
      System.out.print("[" + token + "] ");
    }
    System.out.println();
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}

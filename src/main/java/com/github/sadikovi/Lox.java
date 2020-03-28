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
  private static Interpreter interpreter = new Interpreter();
  private static boolean hadError = false;
  private static boolean hadRuntimeError = false;

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
    if (hadRuntimeError) System.exit(70);
  }

  /** Runs REPL */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    while (true) {
      try {
        System.out.print("> ");
        run(reader.readLine());
        hadError = false;
      } catch (Exception err) {
        // Exit on Ctrl-D
        error(-1, err.toString());
        break;
      }
    }
  }

  /** Runs command */
  private static void run(String command) {
    Scanner scanner = new Scanner(command);
    List<Token> tokens = scanner.getTokens();

    System.out.println("== Tokens ==");
    for (Token token : tokens) {
      System.out.print("[" + token + "] ");
    }
    System.out.println();

    System.out.println("== AST ==");

    Parser parser = new Parser(tokens);
    Expr expression = parser.parse();

    if (hadError) return;

    // If the tree was correct, print the expression
    System.out.println(new AstPrinter().print(expression));

    System.out.println("== Eval ==");

    // Evaluate expression
    interpreter.interpret(expression);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

  static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  static void report(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at the end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
}

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
    run(new String(bytes, Charset.defaultCharset()), false);
    if (hadError) System.exit(64);
    if (hadRuntimeError) System.exit(70);
  }

  /** Runs REPL */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    while (true) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) {
        // Ctrl-D or EOL
        System.out.println("Bye!");
        break;
      }
      run(line, true);
      hadError = false;
    }
  }

  /** Runs command */
  private static void run(String command, boolean printExpressions) {
    Scanner scanner = new Scanner(command);
    List<Token> tokens = scanner.getTokens();

    System.out.println("\n== Tokens ==");
    for (Token token : tokens) {
      System.out.print("[" + token + "] ");
    }
    System.out.println();

    System.out.println("\n== AST ==");

    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    if (hadError) return;

    // If the tree was correct, print the expression
    System.out.println(new AstPrinter().print(statements));

    System.out.println("== Resolve ==");

    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);

    if (hadError) return;

    System.out.println("== Eval ==");

    // Evaluate statements
    // If `printExpressions` is true, convert all expressions into print statements, i.e.
    // evaluates expressions and prints the result.
    interpreter.interpret(statements, printExpressions);
  }

  static void error(Token token, String message) {
    report(token, message);
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

package com.github.sadikovi;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static com.github.sadikovi.TokenType.*;

/**
 * Parser for Lox grammar.
 *
 * program        -> declaration* EOF ;
 * declaration    -> funDecl
 *                | varDecl
 *                | statement ;
 * varDecl        -> "var" IDENTIFIER ( "=" expression )? ";" ;
 * statement      -> exprStmt
 *                | forStmt
 *                | ifStmt
 *                | printStmt
 *                | returnStmt
 *                | whileStmt
 *                | breakStmt
 *                | block ;
 * exprStmt       -> expression ";" ;
 * printStmt      -> "print" expression ";" ;
 * block          -> "{" declaration* "}" ;
 * ifStmt         -> "if" "(" expression ")" statement ("else" statement)? ;
 * whileStmt      -> "while" "(" expression ")" statement | breakStmt ;
 * forStmt        -> "for" "(" ( varDecl | exprStmt | ";" )
 *                      expression? ";"
 *                      expression? ")" statement | breakStmt ;
 * breakStmt      -> "break" ";" ;
 * funDecl        -> "fun" function
 * function       -> IDENTIFIER "(" parameters? ")" "{" block "}" ;
 * parameters     -> IDENTIFIER ( "," IDENTIFIER )* ;
 * returnStmt     -> "return" expression? ";" ;
 *
 * expression     -> assignment ;
 * assignment     -> IDENTIFIER "=" assignment
 *                | logic_or ;
 * logic_or       -> logic_and ( "or" logic_and )* ;
 * logic_and      -> equality ( "and" equality )* ;
 * equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
 * addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
 * multiplication -> unary ( ( "/" | "*" ) unary )* ;
 * unary          -> ( "!" | "-" ) unary
 *                | call
 *                | primary ;
 * call           -> primary ( "(" arguments? ")" )* ;
 * primary        -> NUMBER | STRING | "false" | "true" | "nil"
 *                | "(" expression ")"
 *                | IDENTIFIER ;
 * arguments      -> expression ( "," expresssion )* ;
 */
class Parser {
  public static class ParseError extends RuntimeException { }

  private final List<Token> tokens;
  private int current;
  private int loopDepth; // flag to indicate the loop (while or for) depth for "break"

  Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.current = 0;
    this.loopDepth = 0;
  }

  public List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<Stmt>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    return statements;
  }

  /** Returns false if there are no tokens left or we have reached the end */
  private boolean isAtEnd() {
    return current >= tokens.size() || peek().type == EOF;
  }

  /** Returns the current token */
  private Token peek() {
    return tokens.get(current);
  }

  /** Returns true if current token has one of the input types */
  private boolean check(TokenType... types) {
    if (isAtEnd()) return false;
    for (TokenType type : types) {
      if (peek().type == type) return true;
    }
    return false;
  }

  /** Advances the current pointer */
  private void advance() {
    current++;
  }

  /** Performs peek and advances pointer */
  private Token peekAndAdvance() {
    Token op = peek();
    advance();
    return op;
  }

  /** Synchronize parser after an error */
  private void synchronize() {
    while (!isAtEnd()) {
      switch (peek().type) {
        case SEMICOLON:
          advance();
          return;
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }
      advance();
    }
  }

  /** Throws a parsing error */
  private ParseError error(Token token, String message) {
    Lox.report(token, message);
    throw new ParseError();
  }

  private Stmt declaration() {
    try {
      if (check(FUN)) {
        advance();
        return function("function");
      }
      if (check(VAR)) {
        advance();
        return varDeclaration();
      }
      return statement();
    } catch (ParseError err) {
      synchronize();
      return null;
    }
  }

  private Stmt function(String kind) {
    if (!check(IDENTIFIER)) throw error(peek(), "Expected " + kind + " name");
    Token name = peekAndAdvance();

    if (!check(LEFT_PAREN)) throw error(peek(), "Expected '(' after " + kind + " name");
    advance();

    List<Token> params = new ArrayList<Token>();
    if (!check(RIGHT_PAREN)) {
      while (true) {
        if (params.size() >= 255) {
          error(peek(), "Cannot have more than 255 parameters");
        }
        if (!check(IDENTIFIER)) throw error(peek(), "Expected parameter name");
        params.add(peekAndAdvance());
        if (!check(COMMA)) break;
        advance();
      }
    }

    if (!check(RIGHT_PAREN)) throw error(peek(), "Expected ')' after " + kind + " parameters");
    advance();

    if (!check(LEFT_BRACE)) throw error(peek(), "Expected '{' before " + kind + " body");
    advance();

    List<Stmt> body = block();

    return new Stmt.Function(name, params, body);
  }

  private Stmt varDeclaration() {
    if (!check(IDENTIFIER)) throw error(peek(), "Expected variable name");
    Token name = peekAndAdvance();
    Expr expression = null;
    if (check(EQUAL)) {
      advance();
      expression = expression();
    }
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after expression");
    advance();
    return new Stmt.Var(name, expression);
  }

  private Stmt statement() {
    if (check(FOR)) {
      advance();
      return forStatement();
    }
    if (check(IF)) {
      advance();
      return ifStatement();
    }
    if (check(PRINT)) {
      advance();
      return printStatement();
    }
    if (check(RETURN)) {
      return returnStatement();
    }
    if (check(WHILE)) {
      advance();
      return whileStatement();
    }
    if (check(BREAK)) {
      advance();
      return breakStatement();
    }
    if (check(LEFT_BRACE)) {
      advance();
      return new Stmt.Block(block());
    }
    return expressionStatement();
  }

  private Stmt forStatement() {
    if (!check(LEFT_PAREN)) throw error(peek(), "Expected '(' after 'for'");
    advance();

    Stmt initializer;
    if (check(SEMICOLON)) {
      advance();
      initializer = null;
    } else if (check(VAR)) {
      advance();
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    if (!check(SEMICOLON)) throw error(peek(), "Expeced ';' after 'for' condition");
    advance();

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    if (!check(RIGHT_PAREN)) throw error(peek(), "Expeced ')' after 'for' clause");
    advance();

    Stmt body = statementWithBreak();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
    }

    if (condition == null) {
      condition = new Expr.Literal(true);
    }
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt ifStatement() {
    if (!check(LEFT_PAREN)) throw error(peek(), "Expected '(' after 'if'");
    advance();
    Expr condition = expression();

    if (!check(RIGHT_PAREN)) throw error(peek(), "Expected ')' after 'if' condition");
    advance();
    Stmt thenBranch = statement();

    Stmt elseBranch = null;
    if (check(ELSE)) {
      advance();
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    Expr expr = expression();
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after expression");
    advance();
    return new Stmt.Print(expr);
  }

  private Stmt whileStatement() {
    if (!check(LEFT_PAREN)) throw error(peek(), "Expected '(' after 'while'");
    advance();
    Expr condition = expression();

    if (!check(RIGHT_PAREN)) throw error(peek(), "Expected ')' after 'while' condition");
    advance();
    Stmt body = statementWithBreak();

    return new Stmt.While(condition, body);
  }

  private Stmt returnStatement() {
    if (!check(RETURN)) throw error(peek(), "Expected 'return' keyword");
    Token keyword = peekAndAdvance();
    Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
    }
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after return value");
    advance();

    return new Stmt.Return(keyword, value);
  }

  /** Returns statement with support of rolling back "containsBreak" flag */
  private Stmt statementWithBreak() {
    loopDepth++;
    try {
      return statement();
    } finally {
      loopDepth--;
    }
  }

  private Stmt breakStatement() {
    if (loopDepth <= 0) throw error(peek(), "'break' outside loop"); // error message from Python
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after 'break'");
    advance();
    return new Stmt.Break();
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after expression");
    advance();
    return new Stmt.Expression(expr);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<Stmt>();
    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }
    if (!check(RIGHT_BRACE)) throw error(peek(), "Expected '}' after block");
    advance();
    return statements;
  }

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = or();

    if (check(EQUAL)) {
      Token equals = peekAndAdvance();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Invalid assignment target");
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (check(OR) && !isAtEnd()) {
      Token operator = peekAndAdvance();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (check(AND) && !isAtEnd()) {
      Token operator = peekAndAdvance();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (check(BANG_EQUAL, EQUAL_EQUAL)) {
      Token op = peekAndAdvance();
      Expr right = comparison();
      expr = new Expr.Binary(expr, op, right);
    }

    return expr;
  }

  private Expr comparison() {
    // addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    Expr expr = addition();

    while (check(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token op = peekAndAdvance();
      Expr right = addition();
      expr = new Expr.Binary(expr, op, right);
    }

    return expr;
  }

  private Expr addition() {
    // multiplication ( ( "-" | "+" ) multiplication )* ;
    Expr expr = multiplication();

    while (check(MINUS, PLUS)) {
      Token op = peekAndAdvance();
      Expr right = multiplication();
      expr = new Expr.Binary(expr, op, right);
    }

    return expr;
  }

  private Expr multiplication() {
    // unary ( ( "/" | "*" ) unary )* ;
    Expr expr = unary();

    while (check(SLASH, STAR)) {
      Token op = peekAndAdvance();
      Expr right = unary();
      expr = new Expr.Binary(expr, op, right);
    }

    return expr;
  }

  private Expr unary() {
    // ( "!" | "-" ) unary | primary
    // also support "+123"
    if (check(BANG, MINUS, PLUS)) {
      Token op = peekAndAdvance();
      return new Expr.Unary(op, unary());
    } else {
      return call();
    }
  }

  private Expr call() {
    Expr expr = primary();
    while (check(LEFT_PAREN) && !isAtEnd()) {
      advance();

      List<Expr> arguments = new ArrayList<Expr>();

      if (!check(RIGHT_PAREN)) {
        // parse arguments
        while (true) {
          if (arguments.size() >= 255) {
            error(peek(), "Cannot have more than 255 arguments");
          }
          arguments.add(expression());
          if (!check(COMMA)) break;
          advance();
        }
      }

      if (!check(RIGHT_PAREN)) throw error(peek(), "Expected ')' after function call");
      Token paren = peekAndAdvance();

      expr = new Expr.Call(expr, paren, arguments);
    }
    return expr;
  }

  private Expr primary() {
    // NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" ;
    if (check(NUMBER, STRING)) return new Expr.Literal(peekAndAdvance().literal);

    if (check(FALSE)) {
      advance();
      return new Expr.Literal(false);
    }

    if (check(TRUE)) {
      advance();
      return new Expr.Literal(true);
    }

    if (check(NIL)) {
      advance();
      return new Expr.Literal(null);
    }

    if (check(IDENTIFIER)) {
      return new Expr.Variable(peekAndAdvance());
    }

    if (check(LEFT_PAREN)) {
      advance();
      Expr expr = expression();
      if (check(RIGHT_PAREN)) {
        advance();
        return new Expr.Grouping(expr);
      } else {
        Token token = peek();
        advance();
        throw error(token, "Expected ')' after expression");
      }
    }
    throw error(peek(), "Expected expression");
  }
}

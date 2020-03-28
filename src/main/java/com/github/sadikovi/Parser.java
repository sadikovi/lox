package com.github.sadikovi;

import java.util.ArrayList;
import java.util.List;

import static com.github.sadikovi.TokenType.*;

/**
 * Parser for Lox grammar.
 *
 * program   -> statement* EOF ;
 * statement -> exprStmt
 *            | printStmt ;
 * exprStmt  -> expression ";" ;
 * printStmt -> "print" expression ";" ;
 *
 * expression     -> equality ;
 * equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
 * addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
 * multiplication -> unary ( ( "/" | "*" ) unary )* ;
 * unary          -> ( "!" | "-" ) unary
 *                | primary ;
 * primary        -> NUMBER | STRING | "false" | "true" | "nil"
 *                | "(" expression ")" ;
 */
class Parser {
  public static class ParseError extends RuntimeException { }

  private final List<Token> tokens;
  private int current;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.current = 0;
  }

  public List<Stmt> parse() {
    try {
      List<Stmt> statements = new ArrayList<Stmt>();
      while (!isAtEnd()) {
        statements.add(statement());
      }
      return statements;
    } catch (ParseError err) {
      return null;
    }
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

  private Stmt statement() {
    if (check(PRINT)) {
      advance();
      return printStatement();
    }
    return expressionStatement();
  }

  private Stmt printStatement() {
    Expr expr = expression();
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after expression");
    advance();
    return new Stmt.Print(expr);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after expression");
    advance();
    return new Stmt.Expression(expr);
  }

  private Expr expression() {
    return equality();
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
      return primary();
    }
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

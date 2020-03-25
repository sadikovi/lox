package com.github.sadikovi;

import java.util.List;

import static com.github.sadikovi.TokenType.*;

/**
 * Parser for Lox grammar.
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
  private final List<Token> tokens;
  private int current;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.current = 0;
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
    if (check(BANG, MINUS)) {
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
        return new Expr.Grouping(expression());
      }
    }
    throw new RuntimeException("Failed to parse primary expression");
  }
}

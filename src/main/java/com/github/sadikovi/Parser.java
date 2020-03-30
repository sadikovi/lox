package com.github.sadikovi;

import java.util.ArrayList;
import java.util.List;

import static com.github.sadikovi.TokenType.*;

/**
 * Parser for Lox grammar.
 *
 * program        -> declaration* EOF ;
 * declaration    -> varDecl
 *                | statement ;
 * varDecl        -> "var" IDENTIFIER ( "=" expression )? ";" ;
 * statement      -> exprStmt
 *                | ifStmt
 *                | printStmt
 *                | block ;
 * exprStmt       -> expression ";" ;
 * printStmt      -> "print" expression ";" ;
 * block          -> "{" declaration* "}" ;
 * ifStmt         -> "if" "(" expression ")" statement ("else" statement)? ;
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
 *                | primary ;
 * primary        -> NUMBER | STRING | "false" | "true" | "nil"
 *                | "(" expression ")"
 *                | IDENTIFIER ;
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
    if (check(IF)) {
      advance();
      return ifStatement();
    }
    if (check(PRINT)) {
      advance();
      return printStatement();
    }
    if (check(LEFT_BRACE)) {
      advance();
      return block();
    }
    return expressionStatement();
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

  private Stmt expressionStatement() {
    Expr expr = expression();
    if (!check(SEMICOLON)) throw error(peek(), "Expected ';' after expression");
    advance();
    return new Stmt.Expression(expr);
  }

  private Stmt block() {
    List<Stmt> statements = new ArrayList<Stmt>();
    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }
    if (!check(RIGHT_BRACE)) throw error(peek(), "Expected '}' after block");
    advance();
    return new Stmt.Block(statements);
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

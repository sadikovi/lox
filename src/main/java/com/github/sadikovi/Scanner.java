package com.github.sadikovi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.sadikovi.TokenType.*;

/**
 * Scanner class that tokenizes the source code.
 */
public class Scanner {
  private static final HashMap<String, TokenType> keywords = new HashMap<String, TokenType>();

  static {
    keywords.put("and", AND);
    keywords.put("class", CLASS);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("fun", FUN);
    keywords.put("for", FOR);
    keywords.put("if", IF);
    keywords.put("nil", NIL);
    keywords.put("or", OR);
    keywords.put("print", PRINT);
    keywords.put("return", RETURN);
    keywords.put("super", SUPER);
    keywords.put("this", THIS);
    keywords.put("true", TRUE);
    keywords.put("var", VAR);
    keywords.put("while", WHILE);
  }

  private final String source;
  private final List<Token> tokens;
  private int start;
  private int current;
  private int line;

  public Scanner(String source) {
    this.source = source;
    this.tokens = new ArrayList<Token>();
    this.start = 0;
    this.current = 0;
    this.line = 1;
  }

  /** Converts source into a list of tokens */
  private void scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme
      start = current;
      scanToken();
    }

    // Add termination token
    tokens.add(new Token(EOF, "", null, line));
  }

  /** Returns true if all characters have been consumed */
  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  /** Matches the current character and advances index only on match */
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;
    current++;
    return true;
  }

  /** Peek the current character without advancing */
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  /** Peeks the next after current without advancing */
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  /** Match a multiline comment */
  private void comments() {
    // search for "*/"
    while (!isAtEnd() && !(peek() == '*' && peekNext() == '/')) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated comment");
      return;
    }

    advance(); // close "*"
    if (!isAtEnd()) advance(); // close "/"
  }

  /** Match a string */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string");
      return;
    }

    advance(); // close '"'

    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  /** Returns true if character is a digit */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /** Match a number */
  private void number() {
    while (isDigit(peek())) advance();

    if (peek() == '.' && isDigit(peekNext())) {
      advance(); // consume the "."
    }

    while (isDigit(peek())) advance();

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  /** Returns true if a character is a letter */
  private boolean isAlpha(char c) {
    return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
  }

  /** Returns true is a character is alphanumeric */
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  /** Match an identifier */
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String lexeme = source.substring(start, current);
    TokenType type = keywords.get(lexeme);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String lexeme = source.substring(start, current);
    tokens.add(new Token(type, lexeme, literal, line));
  }

  private void scanToken() {
    char c = advance();

    switch (c) {
      // Single character tokens
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;

      // One or two character tokens
      case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
      case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;

      // Handle slash/comment
      case '/':
        if (match('/')) {
          // it is a inline comment, skip the line
          while (peek() != '\n' && !isAtEnd()) advance();
        } else if (match('*')) {
          comments();
        } else {
          addToken(SLASH);
        }
        break;

      // Whitespaces
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        line++;
        break;

      // Match string
      case '"': string(); break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character");
        }
        break;
    }
  }

  /**
   * Returns parsed tokens.
   */
  public List<Token> getTokens() {
    if (this.tokens.isEmpty()) {
      scanTokens();
    }
    return this.tokens;
  }
}

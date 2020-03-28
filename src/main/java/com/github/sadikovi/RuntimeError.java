package com.github.sadikovi;

/**
 * Error that is thrown during expression evaluation.
 * See Interpreter.java for usages.
 */
class RuntimeError extends RuntimeException {
  final Token token;

  RuntimeError(Token token, String message) {
    super(message);
    this.token = token;
  }
}

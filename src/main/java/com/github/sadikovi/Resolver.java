package com.github.sadikovi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final LinkedList<Map<String, State>> scopes;
  private final Interpreter interpreter;
  private FunctionType currentFunction = FunctionType.NONE;
  private ClassType currentClass = ClassType.NONE;

  /** Variable scope state, state is added when a variable is declared  */
  private static class State {
    final Token name; // variable name
    boolean defined; // true is variable is defined
    boolean used; // true if variable is referenced in the scope

    State(Token name) {
      this.name = name;
    }

    State markUsed() {
      this.used = true;
      return this;
    }

    State markDefined() {
      this.defined = true;
      return this;
    }
  }

  private enum FunctionType {
    NONE,
    CLASS_METHOD,
    FUNCTION,
    GETTER,
    INITIALIZER,
    METHOD
  }

  private enum ClassType {
    NONE,
    CLASS,
    SUBCLASS
  }

  Resolver(Interpreter interpreter) {
    this.scopes = new LinkedList<Map<String, State>>();
    this.interpreter = interpreter;
  }

  @Override
  public Void visit(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  @Override
  public Void visit(Stmt.Break stmt) {
    return null;
  }

  @Override
  public Void visit(Stmt.Class stmt) {
    ClassType enclosing = currentClass;
    currentClass = ClassType.CLASS;

    declare(stmt.name);
    define(stmt.name);

    if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
      Lox.error(stmt.superclass.name, "A class cannot inherit from itself");
    }

    if (stmt.superclass != null) {
      currentClass = ClassType.SUBCLASS;
      resolve(stmt.superclass);
    }

    if (stmt.superclass != null) {
      beginScope();
      scopes.peek().put("super", new State(stmt.name).markDefined().markUsed());
    }

    beginScope();

    for (Stmt.Function method : stmt.classMethods) {
      FunctionType declaration = FunctionType.CLASS_METHOD;
      resolveFunction(method, declaration);
    }

    scopes.peek().put("this", new State(stmt.name).markDefined().markUsed()); // pass class token

    for (Stmt.Function method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }
      if (method.params == null) {
        declaration = FunctionType.GETTER;
      }
      resolveFunction(method, declaration);
    }

    endScope();

    if (stmt.superclass != null) {
      endScope();
    }

    currentClass = enclosing;

    return null;
  }

  @Override
  public Void visit(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visit(Stmt.Function stmt) {
    // Define and declare so function can recursively refer itself in the function body.
    declare(stmt.name);
    define(stmt.name);

    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  @Override
  public Void visit(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) {
      resolve(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visit(Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visit(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      Lox.error(stmt.keyword, "Cannot return from top-level code");
    }

    if (stmt.value != null) {
      if (currentFunction == FunctionType.INITIALIZER) {
        Lox.error(stmt.keyword, "Cannot return a value from an initializer");
      }
      resolve(stmt.value);
    }
    return null;
  }

  @Override
  public Void visit(Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

  @Override
  public Void visit(Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.expression != null) {
      resolve(stmt.expression);
    }
    define(stmt.name);

    return null;
  }

  @Override
  public Void visit(Expr.Assign expr) {
    resolve(expr.expression);
    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visit(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visit(Expr.Call expr) {
    resolve(expr.callee);
    for (Expr argument : expr.arguments) {
      resolve(argument);
    }
    return null;
  }

  @Override
  public Void visit(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visit(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visit(Expr.Lambda expr) {
    resolveFunction(expr.keyword, expr.params, expr.body, FunctionType.FUNCTION);
    return null;
  }

  @Override
  public Void visit(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visit(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visit(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visit(Expr.Super expr) {
    if (currentClass == ClassType.NONE) {
      Lox.error(expr.keyword, "Cannot use 'super' outside of a class");
    } else if (currentClass != ClassType.SUBCLASS) {
      Lox.error(expr.keyword, "Cannot use 'super' inside of a class with no superclass");
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visit(Expr.This expr) {
    if (currentClass == ClassType.NONE) {
      Lox.error(expr.keyword, "Cannot use 'this' outside of a class");
    }
    if (currentFunction == FunctionType.CLASS_METHOD) {
      Lox.error(expr.keyword, "Cannot use 'this' inside a class method");
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visit(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visit(Expr.Variable expr) {
    if (!scopes.isEmpty()) {
      State state = scopes.peek().get(expr.name.lexeme);
      if (state != null && !state.defined) {
        Lox.error(expr.name, "Cannot read local variable in its own initializer");
      }
    }
    resolveLocal(expr, expr.name);
    return null;
  }

  // Helper functions.

  public void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  private void resolve(Stmt statement) {
    statement.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = 0; i < scopes.size(); i++) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, i);
        // Mark as used because we have encountered the variable
        scopes.get(i).get(name.lexeme).markUsed();
        return;
      }
    }
    // Not found. Assume it is global.
  }

  private void resolveFunction(Stmt.Function function, FunctionType type) {
    resolveFunction(function.name, function.params, function.body, type);
  }

  private void resolveFunction(Token name, List<Token> params, List<Stmt> body, FunctionType type) {
    FunctionType enclosing = currentFunction;
    currentFunction = type;
    try {
      beginScope();

      if (params == null && currentFunction != FunctionType.GETTER) {
        Lox.error(name, "Is not a getter method");
      }

      if (params != null) {
        for (Token param : params) {
          declare(param);
          define(param);
        }
      }
      resolve(body);
      endScope();
    } finally {
      currentFunction = enclosing;
    }
  }

  private void beginScope() {
    scopes.push(new HashMap<String, State>());
  }

  private void endScope() {
    Map<String, State> scope = scopes.pop();
    for (String name : scope.keySet()) {
      if (!scope.get(name).used) {
        Lox.error(scope.get(name).name, "Variable '" + name + "' is never used");
      }
    }
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    Map<String, State> scope = scopes.peek();
    if (scope.containsKey(name.lexeme)) {
      Lox.error(name, "Variable with this name was already declared in this scope");
    }
    scope.put(name.lexeme, new State(name));
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    State state = scopes.peek().get(name.lexeme);
    if (state != null) {
      state.markDefined();
    }
    // Otherwise the variable is not declared.
  }
}

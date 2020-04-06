package com.github.sadikovi;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable, LoxGetter {
  final String name;
  final Map<String, LoxFunction> methods;
  final Map<String, LoxFunction> classMethods;

  LoxClass(String name, Map<String, LoxFunction> methods, Map<String, LoxFunction> classMethods) {
    this.name = name;
    this.methods = methods;
    this.classMethods = classMethods;
  }

  public LoxFunction findClassMethod(String name) {
    return classMethods.get(name);
  }

  public LoxFunction findMethod(String name) {
    return methods.get(name);
  }

  @Override
  public Object get(Token name) {
    if (classMethods.containsKey(name.lexeme)) {
      return classMethods.get(name.lexeme);
    }

    throw new RuntimeError(name, "Undefined class method '" + name.lexeme + "'");
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init");
    if (initializer == null) return 0;
    return initializer.arity();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);
    LoxFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }

  @Override
  public String toString() {
    return "<class " + name + ">";
  }
}

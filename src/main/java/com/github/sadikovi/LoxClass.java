package com.github.sadikovi;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {
  final String name;
  final Map<String, LoxFunction> methods;

  LoxClass(String name, Map<String, LoxFunction> methods) {
    this.name = name;
    this.methods = methods;
  }

  public LoxFunction findMethod(String name) {
    return methods.get(name);
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);
    return instance;
  }

  @Override
  public String toString() {
    return "<class " + name + ">";
  }
}

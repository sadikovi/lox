package com.github.sadikovi;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable, LoxGetter {
  final String name;
  final LoxClass superclass;
  final Map<String, LoxFunction> methods;
  final Map<String, LoxFunction> classMethods;

  LoxClass(
      String name,
      LoxClass superclass,
      Map<String, LoxFunction> methods,
      Map<String, LoxFunction> classMethods) {
    this.name = name;
    this.superclass = superclass;
    this.methods = methods;
    this.classMethods = classMethods;
  }

  public LoxFunction findClassMethod(String name) {
    if (classMethods.containsKey(name)) {
      return classMethods.get(name);
    }
    if (superclass != null) {
      return superclass.findClassMethod(name);
    }
    return null;
  }

  public LoxFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }
    if (superclass != null) {
      return superclass.findMethod(name);
    }
    return null;
  }

  @Override
  public Object get(Token name) {
    LoxFunction method = findClassMethod(name.lexeme);
    if (method == null) {
      throw new RuntimeError(name, "Undefined class method '" + name.lexeme + "'");
    }
    return method;
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

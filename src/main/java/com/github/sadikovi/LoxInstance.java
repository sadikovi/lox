package com.github.sadikovi;

import java.util.HashMap;
import java.util.Map;

class LoxInstance implements LoxGetter {
  final LoxClass klass;
  final Map<String, Object> fields = new HashMap<String, Object>();

  LoxInstance(LoxClass klass) {
    this.klass = klass;
  }

  @Override
  public Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    LoxFunction method = klass.findMethod(name.lexeme);
    if (method != null) return method.bind(this);

    throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'");
  }

  public void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return klass.toString() + " instance";
  }
}

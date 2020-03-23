package com.github.sadikovi;

import java.util.ArrayList;
import java.util.List;

/**
 * Scanner class that tokenizes the source code.
 */
public class Scanner {
  private final String source;

  public Scanner(String source) {
    this.source = source;
  }

  public List<String> scanTokens() {
    String[] tokens = this.source.split(" ");
    List<String> res = new ArrayList<String>();
    for (String token : tokens) {
      res.add(token);
    }
    return res;
  }
}

#ifndef clox_line_h
#define clox_line_h

typedef struct {
  int count;
  int capacity;
  int currentValue; // current RLE value
  int currentCount; // current RLE count for current RLE value, 0 means uninitialised
  int* data;
} LineArray;

void initLineArray(LineArray* lines);
void freeLineArray(LineArray* lines);
// Writes line number.
void writeLine(LineArray* lines, int line);
// Gets line number for an index, returns -1 if index is invalid.
int getLine(LineArray* lines, int index);

#endif

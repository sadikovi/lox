#include <stdlib.h>

#include "line.h"
#include "memory.h"

void initLineArray(LineArray* lines) {
  lines->count = 0;
  lines->capacity = 0;
  lines->currentValue = 0;
  lines->currentCount = 0; // uninitialised
  lines->data = NULL;
}

void freeLineArray(LineArray* lines) {
  FREE_ARRAY(int, lines->data, lines->capacity);
  initLineArray(lines);
}

// Writes line number.
void writeLine(LineArray* lines, int line) {
  // We use top 8 bits to write count and other 24 bytes to write the line number.
  // Line number must be less than "1<<24".
  // TODO: add error when line number is greater than or equal to "1<<24".

  if (lines->capacity < lines->count + 1) {
    int oldCapacity = lines->capacity;
    lines->capacity = GROW_CAPACITY(oldCapacity);
    lines->data = GROW_ARRAY(lines->data, int, oldCapacity, lines->capacity);
  }

  if (lines->currentCount == 255 || (lines->currentCount > 0 && lines->currentValue != line)) {
    // flush values
    int payload = (lines->currentCount << 24) | lines->currentValue;
    lines->data[lines->count] = payload;
    lines->count++;
    // Reset current count
    lines->currentCount = 0;
  }
  lines->currentValue = line;
  lines->currentCount++;
}

// Gets line number for an index.
int getLine(LineArray* lines, int index) {
  int start = 0;
  for (int i = 0; i < lines->count; i++) {
    int payload = lines->data[i];
    int count = (payload >> 24) & 0xff;
    int value = payload & 0xffffff;
    start += count;
    if (index < start) return value;
  }

  // If we did not find index, it must be the current value.
  return lines->currentValue;
}

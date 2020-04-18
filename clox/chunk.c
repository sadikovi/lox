#include <stdlib.h>

#include "chunk.h"
#include "memory.h"

void initChunk(Chunk* chunk) {
  chunk->count = 0;
  chunk->capacity = 0;
  chunk->code = NULL;
  initLineArray(&chunk->lines);
  initValueArray(&chunk->constants);
}

void freeChunk(Chunk* chunk) {
  FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
  freeLineArray(&chunk->lines);
  freeValueArray(&chunk->constants);
  initChunk(chunk);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
  if (chunk->capacity < chunk->count + 1) {
    int oldCapacity = chunk->capacity;
    chunk->capacity = GROW_CAPACITY(oldCapacity);
    chunk->code = GROW_ARRAY(chunk->code, uint8_t, oldCapacity, chunk->capacity);
  }

  chunk->code[chunk->count] = byte;
  writeLine(&chunk->lines, line);
  chunk->count++;
}

// Used for OP_CONSTANT instruction.
// Return value is assumed to fit within 1 byte.
int addConstant(Chunk* chunk, Value value) {
  writeValueArray(&chunk->constants, value);
  return chunk->constants.count - 1; // returns it's index, so we can locate the constant
}

// Used for general constants.
// Writes both instruction and it's operand.
// Constant index is written either as 8-bit value or as 32-bit value.
void writeConstant(Chunk* chunk, Value value, int line) {
  // write constant -> get index
  // write index as 3 bytes
  int constant = addConstant(chunk, value);
  if (constant <= UINT8_MAX) {
    // constant index fits within 1 byte
    writeChunk(chunk, OP_CONSTANT, line);
    writeChunk(chunk, constant, line);
  } else {
    // we have to write constant index as 32-bit number
    writeChunk(chunk, OP_CONSTANT_LONG, line);
    writeChunk(chunk, constant & 0xff, line);
    writeChunk(chunk, (constant >> 8) & 0xff, line);
    writeChunk(chunk, (constant >> 16) & 0xff, line);
    writeChunk(chunk, (constant >> 24) & 0xff, line);
  }
}

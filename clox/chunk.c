#include <stdlib.h>

#include "chunk.h"
#include "memory.h"

void initChunk(Chunk* chunk) {
  chunk->count = 0;
  chunk->capacity = 0;
  chunk->code = NULL;
  chunk->lines = NULL;
  initValueArray(&chunk->constants);
}

void freeChunk(Chunk* chunk) {
  FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
  FREE_ARRAY(int, chunk->lines, chunk->capacity);
  freeValueArray(&chunk->constants);
  initChunk(chunk);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
  if (chunk->capacity < chunk->count + 1) {
    int oldCapacity = chunk->capacity;
    chunk->capacity = GROW_CAPACITY(oldCapacity);
    chunk->code = GROW_ARRAY(chunk->code, uint8_t, oldCapacity, chunk->capacity);
    chunk->lines = GROW_ARRAY(chunk->lines, int, oldCapacity, chunk->capacity);
  }

  chunk->code[chunk->count] = byte;
  chunk->lines[chunk->count] = line;
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
// Constant index is written either as 8-bit value or as 24-bit value.
void writeConstant(Chunk* chunk, Value value, int line) {
  // write constant -> get index
  // write index as 3 bytes
  int constant = addConstant(chunk, value);
  if (constant <= 255) {
    // constant index fits within 1 byte
    writeChunk(chunk, OP_CONSTANT, line);
    writeChunk(chunk, constant, line);
  } else {
    // we have to write constant index as 24-bit number
    writeChunk(chunk, OP_CONSTANT_LONG, line);
    writeChunk(chunk, constant & 0x7, line);
    writeChunk(chunk, (constant >> 8) & 0x7, line);
    writeChunk(chunk, (constant >> 16) & 0x7, line);
  }
}

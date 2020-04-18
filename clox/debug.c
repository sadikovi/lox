#include <stdio.h>

#include "debug.h"
#include "line.h"
#include "value.h"

void disassembleChunk(Chunk* chunk, const char* name) {
  printf("== %s ==\n", name);

  for (int offset = 0; offset < chunk->count;) {
    offset = disassembleInstruction(chunk, offset);
  }
}

static int constantInstruction(const char* name, uint8_t instruction, Chunk* chunk, int offset) {
  int constant; // index for a constant value
  int newOffset; // offset to return

  switch (instruction) {
    case OP_CONSTANT_LONG:
      constant =
        (chunk->code[offset + 1]) +
        (chunk->code[offset + 2] << 8) +
        (chunk->code[offset + 3] << 16) +
        (chunk->code[offset + 3] << 24);
      newOffset = offset + 4;
      break;
    default: // OP_CONSTANT
      constant = chunk->code[offset + 1]; // get the next value after the opcode
      newOffset = offset + 2;
      break;
  }

  printf("%-16s %4d '", name, constant);
  printValue(chunk->constants.values[constant]);
  printf("'\n");
  return newOffset;
}

static int simpleInstruction(const char* name, int offset) {
  printf("%s\n", name);
  return offset + 1;
}

int disassembleInstruction(Chunk* chunk, int offset) {
  printf("%04d ", offset);
  if (offset > 0 && getLine(&chunk->lines, offset) == getLine(&chunk->lines, offset - 1)) {
    printf("   | ");
  } else {
    printf("%04d ", getLine(&chunk->lines, offset));
  }

  uint8_t instruction = chunk->code[offset];
  switch (instruction) {
    case OP_CONSTANT:
      return constantInstruction("OP_CONSTANT", instruction, chunk, offset);
    case OP_CONSTANT_LONG:
      return constantInstruction("OP_CONSTANT_LONG", instruction, chunk, offset);
    case OP_ADD:
      return simpleInstruction("OP_ADD", offset);
    case OP_SUBTRACT:
      return simpleInstruction("OP_SUBTRACT", offset);
    case OP_MULTIPLY:
      return simpleInstruction("OP_MULTIPLY", offset);
    case OP_DIVIDE:
      return simpleInstruction("OP_DIVIDE", offset);
    case OP_NEGATE:
      return simpleInstruction("OP_NEGATE", offset);
    case OP_RETURN:
      return simpleInstruction("OP_RETURN", offset);
    default:
      printf("Unknown opcode %d\n", instruction);
      return offset + 1;
  }
}

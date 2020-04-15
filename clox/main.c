#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, const char* argv[]) {
  initVM();

  Chunk chunk;
  initChunk(&chunk);

  // int constant = addConstant(&chunk, 1.2);
  // writeChunk(&chunk, OP_CONSTANT, 123);
  // writeChunk(&chunk, constant, 123);

  // - (1.2 + 3.4 / 5.6)
  // writeConstant(&chunk, 1.2, 123);
  // writeConstant(&chunk, 3.4, 123);
  // writeChunk(&chunk, OP_ADD, 123);
  // writeConstant(&chunk, 5.6, 124);
  // writeChunk(&chunk, OP_DIVIDE, 124);
  // writeChunk(&chunk, OP_NEGATE, 124);
  // writeChunk(&chunk, OP_RETURN, 125);

  // 1 + 2 * 3 - 4 / -5
  writeConstant(&chunk, 1, 123);
  writeConstant(&chunk, 2, 123);
  writeChunk(&chunk, OP_ADD, 123);
  writeConstant(&chunk, 3, 123);
  writeChunk(&chunk, OP_MULTIPLY, 123);
  writeConstant(&chunk, 4, 123);
  writeChunk(&chunk, OP_SUBTRACT, 123);
  writeConstant(&chunk, 5, 123);
  writeChunk(&chunk, OP_NEGATE, 123);
  writeChunk(&chunk, OP_DIVIDE, 123);
  writeChunk(&chunk, OP_RETURN, 124);

  // disassembleChunk(&chunk, "test chunk");

  interpret(&chunk);

  freeVM();

  freeChunk(&chunk);

  return 0;
}

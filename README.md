# CPSC 457 Assignment 5 - Concurrency with Java Threads

**Student:** Eyuel Kahsay  
**Course:** CPSC 457 - Fall 2025  
**Due Date:** November 21st, 2025 @ 11:59 PM

## Description
Implementation of two concurrent Java programs:

### Part 1: Tic-Tac-Toe
- Three threads: Main thread + 2 player threads
- Shared game board with turn-based synchronization
- Random move generation with race condition handling

### Part 2: Parallel Prime Number Finder
- Multi-threaded prime number search
- Concurrent writes to shared buffer
- Thread-safe ArrayList operations

## Compilation and Execution

### Part 1:
```bash
javac TicTacToe.java
java TicTacToe
```

### Part 2:
```bash
javac PrimeFinder.java
java PrimeFinder <LOWER> <UPPER> <N-THREADS>
```

## Repository
https://github.com/Eyu108/cpsc457_a5

/**
 * CPSC 457 Fall 2025 - Assignment 5 Part 1
 * Tic-tac-toe game usng three threads.
 * 
 * Main thread: Manages the board, displays it, checks for winner/draw, and controls game flow
 * Player threads (2): Take turns placing X and O randomly on the board
 * 
 * Synchronization: Uses a turn variable (0 = main thread, 1 = player 1, 2 = player 2)
 */

import java.util.Random;

/**
 * Shared class containing the game board and turn variable.
 * This object is shared between all three threads.
 */
class Shared {
    char[][] board;      // 3x3 game board
    int turn;           // 0 = main thread, 1 = player 1 (X), 2 = player 2 (O)
    
    /**
     * Constructor initializes the board with '-' symbols
     */
    public Shared() {
        board = new char[3][3];
        turn = 0;
        initializeBoard();
    }
    
    /**
     * Initialize all cells of the board to '-'
     */
    public void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
    }
}

/**
 * Player thread class. Each player randomly places their symbol on the board.
 */
class PlayerThread extends Thread {
    private Shared shared;      // Reference to shared game state
    private int id;            // Player ID (1 or 2)
    private char symbol;       // Player symbol ('X' or 'O')
    private Random random;     // Random number generator for moves
    
    /**
     * Constructor for PlayerThread
     * @param shared The shared game state
     * @param id Player ID (1 for X, 2 for O)
     */
    public PlayerThread(Shared shared, int id) {
        this.shared = shared;
        this.id = id;
        this.symbol = (id == 1) ? 'X' : 'O';
        this.random = new Random();
    }
    
    /**
     * Randomly decide on an empty cell for the next move
     * @return The cell number (0-8) for the move
     */
    private int decideMove() {
        int cell;
        int row, col;
        
        // Keep generating random cells until we find an empty one
        do {
            cell = random.nextInt(9);  // Generate random number 0-8
            row = cell / 3;            // Convert to row
            col = cell % 3;            // Convert to column
        } while (shared.board[row][col] != '-');  // Repeat if cell is taken
        
        return cell;
    }
    
    /**
     * Main execution logic for the player thread
     */
    @Override
    public void run() {
        while (true) {
            // Wait for this player's turn
            while (shared.turn != id && !isInterrupted()) {
                // Busy wait (spin) until it's our turn
                Thread.yield();  // Yield to other threads to avoid hogging CPU
            }
            
            // If interrupted, exit the thread
            if (isInterrupted()) {
                break;
            }
            
            // Decide on a move (find an empty cell)
            int cell = decideMove();
            int row = cell / 3;
            int col = cell % 3;
            
            // Place the symbol on the board
            shared.board[row][col] = symbol;
            
            // Give turn back to main thread
            shared.turn = 0;
        }
    }
}

/**
 * Main TicTacToe class
 */
public class TicTacToe {
    
    /**
     * Display the game board on standard output
     * @param shared The shared game state
     */
    private static void displayBoard(Shared shared) {
        System.out.println("-------------");
        for (int i = 0; i < 3; i++) {
            System.out.print("| ");
            for (int j = 0; j < 3; j++) {
                System.out.print(shared.board[i][j] + " | ");
            }
            System.out.println();
            System.out.println("-------------");
        }
    }
    
    /**
     * Check if there is a winer or draw
     * @param shared The shared game state
     * @return 0 = no winner yet, 1 = player 1 wins, 2 = player 2 wins, 3 = draw
     */
    private static int checkWinner(Shared shared) {
        char[][] board = shared.board;
        
        // Check rows for a winner
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != '-' && 
                board[i][0] == board[i][1] && 
                board[i][1] == board[i][2]) {
                return (board[i][0] == 'X') ? 1 : 2;
            }
        }
        
        // Check columns for a winner
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != '-' && 
                board[0][j] == board[1][j] && 
                board[1][j] == board[2][j]) {
                return (board[0][j] == 'X') ? 1 : 2;
            }
        }
        
        // Check diagonal (top-left to bottom-right)
        if (board[0][0] != '-' && 
            board[0][0] == board[1][1] && 
            board[1][1] == board[2][2]) {
            return (board[0][0] == 'X') ? 1 : 2;
        }
        
        // Check diagonal (top-right to bottom-left)
        if (board[0][2] != '-' && 
            board[0][2] == board[1][1] && 
            board[1][1] == board[2][0]) {
            return (board[0][2] == 'X') ? 1 : 2;
        }
        
        // Check for draw (board is full)
        boolean isFull = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '-') {
                    isFull = false;
                    break;
                }
            }
            if (!isFull) break;
        }
        
        if (isFull) {
            return 3;  // Draw
        }
        
        return 0;  // No winner yet
    }
    
    /**
     * Main method - creates threads and manages the game
     */
    public static void main(String[] args) {
        // Create shared game state
        Shared shared = new Shared();
        
        // Initialize the board (already done in constructor, but explicit here)
        shared.turn = 0;
        
        // Create two player threads
        PlayerThread player1 = new PlayerThread(shared, 1);  // Player X
        PlayerThread player2 = new PlayerThread(shared, 2);  // Player O
        
        // Start both player threads
        player1.start();
        player2.start();
        
        // Display initial board
        System.out.println("Initial Board:");
        displayBoard(shared);
        
        // Main game loop
        int currentPlayer = 1;  // Start with player 1 (X)
        
        while (true) {
            // Wait until turn becomes 0 (main thread's turn)
            while (shared.turn != 0) {
                Thread.yield();  // Yield to other threads
            }
            
            // Display the board
            if (currentPlayer == 1) {
                System.out.println("Player X's Turn:");
            } else {
                System.out.println("Player O's Turn:");
            }
            displayBoard(shared);
            
            // Check for winner or draw
            int result = checkWinner(shared);
            
            if (result == 1) {
                // Player 1 (X) wins
                System.out.println("WINNER: Player X wins! :D");
                player1.interrupt();
                player2.interrupt();
                break;
            } else if (result == 2) {
                // Player 2 (O) wins
                System.out.println("WINNER: Player O wins! :D");
                player1.interrupt();
                player2.interrupt();
                break;
            } else if (result == 3) {
                // Draw
                System.out.println("DRAW: It's a tie!");
                player1.interrupt();
                player2.interrupt();
                break;
            }
            
            // Set turn to next player
            shared.turn = currentPlayer;
            
            // Alternate between players
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }
        
        // Wait for player threads to finish
        try {
            player1.join();
            player2.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted while waiting for players to finish.");
        }
        
        // Game ends
        System.exit(0);
    }
}
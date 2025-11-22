/**
 * CPSC 457 Fall 2025 - Assignment 5 Part 2
 * Parallel Prime Number Finder using Java threads with concurrent writes to a shared buffer.
 * 
 * This program searches for prime numbers in a given range using multiple threads.
 * Each thread searches its assigned subrange and writes primes directly to a shared ArrayList.
 * Thread safety is ensured using synchronized methods.
 */

import java.util.ArrayList;
import java.util.Collections;

/**
 * Shared class containing the ArrayList for storing prime numbers.
 * Provides thread-safe methods for adding primes to the shared buffer.
 */
class PrimeNumbers {
    private ArrayList<Integer> primeNumbers;
    
    /**
     * Constructor initializes the ArrayList
     */
    public PrimeNumbers() {
        primeNumbers = new ArrayList<>();
    }
    
    /**
     * Thread-safe method to add a prime number to the shared buffer.
     * Synchronized to prevent race conditions when multiple threads
     * try to add primes simultaneously.
     * @param prime The prime number to add
     */
    public synchronized void addPrime(int prime) {
        primeNumbers.add(prime);
    }
    
    /**
     * Get the list of prime numbers
     * @return ArrayList of prime numbers
     */
    public ArrayList<Integer> getPrimes() {
        return primeNumbers;
    }
    
    /**
     * Sort the prime numbers in ascending order
     */
    public void sort() {
        Collections.sort(primeNumbers);
    }
    
    /**
     * Get the count of prime numbers found
     * @return Number of primes in the list
     */
    public int getCount() {
        return primeNumbers.size();
    }
}

/**
 * Worker thread class that searches for primes in a given range
 */
class PrimeWorker extends Thread {
    private int threadId;
    private int minBound;
    private int maxBound;
    private PrimeNumbers sharedBuffer;
    
    /**
     * Constructor for PrimeWorker thread
     * @param threadId The ID of this thread
     * @param minBound Lower bound of search range (inclusive)
     * @param maxBound Upper bound of search range (inclusive)
     * @param sharedBuffer Shared buffer to write prime numbers to
     */
    public PrimeWorker(int threadId, int minBound, int maxBound, PrimeNumbers sharedBuffer) {
        this.threadId = threadId;
        this.minBound = minBound;
        this.maxBound = maxBound;
        this.sharedBuffer = sharedBuffer;
    }
    
    /**
     * Check if a number is prime
     * @param num The number to check
     * @return true if num is prime, false otherwise
     */
    private boolean isPrime(int num) {
        if (num < 2) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Main execution logic: search range and add primes to shared buffer
     */
    @Override
    public void run() {
        // Announce the search range
        System.out.println("Thread " + threadId + " searching range [" + minBound + ", " + maxBound + "]");
        
        // Search the assigned range for prime numbers
        for (int num = minBound; num <= maxBound; num++) {
            if (isPrime(num)) {
                // Immediately write to shared buffer (thread-safe)
                sharedBuffer.addPrime(num);
            }
        }
    }
}

/**
 * Main PrimeFinder class
 */
public class PrimeFinder {
    
    /**
     * Main method - handles argument parsing, thread creation, and output
     */
    public static void main(String[] args) {
        /* Constant variables */
        final int MIN_BOUND_ARG = 0;
        final int MAX_BOUND_ARG = 1;
        final int THREAD_COUNT_ARG = 2;
        final int MIN_ARGS = 3;
        
        /* Variables to hold arguments */
        int min_bound;
        int max_bound;
        int thread_count;
        
        /* Ensure we have a valid number of args. */
        if (args.length < MIN_ARGS) {
            System.out.println("Usage: java PrimeFinder <min> <max> <thread_count>");
            System.exit(0);
        }
        
        /* Parse args. */
        try {
            min_bound = Integer.parseInt(args[MIN_BOUND_ARG]);
            max_bound = Integer.parseInt(args[MAX_BOUND_ARG]);
            thread_count = Integer.parseInt(args[THREAD_COUNT_ARG]);
        } catch (NumberFormatException e) {
            System.out.println("Error: All arguments must be valid integers.");
            System.exit(1);
            return;
        }
        
        /* Error handling: Check for invalid arguments */
        // N < 1 or UPPER < LOWER: Invalid arguments
        if (thread_count < 1 || max_bound < min_bound) {
            System.out.println("Error: Invalid arguments.");
            System.out.println("Thread count must be >= 1 and max must be >= min.");
            System.exit(1);
        }
        
        /* Calculate the range size */
        int range_size = max_bound - min_bound + 1;
        
        /* N > Range: Adjust N so every thread has a non-overlapping subrange of 1 number */
        if (thread_count > range_size) {
            thread_count = range_size;
        }
        
        /* Create shared buffer for prime numbers */
        PrimeNumbers sharedBuffer = new PrimeNumbers();
        
        /* Variables to compute subranges */
        int nums_per_thread = range_size / thread_count;
        int remainder = range_size % thread_count;
        
        /* Create array to hold worker threads */
        PrimeWorker[] workers = new PrimeWorker[thread_count];
        
        /* Create and start worker threads */
        int current_start = min_bound;
        
        for (int thread_index = 0; thread_index < thread_count; thread_index++) {
            int subrange_size = nums_per_thread;
            
            /* Distribute remainder: first N threads get one extra element */
            if (remainder > 0) {
                subrange_size++;
                remainder--;
            }
            
            int subrange_start = current_start;
            int subrange_end = current_start + subrange_size - 1;
            
            /* Create and start the worker thread */
            workers[thread_index] = new PrimeWorker(thread_index, subrange_start, subrange_end, sharedBuffer);
            workers[thread_index].start();
            
            /* Update start position for next thread */
            current_start = subrange_end + 1;
        }
        
        /* Wait for all worker threads to finish */
        for (int i = 0; i < thread_count; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted while waiting for worker " + i);
            }
        }
        
        /* Sort the prime numbers in ascending order */
        sharedBuffer.sort();
        
        /* Print the results */
        System.out.println("Main Thread: All workers finished. Primes found:");
        
        ArrayList<Integer> primes = sharedBuffer.getPrimes();
        for (int prime : primes) {
            System.out.print(prime + " ");
        }
        System.out.println();
        
        /* Print the count of prime numbers found */
        System.out.println("Main Thread: " + sharedBuffer.getCount() + " prime numbers found.");
    }
}
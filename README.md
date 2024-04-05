# Assignment 3

Hunter Herbst  
COP4520  
Spring 2024  

## How to run

### Problem 1

* Compile `javac Problem1.java`
* Run `java Problem1`

### Problem 2

* Compile `javac Problem2.java`
* Run `java Problem2`

## Approach and testing

### Problem 1

The bag of presents is represented as a thread-safe ArrayList of integers, where the integers represent the numbered tag on the present. This list is filled, then randomly shuffled before the servants begin working. This approach is essentially the same as picking presents at random from the bag, but I only have to take the first element from this list to do so. When removed from the bag, a present is added to a ConcurrentLinkedList that uses a lock to ensure only one thread can access/modify the LL at a time. Presents are removed from the LL by processing whatever present is at the top of the pile (the head of the list). Checking if a present is in the LL is done by traversing the LL until either the tag with the corresponding number is found, or the end of the list is reached. Servants do not stop working until both the bag is empty and the head of the LL is null. Output is all written to a file labeled `output.txt`.

### Problem 2

**NOTE: FOR SAKE OF TIME IN THIS ASSIGNMENT, I HAVE PROBLEM 2 RUN ONLY ACROSS A SIMULATED 24 HOUR PERIOD**  
Temperatures highs, lows, and minute high-low pairs are stored in thread-safe Lists created using Java's syncrhonized lists. Eight probes are created and stored in an array. This array is used by a runnable task called ProbeScanner. When the probe scanner is run, it creates a thread for each probe to record the temperature for that minute, and the temperature recorded by each probe is logged and if it is higher or lower than the currently recorded highs and lows for the hour, it is added to the corresponding list. At the end of each hour, the hourly report is written, labeled by which hour it was recorded. The largest change in temperature across a 10-minute period is recorded using the high-low pairs, scanning across a 10 minute section of temperatures and then scanning forward through the hour one minute at a time (This may be hard to understand in reading, the code for this begins on line 128 in `Problem2.java`). After the report is calculated, the hour highs, lows, and high-low pairs are cleared.

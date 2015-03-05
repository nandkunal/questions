*Write some code, that will flatten an array of arbitrarily nested arrays of integers into a flat array of integers. e.g. [[1,2,[3]],4] -> [1,2,3,4]*

I have written this in Java here 
* [Flattener.java](https://github.com/lynchd/questions/blob/master/src/main/java/com/lynchdt/questions/Flattener.java) 	
* [FlattenerTest.java](https://github.com/lynchd/questions/blob/master/src/test/java/com/lynchdt/questions/FlattenerTest.java)

The [repo here](https://github.com/lynchd/questions/) contains my answers to 4 and 5 as well as means to get things running for yourself. 

To run all tests execute 
```
./gradlew test 
```

I have two implementations for flatten, one is recursive and one is iterative. Since I have used Java, recursion with any depth can often result in stack issues. My iterative solution uses a Stack, which is allocated on the heap. There are some tests that use a million integers. These can be enabled to show what happens when stack space exhausts. There is another test that uses a cycle to exhaust the JVM available heap.

There are two obvious improvements I could make to this code. A nice rewrite would be to generalise it to use any type, not just Integer. Also, I've not considered the case where there are cycles. That is, a nesting level contains a reference to a previous nesting level. One suggestion would to maintain a set of visited nodes to detect a cycle. The cycle can be broken by skipping over the cyclical reference. 


*Write a program, topN, that given an arbitrarily large file and a number, N, containing individual numbers on each line (e.g. 200Gb file), will output the largest N numbers, highest first. Tell me about the run time/space complexity of it, and whether you think there's room for improvement in your approach.*


I have written this in Java [here](https://github.com/lynchd/questions/tree/master/src/main/java/com/lynchdt/questions/topn). There is [a small test text file with 25 numbers](https://github.com/lynchd/questions/blob/master/test.txt) in the root of the project. 

To build..
```
> git clone https://github.com/lynchd/questions.git
> cd questions
> ./gradlew test jar
```
To execute..
```
# Assuming JDK 8 in $JAVA_HOME/$PATH
# Usage is TopN <n> <workerCount> <queueSize> file1 [file2 .. fileM] 
> java -jar build/libs/questions.jar 1 1 1000 test.txt
Top 1 results after about 25 lines [23445]
Top-1 -> [23445]
Complete in 2 sec(s)
```
For some more fun - I have left a [70 million line file on S3 here](https://s3-eu-west-1.amazonaws.com/intercom-dave/numbers.tar.gz). The download is around 1.2Gb

This extracts to 8 files. ```Numbers.all.txt``` contains the full 70 million numbers. ```Numbers.1.txt``` through ```Numbers.7.txt``` contain the same numbers split across 7 files of 10 million lines each. If you want to go a bit harder, [I have a number generator here]( 
https://github.com/lynchd/questions/blob/master/src/main/java/com/lynchdt/questions/topn/NumberFileGenerator.java).
Just tell it how many lines of numbers you want and it will write that many random longs  to STDOUT. 

[Results of some experiments I ran using the bigger files are here.](https://github.com/lynchd/questions/blob/master/results.txt) They show successive runs faster, slower or otherwise as workerCount, queueSize, fileSize and N vary. I've talked about them a bit more below. 

Here is an example run with 3 files 
```
java -jar build/libs/questions.jar 3 3 100000 numbers.1.txt numbers.2.txt numbers.3.txt
Top 3 results after about 111771 lines [9222829471482912576, 9222815328354881768, 9222721952157190536]
Top 3 results after about 2114743 lines [9223367213026329101, 9223372036854775807, 9223372036854775807]
Top 3 results after about 5214259 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 8407845 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 11119119 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 13674432 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 17157792 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 20470957 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 23806150 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 26534160 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top 3 results after about 29487252 lines [9223371376680402307, 9223372036854775807, 9223372036854775807]
Top-3 -> [9223371376680402307, 9223372036854775807, 9223372036854775807]
Complete in 11 sec(s)
```
	
Given time constraints, I have taken some liberties and made some assumptions 
* Text files are decompressed ASCII
* All numbers can fit into 64-bit signed integers (Long)
* Practically, *maximum(N)* is *Integer.MAX_VALUE* - I used Arrays to facilitate top-N management
* Duplicates are acceptable. That is if the 5 top numbers are the same number, they are printed 5 times as a result. 
	
I have implemented a MinHeap that helps with top-N in a few ways. Bounding the size of the heap at N allows us to track the top N
in *O(N)* space. Insertion into a the heap is done in *O(log N)* time, if required. Finding the smallest value in a min heap is an *O(1)* operation, since it is always at the root. If an eviction is required it can also be done in *O(log N)* time, rebuilding the heap after evicting the root. If we allow all numbers to be inserted into the min heap until it is full, then follow the above simple eviction strategy, we will always end up with the top-N numbers that have been previously inserted into the heap. Finally, to establish order, heap-sort is executed in *O(N log N)* time. Heapsort was the obvious choice as a quasi-linear sort, and once you have a heap sorting it is straight forward.

I have treated reading the files as a producer/consumer problem. My solution can read F files concurrently. Numbers are parsed and
placed on a blocking-queue where they are consumed for processing by W workers. These workers each maintain a bounded MinHeap which actually contains partial results of the full scan of all files. Periodically, all top-N's are merged into a single top-N. The final result is obtained when a final merge is done after all files are read, the queue is drained and all workers are finished working. The overall idea is to amplify the performance of the above with parallelism where possible. Test results show that I/O throughput can be improved by scanning more than one file concurrently. Otherwise, the workers are waiting a lot and are bound by I/O. This holds true only for my machine, so number of files as well as worker threads and queue size are variable to assist in tuning. 

There are obvious I/O and CPU trade-offs here. These are encapsulated in the following variables. 
* Number of Files  = F
* Number of Workers = W
* Size of Queue = Q
* Size of N = N
* Number of lines L = L
	
I ran some tests on my 4-core, 8Gb machine using a fixed file of size 70,000,000 - roughly 1.2Gb. When F is greater than 1 it represents the same 70,000,000 split evenly amongst files. This application is primarily I/O bound, however as N increases CPU does become more of a factor. For N = 100,000 with Q = 1,000,000, F = 7 and L = 70,000,000 having 3 vs. 1 worker thread decreases execution time by on average 5 seconds. This is small but worthy of the parallelism. Varying the number of files from 7 to 1 causes a 10 second increase in execution time on average, so there is benefit in splitting the files up. Decreasing Q to 1000 for W = 3 costs and extra 19 seconds, since the queue tends to be full much more. The overarching point is F,W, and Q need to be tuned with respect to L, N and the available compute power and memory. 

This implementation could be improved a number of ways. 
- Support numbers of any size, and type including decimals.

- Support much larger N. This could be done using multiple BoundedHeap instances per worker, or by moving from ArrayList to LinkedList. There is a good bit to rewrite, we get some elegant code that is harder to keep elegant when moving from Array to LinkedList. 

- Needs more code coverage and unit tests. Multi-threaded coded testing is tricky, but important. I've been lazy here with code coverage and would not ship with tests in this shape. 

- Needs metrics! The variables above need tuning for maximum output. Insights into queue-size, worker utilisation and file reading utilisation are required to make smart calls on where bottlenecks lie. StatsD and Graphite would be useful for this - I ran out of time before I could integrate them.

- For large N and L distribute the solution. The solution is bound by a single machine, I/O bus and the limits of memory and cores on a single machine. An obvious improvement is to extend the parallelism off machine. At this point we may not want to roll our own, since Top-N can be done at scale quite nicely as a MapReduce job on e.g. Hadoop.
 


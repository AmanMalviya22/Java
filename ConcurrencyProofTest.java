import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreadBenchmark {

    public static void main(String[] args) {
        int totalTasks = 100_000;
        
        AtomicInteger activeCount = new AtomicInteger(0);
        AtomicInteger maxPeak = new AtomicInteger(0);

        System.out.println("Launching " + totalTasks + " virtual threads...");
        long start = System.currentTimeMillis();

        // Using try-with-resources so the executor automatically blocks until all tasks complete
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < totalTasks; i++) {
                executor.submit(() -> {
                    int currentActive = activeCount.incrementAndGet();
                    
                    // Track the highest number of overlapping active threads
                    maxPeak.accumulateAndGet(currentActive, Math::max);

                    try {
                        // 2 seconds gives the main loop enough time to spin up all threads
                        Thread.sleep(2000); 
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    activeCount.decrementAndGet();
                });
            }
        } 

        long duration = System.currentTimeMillis() - start;

        System.out.println("\nResults:");
        System.out.println("----------------------------");
        System.out.println("Total Spawned  : " + totalTasks);
        System.out.println("Peak Concurrent: " + maxPeak.get());
        System.out.println("Total Duration : " + duration + " ms");
    }
}

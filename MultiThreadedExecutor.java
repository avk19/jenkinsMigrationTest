import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiThreadedExecutor {

    public static void main(String[] args) {
        // Create a ScheduledExecutorService with 1 thread for the scheduled task
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Create an ExecutorService with 5 threads for the task execution
        int numThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Schedule the task to be executed every 5 minutes
        scheduler.scheduleAtFixedRate(() -> {
            // Execute the task using multiple threads
            for (int i = 0; i < numThreads; i++) {
                executorService.execute(new Task());
            }
        }, 0, 5, TimeUnit.MINUTES);

        // Keep the program running to allow the scheduled tasks to execute
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Shut down the executor services when the program is terminated
            scheduler.shutdown();
            executorService.shutdown();
        }
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            // Replace with your task code
            System.out.println(Thread.currentThread().getName() + " executing task...");
            // Sleep for demonstration purposes
            try {
                Thread.sleep(1000); // Sleep for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

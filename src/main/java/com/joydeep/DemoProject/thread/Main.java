package com.joydeep.DemoProject.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class Main {

    /**
     * Enumeration of task types.
     */
    public enum TaskType {
        READ,
        WRITE,
    }

    public interface TaskExecutor {
        /**
         * Submit new task to be queued and executed.
         *
         * @param task Task to be executed by the executor. Must not be null.
         * @return Future for the task asynchronous computation result.
         */
        <T> Future<T> submitTask(Task<T> task) throws InterruptedException;
    }

    /**
     * Representation of computation to be performed by the {@link TaskExecutor}.
     *
     * @param taskUUID   Unique task identifier.
     * @param taskGroup  Task group.
     * @param taskType   Task type.
     * @param taskAction Callable representing task computation and returning the result.
     * @param <T>        Task computation result value type.
     */
    public record Task<T>(
            UUID taskUUID,
            TaskGroup taskGroup,
            TaskType taskType,
            Callable<T> taskAction
    ) {
        public Task {
            if (taskUUID == null || taskGroup == null || taskType == null || taskAction == null) {
                throw new IllegalArgumentException("All parameters must not be null");
            }
        }
    }

    /**
     * Task group.
     *
     * @param groupUUID Unique group identifier.
     */
    public record TaskGroup(
            UUID groupUUID
    ) {
        public TaskGroup {
            if (groupUUID == null) {
                throw new IllegalArgumentException("All parameters must not be null");
            }
        }
    }

    public static class TaskExecutorClass implements TaskExecutor {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

        <T> Future<T> executeTask(Task<T> task) throws InterruptedException {
            System.out.println("Current Tread : " + Thread.currentThread());
            return submitTask(task);
        }

        @Override
        public <T> Future<T> submitTask(Task<T> task) throws InterruptedException {
            Thread.sleep(100);
            Future<T> submit = (Future<T>) executorService.submit(()
                    -> System.out.println("From submitTask & task is : " + task));
            return submit;
        }
    }

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        TaskExecutorClass taskExecutorClass = new TaskExecutorClass();

        UUID uuid1 = getRandomUUID();
        Task task1 = new Task<>(uuid1, new TaskGroup(uuid1), TaskType.valueOf(TaskType.READ.name()), getCallableTask(TaskType.READ.name()));

        UUID uuid2 = getRandomUUID();
        Task task2 = new Task<>(uuid2, new TaskGroup(uuid2), TaskType.valueOf(TaskType.WRITE.name()), getCallableTask(TaskType.WRITE.name()));

        UUID uuid3 = getRandomUUID();
        Task task3 = new Task<>(uuid3, new TaskGroup(uuid3), TaskType.valueOf(TaskType.READ.name()), getCallableTask(TaskType.READ.name()));

        UUID uuid4 = getRandomUUID();
        Task task4 = new Task<>(uuid4, new TaskGroup(uuid4), TaskType.valueOf(TaskType.WRITE.name()), getCallableTask(TaskType.WRITE.name()));

        List<Future> futures = new ArrayList<>();

        Stream.of(task1, task2, task3, task4).forEach(task -> {
            try {
                futures.add(taskExecutorClass.executeTask(task));
            } catch (InterruptedException e) {
                System.out.println("Error occur : " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
        futures.forEach(future -> System.out.println("Tasks : " + future + " status " + future.isDone()));
        taskExecutorClass.executorService.shutdown();
    }

    /**
     * getRandomUUID for UUID generation.
     *
     * @return
     */
    private static UUID getRandomUUID() {
        return UUID.randomUUID();
    }

    /**
     * getCallableTask to make callable type for the thread execution.
     *
     * @param task
     * @return
     */
    private static Callable<String> getCallableTask(String task) {
        return () -> task;
    }
}


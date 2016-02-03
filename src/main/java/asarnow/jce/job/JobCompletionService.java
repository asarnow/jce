package asarnow.jce.job;

import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * (C) 2/2/16 Daniel Asarnow
 */
public class JobCompletionService<V> implements CompletionService<V> {
    private static Logger logger = Logger.getLogger(JobCompletionService.class);
    private final Executor executor;
//    private final AbstractExecutorService aes;
    private final BlockingQueue<Future<V>> completionQueue;

    /**
     * FutureTask extension to enqueue upon completion.
     * Uses put instead of add, will block if the completion queue is full.
     */
    private class BlockingQueueingFuture extends FutureTask<Void> {
        private final Future<V> task;
        BlockingQueueingFuture(RunnableFuture<V> task) {
            super(task, null);
            this.task = task;
        }
        protected void done() {
            try {
                completionQueue.put(task);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    private RunnableFuture<V> newTaskFor(Callable<V> task) {
        return new FutureTask<>(task);
    }

    private RunnableFuture<V> newTaskFor(Runnable task, V result) {
        return new FutureTask<>(task, result);
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and a
     * {@link LinkedBlockingQueue} as a completion queue.
     *
     * @param executor the executor to use
     * @throws NullPointerException if executor is {@code null}
     */
    public JobCompletionService(Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        this.executor = executor;
//        this.aes = (executor instanceof AbstractExecutorService) ?
//                (AbstractExecutorService) executor : null;
        this.completionQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and the supplied queue as its
     * completion queue.
     *
     * @param executor the executor to use
     * @param completionQueue the queue to use as the completion queue
     *        normally one dedicated for use by this service. This
     *        queue is treated as unbounded -- failed attempted
     *        {@code Queue.add} operations for completed taskes cause
     *        them not to be retrievable.
     * @throws NullPointerException if executor or completionQueue are {@code null}
     */
    public JobCompletionService(Executor executor,
                                     BlockingQueue<Future<V>> completionQueue) {
        if (executor == null || completionQueue == null)
            throw new NullPointerException();
        this.executor = executor;
//        this.aes = (executor instanceof AbstractExecutorService) ?
//                (AbstractExecutorService) executor : null;
        this.completionQueue = completionQueue;
    }

    public Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task);
        executor.execute(new BlockingQueueingFuture(f));
        return f;
    }

    public Future<V> submit(Runnable task, V result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task, result);
        executor.execute(new BlockingQueueingFuture(f));
        return f;
    }

    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    public Future<V> poll() {
        return completionQueue.poll();
    }

    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }



}

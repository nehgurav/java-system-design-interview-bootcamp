# Day 02 — Synchronization, Thread Pools & Asynchronous Programming

## 1. Synchronization Fundamentals

### Why do we need synchronization?

When multiple threads access and modify shared mutable data, their operations can interleave. This can cause a **race condition** and incorrect results.

Example:

```java
class Counter {
    int count = 0;

    void increment() {
        count++;
    }
}
```

`count++` is not one indivisible operation. Conceptually, it involves:
1. Read the current value.
2. Add one.
3. Write the new value.

Two threads can read the same old value and overwrite one another's update. This is a **lost update**.

### synchronized

`synchronized` provides:
- **Mutual exclusion:** only one thread at a time can execute code protected by the same monitor.
- **Visibility:** changes made before a thread releases a monitor become visible to a thread that subsequently acquires that same monitor. This is a happens-before relationship.

```java
class BankAccount {
    private int balance;

    public synchronized void deposit(int amount) {
        balance += amount;
    }
}
```

Lock identity matters:

| Method/block | Lock acquired |
|---|---|
| Synchronized instance method | Current object (`this`) |
| `synchronized(this)` block | Current object |
| Static synchronized method | Class object, e.g. `BankAccount.class` |
| `synchronized(lockObject)` block | The specified object |

Two synchronized instance methods on the **same object** use the same monitor and cannot execute concurrently on that object. Different instances have different monitors. A static synchronized method uses the class monitor, not the instance monitor.

### Atomicity of compound operations

`volatile int count` does **not** make `count++` atomic. Volatile provides visibility and ordering guarantees, not mutual exclusion for a read-modify-write operation.

Use:
- `volatile` for a simple shared state/flag where visibility is required and compound atomic updates are not needed.
- `synchronized` when a critical section or multiple related fields must be protected together.
- Atomic classes for supported atomic operations on individual variables.

## 2. volatile vs synchronized vs AtomicInteger

| Feature | volatile | synchronized | AtomicInteger |
|---|---|---|---|
| Visibility | Yes | Yes, through monitor happens-before | Yes |
| Mutual exclusion | No | Yes | No general critical-section lock |
| Atomic increment | No | Yes, when all access uses the same lock | Yes, via atomic operation |
| Typical use | Stop flag / state publication | Protect compound logic and invariants | Counters and single-variable atomic updates |

### AtomicInteger and CAS

```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();
```

Atomic classes support operations such as `incrementAndGet()`, `getAndIncrement()`, and `compareAndSet(expected, update)`.

**CAS (Compare-And-Set)** updates a value only if it still equals the expected value:
1. Read the current value.
2. Attempt to replace it only if it is still the expected value.
3. If another thread changed it, the CAS fails; the operation may retry.

Atomic classes are useful for individual variables. Use a lock when correctness depends on coordinating multiple variables or a larger critical section.

## 3. ReentrantLock

`ReentrantLock` provides explicit lock management and capabilities beyond a basic monitor.

```java
private final ReentrantLock lock = new ReentrantLock();

public void update() {
    lock.lock();
    try {
        // critical section
    } finally {
        lock.unlock();
    }
}
```

Always release an acquired lock in `finally`.

Useful methods:
- `tryLock()`: attempt immediately; do not wait if unavailable.
- `tryLock(timeout, unit)`: wait up to a specified time.
- `lockInterruptibly()`: allow a waiting thread to respond to interruption.
- `newCondition()`: create condition variables; one lock can have multiple conditions.

### Reentrancy

A thread that already owns a reentrant lock can acquire it again without blocking itself. The lock tracks a hold count.

For example: two successful `lock()` calls by the same thread require two corresponding `unlock()` calls before another thread can acquire the lock.

Both Java intrinsic monitors (`synchronized`) and `ReentrantLock` are reentrant.

### Fair vs non-fair locks

- A **fair** `ReentrantLock` favors the longest-waiting thread when granting the lock. It can reduce starvation risk but may reduce throughput.
- A **non-fair** lock may let a newly arriving thread acquire the lock ahead of queued waiters (barging). This often improves throughput but can increase starvation risk.
- Fairness is about lock acquisition preference; it does not guarantee the order in which threads execute on the CPU.

## 4. Throughput vs Latency

| Metric | Meaning | Example |
|---|---|---|
| Throughput | Number of requests completed per unit of time | 500 requests/second |
| Latency | Time taken by an individual request | 100 ms average response time |

For most services, we want high throughput and low latency. They are related, but they are not simple reciprocals in a concurrent system.

When latency rises, investigate before changing configuration:
- API latency percentiles (p50, p95, p99), not only averages.
- Database query time, locks, indexes, and database resource saturation.
- Connection-pool active/idle/waiting counts and acquisition timeouts.
- Application thread-pool utilization and queue depth.
- Downstream service latency, network, CPU, memory, and garbage collection.

**Production caution:** Increasing a connection pool does not automatically improve performance. If the database is saturated, more concurrent connections can increase contention and make latency worse.

## 5. Database Connection Pool vs Application Thread Pool

These are different resources:

- **Application thread pool:** worker threads that execute application tasks.
- **Database connection pool:** reusable database connections borrowed by tasks that need database access.

If all database connections are occupied, another task needing a connection waits for one to be returned, subject to the pool's acquisition timeout. In a synchronous JDBC flow, the calling worker thread is typically blocked while waiting.

A request waiting in an executor's task queue is different: it has not yet been assigned to a worker thread.

## 6. ThreadPoolExecutor — Core Threads, Queue, Maximum Threads

A useful mental model is a support center:
- Worker threads = employees serving requests.
- Work queue = waiting room.
- Core pool size = baseline employees.
- Maximum pool size = maximum employees allowed.

Example:

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                      // corePoolSize
    20,                     // maximumPoolSize
    60,                     // keepAliveTime
    TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(50)
);
```

For the standard `ThreadPoolExecutor` task-submission algorithm:

1. If fewer than `corePoolSize` threads are running, create a worker for the task.
2. Otherwise, try to put the task in the work queue.
3. If the queue is full, try to create another worker, up to `maximumPoolSize`.
4. If neither queuing nor creating a worker is possible, reject the task using the configured rejection handler.

With core = 5, queue capacity = 50, max = 20:
- First 5 tasks can start on core workers.
- The next 50 tasks can be queued if workers remain busy.
- Further tasks can cause up to 15 additional workers to be created.
- If all 20 workers are busy and the queue is full, subsequent tasks are rejected.

Common rejection policies include:
- `AbortPolicy`: throws `RejectedExecutionException` (default).
- `CallerRunsPolicy`: runs the task on the submitting thread, unless the executor is shut down.
- `DiscardPolicy`: silently discards the task.
- `DiscardOldestPolicy`: discards the oldest queued task and retries submission.

**Interview caution:** The queue-first behavior is important. A bounded queue can fill before the executor grows beyond its core pool size. Pool sizing should consider task type, workload, downstream capacity, queueing delay, and resource limits—not just incoming request rate.

## 7. Synchronous vs Asynchronous Execution

- **Synchronous:** the caller waits for the operation to finish before proceeding.
- **Asynchronous:** the caller can continue while the operation is completed separately, often by another thread.

Asynchronous does not always mean fire-and-forget. A caller can receive a future and observe the result or failure later.

For example, order persistence may be required before returning success, while sending a notification may be handled asynchronously—provided the business requirements allow it.

## 8. Spring @Async

Enable asynchronous method execution:

```java
@Configuration
@EnableAsync
public class AsyncConfig {
}
```

Configure an executor rather than relying blindly on a default executor:

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

Use it from a Spring-managed bean:

```java
@Service
public class EmailService {

    @Async("taskExecutor")
    public void sendEmail() {
        // Send email
    }
}
```

Important points:
- `@EnableAsync` enables Spring's async method support.
- `@Async` marks a method for asynchronous invocation.
- The executor controls worker threads and task queuing.
- Spring's default proxy-based mode intercepts calls that pass through the proxy.
- **Self-invocation does not pass through the proxy.** Calling an `@Async` method from another method on the same object therefore does not trigger asynchronous execution in the usual proxy mode. Move the async method to another Spring bean and call that bean.
- Exceptions from `void` async methods cannot be returned to the caller; configure an `AsyncUncaughtExceptionHandler` for handling them. For future-returning methods, observe failures through the future.
- Consider transaction boundaries, retries, idempotency, task rejection, and shutdown behavior.

## 9. CompletableFuture

`CompletableFuture` represents a result that may become available later and supports composing asynchronous stages.

### runAsync vs supplyAsync

```java
CompletableFuture<Void> noResult =
    CompletableFuture.runAsync(() -> doWork());

CompletableFuture<String> withResult =
    CompletableFuture.supplyAsync(() -> fetchName());
```

- `runAsync()`: task does not return a result.
- `supplyAsync()`: task returns a result.
- Without an explicit executor, these async methods generally use `ForkJoinPool.commonPool()`. In server applications, consider supplying a deliberately configured executor.

### allOf and anyOf

```java
CompletableFuture<Customer> customer =
    CompletableFuture.supplyAsync(() -> fetchCustomer(), executor);

CompletableFuture<Product> product =
    CompletableFuture.supplyAsync(() -> fetchProduct(), executor);

CompletableFuture<Void> all =
    CompletableFuture.allOf(customer, product);

all.join();

Customer c = customer.join();
Product p = product.join();
```

- `allOf()` completes when all supplied futures complete. It returns `CompletableFuture<Void>`; retrieve individual results from the original futures.
- `anyOf()` completes when the first supplied future completes (normally or exceptionally). Its result type is `CompletableFuture<Object>`.

### thenApply vs thenCompose

`thenApply()` transforms a completed result:

```java
CompletableFuture<Integer> doubled =
    CompletableFuture.supplyAsync(() -> 10)
        .thenApply(value -> value * 2);
// doubled.join() == 20
```

`thenCompose()` chains another asynchronous operation and flattens the returned future:

```java
CompletableFuture<Integer> doubled =
    CompletableFuture.supplyAsync(() -> 10)
        .thenCompose(value ->
            CompletableFuture.supplyAsync(() -> value * 2)
        );
// doubled.join() == 20
```

If the lambda passed to `thenApply()` returns a `CompletableFuture<Integer>`, the result is nested: `CompletableFuture<CompletableFuture<Integer>>`. `thenCompose()` avoids that nesting.

Memory aid:
- `thenApply` = transform a value.
- `thenCompose` = chain a future-returning operation and flatten.

### Exception handling

| Method | Purpose |
|---|---|
| `exceptionally(fn)` | Recover from an exception and provide a fallback value |
| `handle((result, ex) -> ...)` | Process either success or failure and produce a new result |
| `whenComplete((result, ex) -> ...)` | Observe completion, commonly for logging/metrics, without intentionally transforming the result |

Examples:

```java
CompletableFuture<Integer> recovered =
    future.exceptionally(ex -> 0);

CompletableFuture<Integer> handled =
    future.handle((value, ex) -> ex == null ? value * 2 : 0);

CompletableFuture<Integer> observed =
    future.whenComplete((value, ex) -> {
        if (ex != null) {
            logger.error("Operation failed", ex);
        }
    });
```

Important: `whenComplete()` is for observation, not recovery. If the original future failed, the dependent future normally remains exceptional. If the observation action itself throws, that dependent stage can also complete exceptionally.

## 10. Interview Questions and Short Answers

1. **Why is `volatile count++` not thread-safe?**  
   Because increment is a read-modify-write sequence; volatile does not make the sequence atomic.

2. **What does synchronized provide?**  
   Mutual exclusion and visibility through monitor happens-before guarantees.

3. **What is CAS?**  
   An atomic conditional update: change a value only if it still matches an expected value.

4. **When use AtomicInteger vs synchronized?**  
   AtomicInteger for supported single-variable atomic operations; synchronized for compound logic or multiple related state.

5. **What does reentrant mean?**  
   A thread holding a lock can acquire that same lock again; it must release each acquisition.

6. **What happens when a ThreadPoolExecutor's queue fills?**  
   It may create workers up to maxPoolSize; if it cannot, the rejection handler is invoked.

7. **Does increasing a DB pool always improve performance?**  
   No. It can overload an already saturated database.

8. **Why might @Async execute synchronously?**  
   Self-invocation bypasses the Spring proxy in proxy-based async mode.

9. **What is the difference between thenApply and thenCompose?**  
   thenApply transforms a value; thenCompose chains and flattens a future-returning operation.

10. **How do you handle CompletableFuture failures?**  
    Use exceptionally for fallback, handle for success/failure transformation, and whenComplete for observation.

## 11. Quick Revision Cheat Sheet

- `synchronized` = mutual exclusion + visibility.
- `volatile` = visibility/ordering, not atomic compound updates.
- `AtomicInteger` = atomic single-variable operations, commonly implemented using CAS.
- `ReentrantLock` = explicit lock control; unlock in finally.
- Throughput = requests per unit time; latency = time per request.
- ThreadPoolExecutor order = core workers → queue → extra workers up to max → rejection.
- DB connection pool and application thread pool are separate resources.
- `@EnableAsync` enables async support; `@Async` marks the method.
- Spring proxy self-invocation bypasses `@Async`.
- `supplyAsync` returns a result; `runAsync` does not.
- `allOf` waits for all; `anyOf` completes with the first completed future.
- `thenApply` transforms; `thenCompose` chains and flattens.
- `exceptionally` recovers; `handle` transforms success/failure; `whenComplete` observes.

---

## 12. Practice Questions

1. Two threads increment a shared integer 1,000 times each. Why might the final value be less than 2,000 without synchronization?
2. What lock does a static synchronized method acquire?
3. Why does volatile not make a check-then-act sequence atomic?
4. What happens if a thread calls lock() twice on a ReentrantLock but unlocks only once?
5. What is the difference between a fair and non-fair ReentrantLock?
6. Why does ThreadPoolExecutor queue tasks before growing beyond corePoolSize?
7. What is the difference between waiting in an executor queue and waiting for a database connection?
8. Why can increasing maximumPoolSize or DB connection count reduce performance?
9. Why does self-invocation bypass @Async?
10. When would you choose thenCompose over thenApply?

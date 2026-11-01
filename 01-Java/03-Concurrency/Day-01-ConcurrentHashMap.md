# ConcurrentHashMap — Interview Notes (Java 8+)

> Module 3 — Concurrency  
> Focus: thread-safe map operations, concurrent access, and atomic updates.

## 1. What is ConcurrentHashMap?

`ConcurrentHashMap` is a thread-safe implementation of the `ConcurrentMap` interface. It supports concurrent access without synchronizing the entire map for every operation.

Use it when multiple threads need to read and update a shared map.

## 2. ConcurrentHashMap vs Hashtable

| Feature | ConcurrentHashMap | Hashtable |
|---|---|---|
| Synchronization | Fine-grained coordination; bucket-level locking when needed | Synchronized methods; coarse-grained map-level locking |
| Concurrent writes | Updates to different bins can often proceed concurrently | Competing updates contend on the map's monitor |
| Reads | Retrievals generally do not acquire update locks | Synchronized retrieval methods also acquire the map lock |
| Null keys/values | Not allowed | Not allowed |
| Iterators | Weakly consistent | Fail-fast behavior is generally used by its collection views |

**Interview answer:** ConcurrentHashMap offers better concurrency because it avoids a single map-wide lock for ordinary updates. It uses CAS for certain operations and synchronizes on an individual bin when coordination is needed. Hashtable synchronizes its methods, creating greater contention.

## 3. How does ConcurrentHashMap coordinate updates?

In Java 8 and later:

- CAS (Compare-And-Swap) is used for certain updates, notably insertion into an empty bin.
- When a bin is already occupied, an update may synchronize on the bin's first node while modifying that bin.
- Updates to different bins can often proceed concurrently.
- This is not the old Java 7 segment-locking design. Avoid describing Java 8+ as locking a specific *segment*.

### CAS in brief

CAS is an atomic operation that:
1. Reads the current value.
2. Compares it with an expected value.
3. Replaces it with a new value only if the current value still matches the expected value.
4. Fails if another thread changed the value first; the code can retry or take another path.

## 4. Can reads happen while a write is in progress?

Yes. Retrievals generally do not acquire the locks used by updates, so readers can usually proceed while another thread updates the map.

For example:
- Thread A updates bin 5.
- Thread B updates bin 5 and may have to wait for coordination on that bin.
- Thread C updates bin 8 and can often proceed independently.
- Thread D reads from the map and generally does not wait for the update lock.

A concurrent read may observe the state before or after a concurrent update, depending on timing. Do not assume that every read waits for an in-progress write.

## 5. Weakly consistent iterators

An iterator over ConcurrentHashMap:
- Does not throw `ConcurrentModificationException` merely because another thread modifies the map.
- May reflect some updates made after iteration begins, but is not required to reflect all of them.
- Does not represent a snapshot of the entire map.

## 6. Why are null keys and null values prohibited?

A null result from `get(key)` has one clear meaning: there is no mapping for that key at the time of the lookup.

If null values were allowed, a caller could not distinguish:
- key is absent; or
- key is present and mapped to null.

This clear interpretation is useful for concurrent retrieval and conditional operations.

**Important:** If a concurrent insertion has not become visible to a particular read yet, that read can still return null. It does not mean the key has a null value or that an update is necessarily in progress.

## 7. put() vs putIfAbsent()

| Operation | Key absent | Key already present |
|---|---|---|
| `put(key, value)` | Inserts the mapping | Replaces the existing value |
| `putIfAbsent(key, value)` | Inserts the mapping | Leaves the existing value unchanged |

Both operations return the previous value, or null if there was no previous mapping.

### Example

```java
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

map.put("A", "Apple");
map.put("A", "Apricot");             // Replaces Apple
map.putIfAbsent("A", "Avocado");     // No change
map.putIfAbsent("B", "Banana");      // Inserts B
```

Final mappings:
- A = Apricot
- B = Banana

### Why is putIfAbsent useful?

`putIfAbsent` is an atomic operation. If two threads concurrently try to insert different values for the same absent key, only one insertion wins. The other operation observes that the key is already present and does not replace the winning value.

## 8. Interview Q&A

### Q1. Why is ConcurrentHashMap preferred over Hashtable?

ConcurrentHashMap avoids a single map-wide lock for ordinary operations. It uses CAS for certain updates and bin-level synchronization when required, allowing updates to different bins to proceed concurrently. Hashtable synchronizes its methods, causing more contention.

### Q2. Can multiple threads read while another thread updates a ConcurrentHashMap?

Yes. Retrievals generally do not acquire update locks. A read concurrent with an update may observe the state before or after that update, depending on timing.

### Q3. What does get(key) returning null mean?

Because ConcurrentHashMap prohibits null values, null means there is no mapping for that key at the time of the lookup. It does not prove that no concurrent insertion is underway; the read may simply occur before the insertion becomes visible.

### Q4. Why does ConcurrentHashMap not allow null keys or values?

To avoid ambiguity between an absent mapping and a mapping whose value is null, especially when retrievals and updates happen concurrently.

### Q5. What is the difference between put() and putIfAbsent()?

`put` inserts or replaces a mapping. `putIfAbsent` inserts only when the key is not already mapped; it does not overwrite an existing value.

### Q6. If two threads call putIfAbsent() for the same absent key, can both values be stored?

No. The operation is atomic for the key. Only one value is installed; the competing call observes the existing mapping.

### Q7. Does Java 8+ ConcurrentHashMap use segment locks?

No. The Java 7 implementation used segments. Java 8+ uses a combination of CAS and synchronization at the bin level when required.

## 9. Common interview mistakes

- ❌ “ConcurrentHashMap locks a segment for every write.”
  - ✅ Java 8+ uses CAS for some updates and bin-level synchronization for updates to occupied bins when needed.
- ❌ “A read blocks whenever a write is happening in the same bin.”
  - ✅ Retrievals generally do not acquire update locks.
- ❌ “get(key) returning null means the value is null.”
  - ✅ Null values are prohibited; null means no mapping was found at lookup time.
- ❌ “putIfAbsent always updates the value.”
  - ✅ It inserts only if the key is absent; it leaves an existing mapping unchanged.
- ❌ “ConcurrentHashMap iterators are snapshots.”
  - ✅ They are weakly consistent, not snapshot iterators.

## 10. Quick revision

- Thread-safe map designed for concurrent access.
- Java 8+: CAS for certain updates; bin-level synchronization when needed.
- Different bins can often be updated concurrently.
- Reads generally do not acquire update locks.
- Null keys and values are prohibited.
- Iterators are weakly consistent.
- `put` inserts or replaces.
- `putIfAbsent` inserts only if absent and is atomic.
- Hashtable uses coarse-grained synchronized methods.

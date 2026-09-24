# Day 02 — HashMap Internals (Java 17/21)

> Module 1 — Core Java

**Interview Importance:** ⭐⭐⭐⭐⭐

---

# Learning Objectives

By the end of this lesson you should be able to explain:

* HashMap internal architecture.
* `put()` internal flow.
* `get()` internal flow.
* Collision handling.
* Resize vs Rehashing.
* Treeification and Untreeification.
* Load Factor and Capacity.
* Common senior interview questions.

---

# 1. What is HashMap?

**Interview Definition**

HashMap is a hash table-based implementation of the `Map` interface that stores key-value pairs and provides average **O(1)** lookup using hashing.

---

# 2. Internal Architecture

HashMap internally contains:

* `Node[] table` → Bucket array.
* Each bucket stores a linked list or a red-black tree.
* Each `Node` contains:

  * `hash`
  * `key`
  * `value`
  * `next`

## Node Structure

```java
class Node<K,V> {
    int hash;
    K key;
    V value;
    Node<K,V> next;
}
```

---

# 3. Bucket Calculation

HashMap computes:

```java
int hash = hash(key);
int bucket = (capacity - 1) & hash;
```

Example:

* Capacity = 16
* Hash = 27

```
15 & 27 = Bucket 11
```

## Why `(n-1) & hash` instead of `%`?

Because capacity is always a **power of two**.

Benefits:

* Faster than modulo.
* Better bucket distribution.
* Efficient bitwise operation.

---

# 4. Why Initial Capacity is 16?

Default values:

| Property    | Value |
| ----------- | ----- |
| Capacity    | 16    |
| Load Factor | 0.75  |
| Threshold   | 12    |

Threshold formula:

```
Threshold = Capacity × Load Factor
```

Why 16?

* Power of two.
* Enables fast bucket calculation.
* Good balance between memory and performance.

---

# 5. HashMap.put() Internal Flow

1. Compute hash.
2. Calculate bucket index.
3. Bucket empty → Insert node.
4. Bucket occupied → Collision.
5. Compare stored hash.
6. Compare `equals()`.
7. Same key → Replace value.
8. Different key → Append node.
9. Size exceeds threshold → Resize.

---

# 6. Collision

**Definition**

A collision occurs when two different keys map to the same bucket index.

### Before Java 8

Collision handled using a **Linked List**.

Worst-case lookup:

```
O(n)
```

### Java 8+

If bucket size ≥ **8** and capacity ≥ **64**

Linked List becomes a **Red-Black Tree**.

Worst-case lookup becomes:

```
O(log n)
```

---

# 7. Treeification

Three interview numbers:

| Number | Meaning                               |
| ------ | ------------------------------------- |
| 8      | Treeify threshold                     |
| 6      | Untreeify threshold                   |
| 64     | Minimum capacity before treeification |

### Why capacity must be 64?

HashMap prefers **resizing first** because increasing capacity often reduces collisions naturally.

Treeification happens only if collisions still remain after resizing.

---

# 8. Resize vs Rehashing

## Resize

Capacity doubles.

```
16 → 32 → 64 → 128
```

Triggered when:

```
size > threshold
```

## Rehashing

Rehashing is **part of resizing**.

Existing entries are redistributed into new buckets because bucket calculation changes.

**Important**

Hash code does **not** change.

Bucket index changes.

---

# 9. HashMap.get() Internal Flow

1. Compute hash.
2. Find bucket.
3. Bucket empty → Return `null`.
4. Traverse linked list/tree.
5. Compare hash.
6. Compare `equals()`.
7. Return matching value.

---

# 10. Why Compare hash Before equals()?

Hash comparison is an integer comparison.

* Fast.
* Cheap.

`equals()` compares objects.

* More expensive.

Therefore HashMap compares hash first and calls `equals()` only when hashes match.

---

# 11. Mutable Keys (Interview Trap)

Never use mutable objects as HashMap keys.

Why?

Changing a field that participates in `equals()`/`hashCode()` changes bucket lookup.

Result:

```java
map.get(key)
```

returns `null` even though entry exists.

---

# 12. Time Complexity

| Operation          | Average  | Worst    |
| ------------------ | -------- | -------- |
| put                | O(1)     | O(n)     |
| get                | O(1)     | O(n)     |
| remove             | O(1)     | O(n)     |
| Tree bucket lookup | O(log n) | O(log n) |

---

# 13. Senior Interview Takeaways

* Capacity is always a power of two.
* Threshold = Capacity × Load Factor.
* Resize doubles capacity.
* Rehashing redistributes entries into new buckets.
* Treeification occurs at bucket size 8 and capacity 64.
* Untreeification occurs below bucket size 6.
* HashMap compares `hash` before `equals()`.
* Equal objects must have equal hash codes.
* Hash collisions are acceptable; excessive collisions hurt performance.

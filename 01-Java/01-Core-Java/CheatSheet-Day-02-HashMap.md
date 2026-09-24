# Cheat Sheet — Day 02 HashMap Internals

## 60-Second Revision Before Interview

### HashMap Formula

```java
bucket = (capacity - 1) & hash
```

### Default Values

| Property    | Value |
| ----------- | ----- |
| Capacity    | 16    |
| Load Factor | 0.75  |
| Threshold   | 12    |

---

## Resize

* Trigger: `size > threshold`
* Capacity doubles.
* Example: `16 → 32`

---

## Rehashing

* Happens during resize.
* Existing entries move to new buckets.
* Hash code remains unchanged.

---

## Collision

Different keys mapped to the same bucket index.

### Java 7

Linked List.

### Java 8+

Red-Black Tree after treeification.

---

## Magic Numbers

| Number | Meaning                            |
| ------ | ---------------------------------- |
| 8      | Treeify                            |
| 6      | Untreeify                          |
| 64     | Minimum capacity for treeification |

---

## put()

1. Compute hash.
2. Find bucket.
3. Empty → Insert.
4. Collision → Traverse.
5. hash match?
6. equals match?
7. Replace or Append.
8. Resize if threshold exceeded.

---

## get()

1. Compute hash.
2. Find bucket.
3. Empty → `null`.
4. Traverse bucket.
5. Compare hash.
6. Compare equals.
7. Return value.

---

## Why hash Before equals?

* Integer comparison is fast.
* Avoids unnecessary object comparisons.

---

## Mutable Key Trap

Changing key fields after insertion makes lookup fail because bucket lookup changes.

---

## Complexity

| Scenario              | Complexity |
| --------------------- | ---------- |
| Average get/put       | O(1)       |
| Linked List collision | O(n)       |
| Tree bucket           | O(log n)   |

---

## One-Line Interview Answers

* Collision → Different keys, same bucket.
* Resize → Capacity doubles after threshold.
* Rehashing → Entries redistributed into new buckets.
* Treeification → Linked List → Red-Black Tree.
* Load Factor → Memory vs performance trade-off.

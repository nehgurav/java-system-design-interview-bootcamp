# Interview Q&A — Day 02 HashMap Internals

## Frequently Asked Questions

### Q1. Why is HashMap capacity always a power of two?

**Answer**

Using powers of two allows HashMap to calculate bucket indexes using bitwise AND instead of modulo, improving performance and bucket distribution.

---

### Q2. Why is default capacity 16?

**Answer**

16 is a power of two and provides a good balance between memory consumption and lookup performance.

---

### Q3. Why is default load factor 0.75?

**Answer**

0.75 balances memory usage and collision probability. Lower values use more memory; higher values increase collisions.

---

### Q4. Difference between resize and rehashing?

**Answer**

Resize increases bucket array capacity. Rehashing redistributes existing entries into new buckets during resize.

---

### Q5. What is a collision?

**Answer**

Two different keys are mapped to the same bucket index.

---

### Q6. How are collisions handled?

**Answer**

Linked List in Java 7. Red-Black Tree in Java 8+ when treeification conditions are satisfied.

---

### Q7. Why compare hash before equals?

**Answer**

Hash comparison is cheaper than object comparison. `equals()` is invoked only when hashes match.

---

### Q8. Why override equals and hashCode together?

**Answer**

Equal objects must produce equal hash codes. Otherwise HashMap lookup fails.

---

### Q9. Why are mutable objects bad HashMap keys?

**Answer**

Changing a field that contributes to `hashCode()` changes bucket lookup and the entry becomes unreachable.

---

### Q10. What happens when the 13th element is inserted into a default HashMap?

**Answer**

Threshold is exceeded. HashMap resizes from 16 to 32 and redistributes existing entries into new buckets before completing insertion.

---

## VP Follow-Up Questions

1. Why treeify at 8 and untreeify at 6?
2. Why minimum capacity 64 before treeification?
3. Can two unequal objects have the same hash code?
4. Can two equal objects have different hash codes?
5. Why is `hash` stored inside each node?
6. What is hash spreading (`h ^ (h >>> 16)`)?
7. Why does HashMap use `(n-1) & hash`?
8. What problems occur with a poor `hashCode()` implementation?

---

## Common Interview Mistakes

❌ "Hash code changes during resize."

✅ Hash code stays the same; bucket index changes.

❌ "Collision means same hash code."

✅ Collision means same bucket index.

❌ "Rehashing is separate from resize."

✅ Rehashing is part of the resize process.

---

## 2-Minute Whiteboard Answer

Explain in this order:

1. HashMap architecture.
2. Bucket calculation.
3. `put()` flow.
4. Collision.
5. `equals()` and `hashCode()`.
6. Treeification.
7. Resize and rehashing.
8. `get()` flow.

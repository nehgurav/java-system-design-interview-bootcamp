# Day 01 — equals(), hashCode(), String Pool & Immutability

> Module 1 — Core Java

**Interview Importance:** ⭐⭐⭐⭐⭐

---

# Learning Goal

Understand:

- `==`
- `equals()`
- `hashCode()`
- String Pool
- String Immutability
- HashMap lookup flow

---

# 1. `==` vs `equals()`

## Primitive Types

```java
int a = 10;
int b = 10;

System.out.println(a == b);
```

Output:

```
true
```

`==` compares primitive values.

---

## Objects

```java
String s1 = new String("Neha");
String s2 = new String("Neha");
```

| Expression | Output |
|------------|--------|
| s1 == s2 | false |
| s1.equals(s2) | true |

Reason:

`==` compares references.

`equals()` compares logical content.

---

# 2. String Pool ⭐

```java
String s1 = "Neha";
String s2 = "Neha";
```

Output:

```
true
```

Both references point to the same pooled string.

## Why String Pool Exists?

- Memory optimisation.
- Reuse literals.
- Reduce duplicate objects.
- Improve performance.
- Reduce Garbage Collection.

### Banking Examples

- HTTP headers.
- JSON field names.
- Kafka topic names.
- Spring Bean names.
- URLs.

---

# 3. `intern()`

```java
String s1 = new String("Neha");
String s2 = s1.intern();
```

`intern()` returns the pooled reference.

---

# 4. Default equals()

```java
class Employee {

    int id;

}
```

```java
new Employee(1).equals(new Employee(1));
```

Output:

```
false
```

Reason:

Object.equals() compares references.

---

# 5. Override equals()

```java
@Override
public boolean equals(Object obj) {

    if (this == obj) return true;

    if (!(obj instanceof Employee)) return false;

    Employee other = (Employee) obj;

    return this.id == other.id;

}
```

Equality becomes business identity.

---

# Equals Contract ⭐

1. Reflexive
2. Symmetric
3. Transitive
4. Consistent
5. Null comparison returns false

---

# 6. hashCode()

`hashCode()` generates an integer used to locate a bucket inside HashMap.

Example:

```
hashCode = 21

bucket = 21 % capacity
```

Important:

> hashCode finds bucket.

> equals confirms correct key.

---

# 7. equals() and hashCode() Contract ⭐

| Rule | Meaning |
|------|---------|
| Equal objects | Must return same hashCode. |
| Same hashCode | Objects may still be different. |

---

# 8. Common HashMap Bug ⭐

```java
Map<Employee,String> map = new HashMap<>();

map.put(new Employee(1),"Neha");

map.get(new Employee(1));
```

Without overriding hashCode():

Output:

```
null
```

Reason:

Different bucket.

equals() is never called.

---

# Correct hashCode()

```java
@Override
public int hashCode() {
    return Objects.hash(id);
}
```

Now lookup succeeds.

---

# 9. HashMap Lookup Flow

1. Calculate hashCode.
2. Find bucket.
3. Traverse bucket.
4. Use equals() to find exact key.
5. Return value.

**Interview Line**

> hashCode narrows the search; equals verifies identity.

---

# 10. String Immutability ⭐

```java
String name = "Neha";

name.concat(" Gurav");

System.out.println(name);
```

Output:

```
Neha
```

A new String object is created.

---

# Why String Is Immutable?

| Reason | Benefit |
|--------|---------|
| String Pool | Shared literals remain safe. |
| Security | Passwords, JWTs, URLs cannot change. |
| Thread Safety | Immutable objects are naturally thread-safe. |
| HashMap Keys | Hash code remains stable. |
| Caching | Hash code can be cached safely. |

---

# Production Example

JWT token stored as String.

Multiple request threads read the same token.

No thread can modify it.

Authentication remains safe.

---

# String vs StringBuilder vs StringBuffer

| String | StringBuilder | StringBuffer |
|--------|---------------|--------------|
| Immutable | Mutable | Mutable |
| Thread-safe because immutable | Not thread-safe | Thread-safe |
| New object every change | Same object | Same object |
| Slow for repeated concatenation | Fastest | Slower than Builder |

---

# Performance Example

## Bad

```java
String result = "";

for (Payment p : payments) {
    result = result + p.getId();
}
```

Creates thousands of temporary Strings.

## Good

```java
StringBuilder builder = new StringBuilder();

for (Payment p : payments) {
    builder.append(p.getId());
}
```

Uses one mutable object.

---

# Frequently Asked Interview Questions

### Q1

Difference between `==` and `equals()`.

### Q2

Explain String Pool.

### Q3

Why override hashCode with equals?

### Q4

Explain HashMap lookup internally.

### Q5

Why is String immutable?

### Q6

StringBuilder vs StringBuffer.

---

# VP / Principal Follow-up Questions

- Why are immutable objects thread-safe?
- Can mutable objects be HashMap keys?
- Why does HashMap fail when hashCode isn't overridden?
- What problems would occur if Strings were mutable?

---

# Revision Cheat Sheet

- `==` → Reference comparison for objects.
- `equals()` → Logical equality.
- String literals live in String Pool.
- `new String()` creates a heap object.
- Equal objects must have equal hash codes.
- hashCode finds bucket.
- equals verifies key.
- String is immutable for security, thread safety, pooling, and stable hashing.

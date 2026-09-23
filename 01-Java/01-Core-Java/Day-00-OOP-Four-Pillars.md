# Day 00 — OOP Four Pillars (Core Java)

> Module 1 — Core Java

**Interview Importance:** ⭐⭐⭐⭐⭐

---

# Learning Goal

Understand OOP beyond definitions and answer interview questions with production examples.

---

# The Four Pillars of OOP

| Pillar | Purpose |
|--------|---------|
| Encapsulation | Protect object state and business rules. |
| Abstraction | Hide implementation complexity and expose only behaviour. |
| Inheritance | Reuse behaviour through an **IS-A** relationship. |
| Polymorphism | Same interface, different runtime behaviour. |

---

# 1. Encapsulation

## Definition

Encapsulation protects an object's internal state by exposing controlled operations instead of allowing direct access.

## Bad Example

```java
class Account {
    public double balance;
}
```

Any class can modify the balance.

## Good Example

```java
class Account {

    private BigDecimal balance;

    public void withdraw(BigDecimal amount) {

        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException("Insufficient Balance");
        }

        balance = balance.subtract(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
```

## Banking Example

Business invariants:

- Balance cannot become negative.
- Frozen accounts cannot be debited.
- Withdrawal amount must be greater than zero.

Encapsulation protects these rules.

---

# 2. Abstraction

## Definition

Expose **what** a service does while hiding **how** it works.

## Example

```java
public interface PaymentService {
    PaymentResult pay(PaymentRequest request);
}
```

Implementation may use:

- Visa
- Mastercard
- Kafka
- REST API
- IBM MQ

Caller doesn't know.

---

# Encapsulation vs Abstraction

| Encapsulation | Abstraction |
|---------------|-------------|
| Hides state. | Hides implementation. |
| Protects data. | Reduces complexity. |
| Uses private fields. | Uses interfaces/abstract classes. |

**Interview Answer**

> Encapsulation controls access to state, whereas abstraction hides unnecessary implementation details behind a contract.

---

# 3. Inheritance

## Definition

Inheritance models an **IS-A** relationship.

```java
class PaymentProcessor {}

class CardPaymentProcessor extends PaymentProcessor {}

class UpiPaymentProcessor extends PaymentProcessor {}
```

Examples:

- CardPaymentProcessor IS-A PaymentProcessor.
- UpiPaymentProcessor IS-A PaymentProcessor.

---

# 4. Composition vs Inheritance ⭐

## Composition

Models a **HAS-A** relationship.

```java
class PaymentProcessor {

    private Validator validator;
    private AuditLogger logger;

}
```

PaymentProcessor HAS-A Validator.

PaymentProcessor HAS-A Logger.

### Why Composition?

- Loose coupling.
- Easy testing.
- Dependency Injection.
- Easier replacement of implementations.

### Banking Example

PaymentProcessor should **not extend** LoginService.

Instead:

```java
class PaymentProcessor {

    private LoginValidator loginValidator;

}
```

---

# 5. Polymorphism

## Runtime Polymorphism

```java
interface PaymentProcessor {
    void process();
}
```

```java
class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public void process() {
        System.out.println("Card");
    }

}
```

```java
PaymentProcessor processor = new CardPaymentProcessor();
processor.process();
```

Output:

```
Card
```

Reason:

Java performs **dynamic method dispatch**.

Runtime object decides implementation.

---

# Reference Type vs Object Type ⭐

```java
PaymentProcessor processor = new CardPaymentProcessor();
```

| Type | Value |
|------|-------|
| Reference Type | PaymentProcessor |
| Runtime Type | CardPaymentProcessor |

Compiler checks method existence using reference type.

Runtime invokes implementation using object type.

---

# 6. Overloading vs Overriding

| Overloading | Overriding |
|-------------|------------|
| Same method name | Same method signature |
| Different parameters | Different implementation |
| Compile time | Runtime |
| No inheritance required | Requires inheritance |

---

# 7. Static Methods Are Not Polymorphic ⭐

Static methods belong to the class.

```java
class Parent {

    static void print() {}

}

class Child extends Parent {

    static void print() {}

}
```

Method hiding occurs.

No runtime dispatch.

---

# Production Scenario (Barclays)

EMM Payment API:

- PaymentProcessor interface.
- CardPaymentProcessor implementation.
- UpiPaymentProcessor implementation.
- Validator injected through composition.

Benefits:

- Easy testing.
- New payment methods without changing caller.
- Spring Dependency Injection support.

---

# Frequently Asked Interview Questions

### Q1

Difference between abstraction and encapsulation.

### Q2

Why prefer composition over inheritance?

### Q3

Explain runtime polymorphism.

### Q4

Reference type vs object type.

### Q5

Why are static methods not polymorphic?

---

# Common Interview Mistakes

❌ Encapsulation means wrapping data into one unit.

✅ Encapsulation protects object state and business invariants.

❌ Abstraction means hiding functions.

✅ Abstraction hides implementation complexity while exposing behaviour.

---

# Revision Cheat Sheet

- Encapsulation → Protect state.
- Abstraction → Hide complexity.
- Inheritance → IS-A.
- Composition → HAS-A.
- Runtime Polymorphism → Dynamic dispatch.
- Static methods → Hidden, not overridden.

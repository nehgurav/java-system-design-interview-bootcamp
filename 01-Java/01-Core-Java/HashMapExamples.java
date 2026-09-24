# HashMapExamples.java

```java
package org.example.corejava.hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HashMapExamples {

    public static void main(String[] args) {

        basicPutGet();

        collisionExample();

        mutableKeyProblem();

        duplicateKeyExample();
    }

    // Example 1
    static void basicPutGet() {

        Map<Integer, String> map = new HashMap<>();

        map.put(101, "Neha");

        System.out.println(map.get(101));
    }

    // Example 2
    static void collisionExample() {

        Map<Employee, String> map = new HashMap<>();

        Employee e1 = new Employee(1, "Neha");
        Employee e2 = new Employee(2, "Rahul");
        Employee e3 = new Employee(3, "Amit");

        map.put(e1, "Developer");
        map.put(e2, "QA");
        map.put(e3, "Architect");

        System.out.println(map.get(new Employee(2, "Rahul")));
    }

    // Example 3
    static void mutableKeyProblem() {

        Map<MutableEmployee, String> map = new HashMap<>();

        MutableEmployee emp = new MutableEmployee(101, "Neha");

        map.put(emp, "Developer");

        emp.setId(102);

        System.out.println(map.get(emp));   // null
    }

    // Example 4
    static void duplicateKeyExample() {

        Map<Employee, String> map = new HashMap<>();

        Employee e1 = new Employee(1, "Neha");

        map.put(e1, "Developer");

        map.put(new Employee(1, "Neha"), "Lead");

        System.out.println(map.get(e1));    // Lead
    }
}

final class Employee {

    private final int id;
    private final String name;

    Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Deliberately poor hashCode to demonstrate collisions.
    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof Employee)) return false;

        Employee other = (Employee) obj;

        return id == other.id &&
                Objects.equals(name, other.name);
    }
}

class MutableEmployee {

    private int id;
    private final String name;

    MutableEmployee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof MutableEmployee)) return false;

        MutableEmployee other = (MutableEmployee) obj;

        return id == other.id &&
                Objects.equals(name, other.name);
    }
}
```

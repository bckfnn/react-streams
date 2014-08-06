# Filter operations.

### filter(predicate)

The filter operation will emit the elements where the predicate function return true.

![Filter operation](filter.png "title")

```java
Builder.from(1, 2, 3, 4, 5, 6, 7)
    .filter(x -> x % 2 == 0)
    .printStream("filter", System.out)
    .start(1);
```

```none
filter onNext: 2
filter onNext: 4
filter onNext: 6
filter onCompleted 
```

### last()

The last operation will ignore all the input element except the very last.

![Last operation](last.png)

```java
Builder.from(1, 2, 3, 4)
    .last()
    .printStream("last", System.out)
    .start(1);
```

```
last onNext: 4
last onComplete
```


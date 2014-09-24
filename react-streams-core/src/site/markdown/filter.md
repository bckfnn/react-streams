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
filter onSubScribe:Subscription to PrintStreamOp
filter request(1)
filter onNext:2
filter request(1)
filter onNext:4
filter request(1)
filter onNext:6
filter request(1)
filter onComplete
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
last onSubScribe:Subscription to PrintStreamOp
last request(1)
last onNext:4
last request(1)
last onComplete
```


/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bckfnn.reactstreams.test;

import io.github.bckfnn.reactstreams.Func1;
import io.github.bckfnn.reactstreams.Pipe;
import io.github.bckfnn.reactstreams.Stream;
import io.github.bckfnn.reactstreams.Tuple;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Testing of streams.
 */
public class SimpleTest {

    /**
     * Test tuples.
     */
    @Test
    public void testTuple() {
        Tuple<Integer, Integer> t1 = new Tuple<>(1, 2);
        Assert.assertEquals((Integer) 1, t1.left());
        Assert.assertEquals((Integer) 2, t1.right());
        Assert.assertEquals("Tuple[1, 2]", t1.toString());

        Tuple<Integer, Integer> t2 = new Tuple<>(1, 2);
        Assert.assertTrue(t2.equals(t1));
        Assert.assertTrue(t1.equals(t2));
        Assert.assertEquals(t1.hashCode(), t2.hashCode());
        Assert.assertTrue(t1.equals(t1));

        Assert.assertFalse(t1.equals("abc"));
        Assert.assertFalse(t1.equals(null));
        Assert.assertFalse(t1.equals(new Tuple<Integer, Integer>(1, null)));
        Assert.assertFalse(t1.equals(new Tuple<Integer, Integer>(null, null)));
        Assert.assertFalse(new Tuple<Integer, Integer>(1, null).equals(t1));
        Assert.assertFalse(new Tuple<Integer, Integer>(null, null).equals(t1));

        Assert.assertTrue(new Tuple<Integer, Integer>(null, null).equals(new Tuple<>(null, null)));


        Tuple<Integer, Integer> t3 = new Tuple<>(3, 4);
        Assert.assertFalse(t1.equals(t3));
        Assert.assertFalse(t1.hashCode() == t3.hashCode());

        Assert.assertTrue(new Tuple<>(null, null).hashCode() == new Tuple<>(null, null).hashCode());
    }

    /**
     * Test a simple List.
     */
    @Test
    public void testIterable() {
        Keep<String> keep = new Keep<>();
        Stream
        .from(Arrays.asList("12", "34", "56"))
        .chain(keep)
        .start(1);

        keep.assertEquals("12", "34", "56");
    }

    /**
     * Test an empty List.
     */
    @Test
    public void testIterableEmpty() {
        Keep<String> keep = new Keep<>();
        Stream
        .from(Arrays.<String> asList())
        .chain(keep)
        .start(1);
        keep.assertEquals();
    }

    /**
     * Test an array.
     */
    @Test
    public void testArray() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .chain(keep)
        .start(1);
        keep.assertEquals("12", "34", "56");
    }

    /**
     * Test a single value.
     */
    @Test
    public void testOne() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12")
        .chain(keep)
        .start(1);

        keep.assertEquals("12");
    }

    /**
     * Test an error stream.
     */
    @Test
    public void testError() {
        Keep<String> keep = new Keep<>();
        Stream
        .<String> error(new RuntimeException("test"))
        .chain(keep)
        .start(1);
        keep.assertException(new RuntimeException("test"));
    }

    /**
     * Test a done operation.
     */
    @Test
    public void testDone1() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .done()
        .chain(keep)
        .start(1);
        keep.assertEquals();

    }

    /**
     * Test a counter operation.
     */
    @Test
    public void testCounter() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .counter()
        .take(5)
        .chain(keep)
        .start(1);
        keep.assertEquals(0, 1, 2, 3, 4);
    }

    /**
     * Test a counter operation.
     */
    @Test
    public void testCounter2() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .counter(3)
        .take(5)
        .chain(keep)
        .start(1);
        keep.assertEquals(3, 4, 5, 6, 7);
    }

    /**
     * Test a counter operation.
     */
    @Test
    public void testCounter3() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .counter(3)
        .take(1000000)
        .chain(keep)
        .start(1);
    }

    /**
     * Test a counter operation.
     */
    @Test
    public void testCounter4() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .counter(3)
        .take(1000000)
        .chain(keep)
        .start(100);
    }

    /**
     * Test a zip operation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testZip1() {
        Keep<Tuple<Integer, String>> keep = new Keep<>();

        Stream.zip(Stream.counter(), Stream.from("a", "b", "c"))
        .chain(keep)
        .start(1);
        keep.assertEquals(new Tuple<>(0, "a"), new Tuple<>(1, "b"), new Tuple<>(2, "c"));
    }

    /**
     * Test a zip operation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testZip2() {
        Keep<Tuple<String, Integer>> keep = new Keep<>();

        Stream.zip(Stream.from("a", "b", "c"), Stream.counter())
        .chain(keep)
        .start(1);

        keep.assertEquals(new Tuple<>("a", 0), new Tuple<>("b", 1), new Tuple<>("c", 2));
    }

    /**
     * Test a zip operation.
     */
    @Test
    public void testZip3() {
        Keep<Tuple<Integer, Integer>> keep = new Keep<>();

        Stream.zip(Stream.counter(10), Stream.counter())
        .take(1000000)
        .chain(keep)
        .start(1);
    }

    /**
     * Test a zip operation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testZip4() {
        Keep<Tuple<String, Integer>> keep = new Keep<>();

        Stream.zip(Stream.from("a", "b", "c").continueWithError(new Exception("xx")), Stream.counter())
        .chain(keep)
        .start(1);

        keep.assertException(new Exception("xx"), new Tuple<>("a", 0), new Tuple<>("b", 1));
    }

    /**
     * Test a nop operation.
     */
    @Test
    public void testNop() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("def")
        .nop()
        .chain(keep)
        .start(1);

        keep.assertEquals("def");
    }

    /**
     * Test a map operation.
     */
    @Test
    public void testMap1() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .map((String value) -> "-" + value)
        .map((String value) -> Integer.valueOf(value))
        .chain(keep)
        .start(1);
        keep.assertEquals(-12, -34, -56); 
    }

    /**
     * Test a map operation.
     */
    @Test
    public void testMap2() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .map(new Func1<String, String>() { 
            @Override
            public String apply(String value) {
                if (value.equals("34")) {
                    throw new RuntimeException("stop!");
                }
                return "-" + value;
            }
        })
        .map(new Func1<String, Integer>() {
            @Override
            public Integer apply(String value) {
                return Integer.valueOf(value);
            }
        })
        .chain(keep)
        .start(1);

        keep.assertException(new RuntimeException("stop!"), -12);
    }

    /**
     * Test a map operation.
     */
    @Test
    public void testMap5() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .counter()
        .take(1000000)
        .map((Integer value) -> "-" + value)
        .map((String value) -> Integer.valueOf(value))
        .chain(keep)
        .start(1);
    }


    /**
     * Test a mapMany operation.
     */
    @Test
    public void testMapMany1() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .mapMany(new Func1<String, Stream<String>>() {
            @Override
            public Stream<String> apply(String value) {
                return Stream.from("x" + value, "y" + value, "z" + value);
            }
        })
        .chain(keep)
        .start(1);

        keep.assertEquals("x12", "y12", "z12", "x34", "y34", "z34", "x56", "y56", "z56");
    }

    /**
     * Test a mapMany operation.
     */
    @Test
    public void testMapMany2() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .mapMany((String value) -> Stream.from("x" + value, "y" + value, "z" + value))
        .chain(keep)
        .start(1);

        keep.assertEquals("x12", "y12", "z12", "x34", "y34", "z34", "x56", "y56", "z56");
    }

    /**
     * Test a mapMany operation.
     */
    @Test
    public void testMapMany3() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .<String> mapMany((value) -> { throw new Exception("x"); })
        .chain(keep)
        .start(1);

        keep.assertException(new Exception("x"));
    }

    /**
     * Test a mapMany operation.
     */
    @Test
    public void testMapMany4() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .mapMany((String value) -> Stream.<String> error(new Exception("x")))
        .chain(keep)
        .start(1);

        keep.assertException(new Exception("x"));
    }

    /**
     * Test a mapMany operation.
     * @throws InterruptedException exception.
     */
    @Test
    public void testMapMany5() throws InterruptedException {
        Keep<String> keep = new Keep<>();
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.execute(() -> {
            Stream.from(1, 2, 3).mapMany(i -> {
                return Stream.
                        from("a" + i, "b" + i, "c" + i).
                        each((v, op) -> {
                            s.execute(() -> {
                                op.sendNext(v);
                                op.handled();
                            });
                        });
            }).
            continueWith(() -> { s.shutdown(); }).
            chain(keep).
            start(1);
        });
        s.awaitTermination(10, TimeUnit.SECONDS);
        keep.assertEquals("a1", "b1", "c1", "a2", "b2", "c2", "a3", "b3", "c3");
    }

    /**
     * Test a mapManyWith operation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMapManyWith1() {
        Keep<Tuple<String, String>> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .mapManyWith(value -> Stream.from("x" + value, "y" + value, "z" + value))
        .chain(keep)
        .start(1);

        keep.assertEquals(
                new Tuple<>("12", "x12"), new Tuple<>("12", "y12"), new Tuple<>("12", "z12"),
                new Tuple<>("34", "x34"), new Tuple<>("34", "y34"), new Tuple<>("34", "z34"),
                new Tuple<>("56", "x56"), new Tuple<>("56", "y56"), new Tuple<>("56", "z56"));
    }

 
    /**
     * Test a filter operation.
     */
    @Test
    public void testFilter1() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .filter(x -> x.equals("34") )
        .chain(keep)
        .start(1);

        keep.assertEquals("34");
    }


    /**
     * Test a filter operation.
     */
    @Test
    public void testFilter2() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .continueWithError(new Exception("xx"))
        .filter(x -> x.equals("34") )
        .chain(keep)
        .start(1);

        keep.assertException(new Exception("xx"), "34");
    }

    /**
     * Test a filter operation.
     */
    @Test
    public void testFilter3() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .continueWithError(new Exception("xx"))
        .filter(x -> {
            if (x.equals("34")) {
                return true;
            }
            if (x.equals("56")) {
                throw new Exception("xx");
            }
            return false;
        })
        .chain(keep)
        .start(1);

        keep.assertException(new Exception("xx"), "34");
    }

    /**
     * Test a last operation.
     */
    @Test
    public void testLast() {
        Keep<String> keep = new Keep<>();
        Stream
        .from("12", "34", "56")
        .last()
        .chain(keep)
        .start(1);

        keep.assertEquals("56");
    }

    /**
     * Test a last operation.
     */
    @Test
    public void testLast2() {
        Keep<String> keep = new Keep<>();
        Stream
        .<String> from()
        .last()
        .chain(keep)
        .start(1);

        keep.assertEquals();
    }

    /**
     * Test a skip operation.
     */
    @Test
    public void testSkip() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .skip(1)
        .chain(keep)
        .start(1);

        keep.assertEquals(2, 3);
    }

    /**
     * Test a skip operation.
     */
    @Test
    public void testSkip2() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .skip(5)
        .chain(keep)
        .start(1);

        keep.assertEquals();
    }

    /**
     * Test a take operation.
     */
    @Test
    public void testTake() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .take(2)
        .chain(keep)
        .start(1);

        keep.assertEquals(1, 2);
    }

    /**
     * Test a take operation.
     */
    @Test
    public void testTake2() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .take(5)
        .chain(keep)
        .start(1);

        keep.assertEquals(1, 2, 3);
    }

    /**
     * Test a accumulator operation.
     */
    @Test
    public void testAccumulator1() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .accumulate(0, (value, next) -> value + next)
        .chain(keep)
        .start(1);

        keep.assertEquals(0, 1, 3, 6);
    }

    /**
     * Test a accumulator operation.
     */
    @Test
    public void testAccumulator2() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .accumulate(null, (value, next) -> value + next)
        .chain(keep)
        .start(1);

        keep.assertEquals(1, 3, 6);
    }

    /**
     * Test a accumulator operation.
     */
    @Test
    public void testAccumulator4() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .accumulate(0, (value, next) -> {
            if (next == 3) {
                throw new Exception("xx");
            }
            return value + next;
        })
        .chain(keep)
        .start(1);
        keep.assertException(new Exception("xx"), 0, 1, 3);
    }

    /**
     * Test a concat operation.
     */
    @Test
    public void testConcat() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .concat(Stream.from(1, 2, 3), Stream.from(4, 5))
        .chain(keep)
        .start(1);

        keep.assertEquals(1, 2, 3, 4, 5);
    }


    /**
     * Test a whenDoneValue operation.
     */
    @Test
    public void testWhenDoneValue1() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .whenDoneValue(4)
        .chain(keep)
        .start(1);

        keep.assertEquals(4);
    }

    /**
     * Test a whenDoneError operation.
     */
    @Test
    public void testWhenDoneError1() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .whenDoneError(new Exception("xx"))
        .chain(keep)
        .start(1);

        keep.assertException(new Exception("xx"));
    }

    /**
     * Test a onComplete operation.
     */
    //@Test
    public void testOnComplete() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .ignore()
        .onComplete(() -> { keep.doNext(44); })
        .chain(keep)
        .start(1);

        keep.assertEquals(44);
    }

    /**
     * Test a whenDone operation.
     */
    @Test
    public void testWhenDonePublisher1() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .whenDone(Stream.from(5, 6, 7))
        .chain(keep)
        .start(1);

        keep.assertEquals(5, 6, 7);
    }

    /**
     * Test a whenDone operation.
     */
    @Test
    public void testWhenDonePublisher2() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .whenDone(Stream.counter())
        .take(100000)
        .chain(keep)
        .start(1);
    }

    /**
     * Test a continueWithError operation.
     */
    @Test
    public void testContinueWithError1() {
        Keep<Integer> keep = new Keep<>();
        Stream
        .from(1, 2, 3)
        .continueWithError(new Exception("xx"))
        .chain(keep)
        .start(1);

        keep.assertException(new Exception("xx"), 1, 2, 3);
    }
    /*
    @Test
    public void testWhenDone2() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .whenDone(new Proc0() {
            @Override
            public void apply() {
            	onchain(4);
            	onComplete();
            }
        })
        .then(keep)
        .start();

        keep.assertEquals(4);
    }
/*
    @Test
    public void testWhenDone3() {
        Keep<Integer> keep = new Keep<>();
        final AtomicLong cnt = new AtomicLong(0);

        Stream.from(1, 2, 3)
        .<Integer> whenDone(new Stream.Proc0() {
            @Override
            public void call() {
                cnt.incrementAndGet();
            }

        })
        .then(keep)
        .start();

        keep.assertEquals();
        Assert.assertEquals(1, cnt.longValue());
    }


    @Test
    public void testWhenDone4() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .whenDone(Stream.from(4, 5, 6))
        .then(keep);

        keep.assertEquals(4, 5, 6);
    }
     */

    /**
     * Test pipe.
     */
    @Test
    public void testPipe() {
        Keep<String> keep = new Keep<>();
        Stream.from(1, 2, 3, 4, 5).
        chain(makePipe()).
        chain(keep).
        start(1);
        keep.assertEquals("x2", "x4");
    }

    private Pipe<Integer, String> makePipe() {
        return Stream.asPipe(pipe -> pipe.filter(v -> v % 2 == 0).map(v -> "x" + v));
    }

    /**
     * Test delegate
     */
    @Test
    public void testDelegate1() {
        Keep<Integer> keep = new Keep<>();

        Keep<Integer> keep2 = new Keep<>();

        Stream
        .from(1, 2, 3)
        .delegate(keep)
        //.chain(keep)
        //.debug()
        //.stdout("xxx")
        .chain(keep2)
        .start(1);

        keep.assertEquals(1, 2, 3);
        keep2.assertEquals(1, 2, 3);
    }

    /**
     * Test toList operation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testToList1() {
        Keep<List<Integer>> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .toList()
        .chain(keep)
        .start(1);

        keep.assertEquals(Arrays.asList(1, 2, 3));
    }

    /**
     * Test each operation.
     */
    @Test
    public void testEach1() {
        Keep<Integer> keep = new Keep<>();

        Stream
        .from(1, 2, 3)
        .each((v, proc) -> { 
            proc.sendNext(v);
            proc.handled();
        })
        .chain(keep)
        .start(1);

        keep.assertEquals(1, 2, 3);

    }



    /*    
    @Test
    public void testQueue() {
        final Keep<Integer> keep = new Keep<>();

        final Queue q = new Queue();
        q.add(new Runnable() {
            @Override
            public void run() {
                Stream
                .from(1, 2, 3)
                .then(new Chain.Counting<Integer, Integer>() {
                    @Override
                    public void onchain(final Integer value) {
                        super.onchain(value);
                        q.add(new Runnable() {
                            @Override
                            public void run() {
                                chain(value + 10);
                            }
                        });
                    }
                })
                //.stdout("xx")
                .then(keep)
                .whenDone(new Stream.Proc0()   {
                    @Override
                    public void call() {
                        keep.assertEquals(11, 12, 13);
                    }
                })
                .start();
            }
        });

        q.run();
    }
     */

    /*
    @Test
    public void testPipe() {
        final Keep<String> keep = new Keep<>();

        Stream
        .from(1, 2, 3, 4)
        //.stdout("..")
        .then(makePipe())
        //.stdout("xx")
        .then(keep)
        //.debug()
        .start();

        keep.assertEquals("11", "12", "13", "14");
    }

    private Chain<Integer, String> makePipe() {
        return Stream
        .<Integer>pipe()
        .map(new Stream.MapFunction<Integer, Integer>() {
            @Override
            public Integer map(Integer value) throws Exception {
                return value + 10;
            }
        })
        .map(new Stream.MapFunction<Integer, String>() {
            @Override
            public String map(Integer value) throws Exception {
                return String.valueOf(value);
            }
        })
        .asChain();
    }
     */
    static class Queue {
        Deque<Runnable> queue = new LinkedList<>();

        public void add(Runnable runnable) {
            queue.add(runnable);
        }

        public void run() {
            while (queue.size() > 0) {
                Runnable r = queue.pop();
                //System.out.println(r);
                r.run();
            }
        }
    }

    static class StdoutSubscriber<T> implements Subscriber<T> {
        Subscription subscription;
        int buffer;

        public StdoutSubscriber() {
            this(1);
        }

        public StdoutSubscriber(int buffer) {
            this.buffer = buffer;
        }

        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(buffer);
        }

        @Override
        public void onNext(T element) {
            System.out.println("onNext:" + element);
            subscription.request(1);
        }

        @Override
        public void onComplete() {
            System.out.println("onComplete:");
        }

        @Override
        public void onError(Throwable cause) {
            System.out.println("onError:" + cause);
        }
    }
}

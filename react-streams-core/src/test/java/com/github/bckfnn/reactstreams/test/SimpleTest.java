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
package com.github.bckfnn.reactstreams.test;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.Builder;
import com.github.bckfnn.reactstreams.Func1;
import com.github.bckfnn.reactstreams.Operations;
import com.github.bckfnn.reactstreams.Tuple;
import com.github.bckfnn.reactstreams.ops.AccumulatorOp;
import com.github.bckfnn.reactstreams.ops.FilterOp;
import com.github.bckfnn.reactstreams.ops.MapManyOp;
import com.github.bckfnn.reactstreams.ops.MapOp;


public class SimpleTest {

    @Test
    public void testIterable() {
        Keep<String> keep = new Keep<>();
        Builder
        .from(Arrays.asList("12", "34", "56"))
        .then(keep)
        .start(1);

        keep.assertEquals("12", "34", "56");
    }

    @Test
    public void testIterableEmpty() {
        Keep<String> keep = new Keep<>();
        Builder
        .from(Arrays.<String> asList())
        .then(keep)
        .start(1);
        keep.assertEquals();
    }

    @Test
    public void testArray() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .then(keep)
        .start(1);
        keep.assertEquals("12", "34", "56");
    }

    @Test
    public void testOne() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12")
        .then(keep)
        .start(1);

        keep.assertEquals("12");
    }


    @Test
    public void testSingle() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("abc")
        .then(keep)
        .start(1);
        keep.assertEquals("abc");
    }

    @Test
    public void testError() {
        Keep<String> keep = new Keep<>();
        Builder
        .<String> error(new RuntimeException("test"))
        .then(keep)
        .start(1);
        keep.assertException(new RuntimeException("test"));
    }
    
    @Test
    public void testDone1() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .done()
        .then(keep)
        .start(1);
        keep.assertEquals();
        
    }

    @Test
    public void testCounter() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .counter()
        .take(5)
        .then(keep)
        .start(1);
        keep.assertEquals(0, 1, 2, 3, 4);
    }

    @Test
    public void testCounter2() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .counter(3)
        .take(5)
        .then(keep)
        .start(1);
        keep.assertEquals(3, 4, 5, 6, 7);
    }

    @Test
    public void testCounter3() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .counter(3)
        .take(1000000)
        .then(keep)
        .start(1);
    }

    @Test
    public void testCounter4() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .counter(3)
        .take(1000000)
        .then(keep)
        .start(100);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testZip1() {
        Keep<Tuple<Integer, String>> keep = new Keep<>();

        Builder.zip(Builder.counter(), Builder.from("a", "b", "c"))
        .then(keep)
        .start(1);
        keep.assertEquals(new Tuple<>(0, "a"), new Tuple<>(1, "b"), new Tuple<>(2, "c"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testZip2() {
        Keep<Tuple<String, Integer>> keep = new Keep<>();

        Builder.zip(Builder.from("a", "b", "c"), Builder.counter())
        .then(keep)
        .start(1);

        keep.assertEquals(new Tuple<>("a", 0), new Tuple<>("b", 1), new Tuple<>("c", 2));
    }

    @Test
    public void testZip3() {
        Keep<Tuple<Integer, Integer>> keep = new Keep<>();

        Builder.zip(Builder.counter(10), Builder.counter())
        .take(1000000)
        .then(keep)
        .start(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testZip4() {
        Keep<Tuple<String, Integer>> keep = new Keep<>();

        Builder.zip(Builder.from("a", "b", "c").continueWithError(new Exception("xx")), Builder.counter())
        .then(keep)
        .start(1);

        keep.assertException(new Exception("xx"), new Tuple<>("a", 0), new Tuple<>("b", 1));
    }

    @Test
    public void testNop() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("def")
        .nop()
        .then(keep)
        .start(1);

        keep.assertEquals("def");
    }


    @Test
    public void testMap1() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .then(new MapOp<String, String>() {
            @Override
            public String map(String value) {
                return "-" + value;
            }
        })
        .then(new MapOp<String, Integer>() {
            @Override
            public Integer map(String value) {
                return Integer.valueOf(value);
            }
        })
        .then(keep)
        .start(1);

        keep.assertEquals(-12, -34, -56);
    }

    @Test
    public void testMap2() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .map((String value) -> "-" + value)
        .map((String value) -> Integer.valueOf(value))
        .then(keep)
        .start(1);
        keep.assertEquals(-12, -34, -56); 
    }

    @Test
    public void testMap3() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .then(new MapOp<String, String>() {
            @Override
            public String map(String value) {
                if (value.equals("34")) {
                    throw new RuntimeException("stop!");
                }
                return "-" + value;
            }
        })
        .then(new MapOp<String, Integer>() {
            @Override
            public Integer map(String value) {
                return Integer.valueOf(value);
            }
        })
        .then(keep)
        .start(1);

        keep.assertException(new RuntimeException("stop!"), -12);
    }

    @Test
    public void testMap4() {
        Keep<Integer> keep = new Keep<>();
        Builder
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
        .then(keep)
        .start(1);

        keep.assertException(new RuntimeException("stop!"), -12);
    }

    @Test
    public void testMap5() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .counter()
        .take(1000000)
        .map((Integer value) -> "-" + value)
        .map((String value) -> Integer.valueOf(value))
        .then(keep)
        .start(1);
    }

    @Test
    public void testMapMany1() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .then(new MapManyOp<String, String>() {
            @Override
            public Operations<String> map(String value) {
                return Builder.from("x" + value, "y" + value, "z" + value);
            }
        })
        .then(keep)
        .start(1);

        keep.assertEquals("x12", "y12", "z12", "x34", "y34", "z34", "x56", "y56", "z56");
    }

    @Test
    public void testMapMany2() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .mapMany(new Func1<String, Operations<String>>() {
            @Override
            public Operations<String> apply(String value) {
                return Builder.from("x" + value, "y" + value, "z" + value);
            }
        })
        .then(keep)
        .start(1);

        keep.assertEquals("x12", "y12", "z12", "x34", "y34", "z34", "x56", "y56", "z56");
    }

    @Test
    public void testMapMany3() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .mapMany((String value) -> Builder.from("x" + value, "y" + value, "z" + value))
        .then(keep)
        .start(1);

        keep.assertEquals("x12", "y12", "z12", "x34", "y34", "z34", "x56", "y56", "z56");
    }

    @Test
    public void testMapMany4() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .<String> mapMany((String value) -> { throw new Exception("x"); })
        .then(keep)
        .start(1);

        keep.assertException(new Exception("x"));
    }

    @Test
    public void testMapMany5() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .mapMany((String value) -> Builder.<String> error(new Exception("x")))
        .then(keep)
        .start(1);

        keep.assertException(new Exception("x"));
    }

    @Test
    public void testFilter1() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .then(new FilterOp<String>() {
            @Override
            public boolean check(String value) {
                return value.equals("34");
            }
        })
        .then(keep)
        .start(1);

        keep.assertEquals("34");
    }

    @Test
    public void testFilter2() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .filter(x -> x.equals("34") )
        .then(keep)
        .start(1);

        keep.assertEquals("34");
    }

    
    @Test
    public void testFilter3() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .continueWithError(new Exception("xx"))
        .filter(x -> x.equals("34") )
        .then(keep)
        .start(1);

        keep.assertException(new Exception("xx"), "34");
    }

    @Test
    public void testFilter4() {
        Keep<String> keep = new Keep<>();
        Builder
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
        .then(keep)
        .start(1);

        keep.assertException(new Exception("xx"), "34");
    }

    @Test
    public void testLast() {
        Keep<String> keep = new Keep<>();
        Builder
        .from("12", "34", "56")
        .last()
        .then(keep)
        .start(1);

        keep.assertEquals("56");
    }

    @Test
    public void testLast2() {
        Keep<String> keep = new Keep<>();
        Builder
        .<String> from()
        .last()
        .then(keep)
        .start(1);

        keep.assertEquals();
    }

    @Test
    public void testSkip() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .skip(1)
        .then(keep)
        .start(1);

        keep.assertEquals(2, 3);
    }

    @Test
    public void testSkip2() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .skip(5)
        .then(keep)
        .start(1);

        keep.assertEquals();
    }

    @Test
    public void testTake() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .take(2)
        .then(keep)
        .start(1);

        keep.assertEquals(1, 2);
    }

    @Test
    public void testTake2() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .take(5)
        .then(keep)
        .start(1);

        keep.assertEquals(1, 2, 3);
    }

    @Test
    public void testAccumulator1() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .then(new AccumulatorOp<Integer>(0) {
            @Override
            public Integer calc(Integer value, Integer nextValue) {
                return value + nextValue;
            }
        })
        .then(keep)
        .start(1);

        keep.assertEquals(0, 1, 3, 6);
    }

    @Test
    public void testAccumulator2() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .accumulate(0, (value, next) -> value + next)
        .then(keep)
        .start(1);

        keep.assertEquals(0, 1, 3, 6);
    }

    @Test
    public void testAccumulator3() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .accumulate(null, (value, next) -> value + next)
        .then(keep)
        .start(1);

        keep.assertEquals(1, 3, 6);
    }
    
    @Test
    public void testAccumulator4() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .accumulate(0, (value, next) -> {
            if (next == 3) {
                throw new Exception("xx");
            }
            return value + next;
        })
        .then(keep)
        .start(1);
        keep.assertException(new Exception("xx"), 0, 1, 3);
    }
    
    @Test
    public void testConcat() {
        Keep<Integer> keep = new Keep<>();

        Builder
        .concat(Builder.from(1, 2, 3), Builder.from(4, 5))
        .then(keep)
        .start(1);

        keep.assertEquals(1, 2, 3, 4, 5);
    }


    @Test
    public void testWhenDoneValue1() {
        Keep<Integer> keep = new Keep<>();

        Builder
        .from(1, 2, 3)
        .whenDoneValue(4)
        .then(keep)
        .start(1);

        keep.assertEquals(4);
    }
    
    @Test
    public void testWhenDoneError1() {
        Keep<Integer> keep = new Keep<>();

        Builder
        .from(1, 2, 3)
        .whenDoneError(new Exception("xx"))
        .then(keep)
        .start(1);

        keep.assertException(new Exception("xx"));
    }
    
    @Test
    public void testWhenDoneProc1() {
        Keep<Integer> keep = new Keep<>();

        Builder
        .from(1, 2, 3)
        .whenDone(() -> { keep.doNext(44); })
        .then(keep)
        .start(1);

        keep.assertEquals(44);
    }
    
    @Test
    public void testWhenDonePublisher1() {
        Keep<Integer> keep = new Keep<>();

        Builder
        .from(1, 2, 3)
        .whenDone(Builder.from(5, 6, 7))
        .then(keep)
        .start(1);

        keep.assertEquals(5, 6, 7);
    }
    
    @Test
    public void testWhenDonePublisher2() {
        Keep<Integer> keep = new Keep<>();

        Builder
        .from(1, 2, 3)
        .whenDone(Builder.counter())
        .take(100000)
        .then(keep)
        .start(1);
    }
    
    @Test
    public void testContinueWithError1() {
        Keep<Integer> keep = new Keep<>();
        Builder
        .from(1, 2, 3)
        .continueWithError(new Exception("xx"))
        .then(keep)
        .start(1);
        
        keep.assertException(new Exception("xx"), 1, 2, 3);
    }
   /*
    @Test
    public void testWhenDone2() {
        Keep<Integer> keep = new Keep<>();

        Builder
        .from(1, 2, 3)
        .whenDone(new Proc0() {
            @Override
            public void apply() {
            	onNext(4);
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

        Builder.from(1, 2, 3)
        .<Integer> whenDone(new Builder.Proc0() {
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

        Builder
        .from(1, 2, 3)
        .whenDone(Builder.from(4, 5, 6))
        .then(keep);

        keep.assertEquals(4, 5, 6);
    }
     */    
    @Test
    public void testDelegate1() {
        Keep<Integer> keep = new Keep<>();

        Keep<Integer> keep2 = new Keep<>();

        Builder
        .from(1, 2, 3)
        .delegate(keep)
        //.next(keep)
        //.debug()
        //.stdout("xxx")
        .then(keep2)
        .start(1);

        keep.assertEquals(1, 2, 3);
        keep2.assertEquals(1, 2, 3);
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void testToList1() {
        Keep<List<Integer>> keep = new Keep<>();

        Builder
        .from(1, 2, 3)
        .toList()
        .then(keep)
        .start(1);

        keep.assertEquals(Arrays.asList(1, 2, 3));
    }
    
    /*    
    @Test
    public void testQueue() {
        final Keep<Integer> keep = new Keep<>();

        final Queue q = new Queue();
        q.add(new Runnable() {
            @Override
            public void run() {
                Builder
                .from(1, 2, 3)
                .then(new Chain.Counting<Integer, Integer>() {
                    @Override
                    public void onNext(final Integer value) {
                        super.onNext(value);
                        q.add(new Runnable() {
                            @Override
                            public void run() {
                                next(value + 10);
                            }
                        });
                    }
                })
                //.stdout("xx")
                .then(keep)
                .whenDone(new Builder.Proc0()   {
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

        Builder
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
        return Builder
        .<Integer>pipe()
        .map(new Builder.MapFunction<Integer, Integer>() {
            @Override
            public Integer map(Integer value) throws Exception {
                return value + 10;
            }
        })
        .map(new Builder.MapFunction<Integer, String>() {
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

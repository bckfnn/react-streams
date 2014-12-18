package io.github.bckfnn.reactstreams.test;

import io.github.bckfnn.reactstreams.Stream;

import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

/**
 * Test parts of the TCK
 */
public class TckTest {

    /**
     * Test sequential call to onXX methods.
     */
    @Test
    public void testSeq() {
        Stream<Integer> op = Stream.counter(1).take(3);

        List<String> res = new ArrayList<String>();

        op.subscribe(new Subscriber<Integer>() {
            Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                res.add("preOnSubscribe");
                s.request(1);
                res.add("postOnSubscribe");
            }

            @Override
            public void onNext(Integer t) {
                res.add("preOnNext-" + t);
                s.request(1);
                res.add("postOnNext-" + t);
            }

            @Override
            public void onError(Throwable t) {
                res.add("preOnError");
            }

            @Override
            public void onComplete() {
                res.add("preOnComplete");
            }
        });
        System.out.println(res);
    }
    
    /**
     * Test max.
     */
    @Test
    public void testMax() {
        Stream<Integer> op = Stream.counter(1).take(3);

        List<String> res = new ArrayList<String>();

        op.subscribe(new Subscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                res.add("preOnSubscribe");
                s.request(Long.MAX_VALUE);
                res.add("postOnSubscribe");
            }

            @Override
            public void onNext(Integer t) {
                res.add("preOnNext-" + t);
                res.add("postOnNext-" + t);
            }

            @Override
            public void onError(Throwable t) {
                res.add("preOnError");
            }

            @Override
            public void onComplete() {
                res.add("preOnComplete");
            }
        });
        System.out.println(res);
    }

}

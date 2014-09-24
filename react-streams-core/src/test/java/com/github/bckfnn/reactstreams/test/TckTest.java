package com.github.bckfnn.reactstreams.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.Builder;
import com.github.bckfnn.reactstreams.Operations;

/**
 * Test parts of the TCK
 */
public class TckTest {

    /**
     * Test sequential call to onXX methods.
     */
    @Test
    public void testSeq() {
        Operations<Integer> op = Builder.counter(1).take(3);

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
    
    @Test
    public void testMax() {
        Operations<Integer> op = Builder.counter(1).take(3);

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

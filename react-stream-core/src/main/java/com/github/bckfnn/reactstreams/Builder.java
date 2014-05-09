package com.github.bckfnn.reactstreams;

import java.util.Collection;
import java.util.List;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.bckfnn.reactstreams.ops.AccumulatorOp;
import com.github.bckfnn.reactstreams.ops.ConcatOp;
import com.github.bckfnn.reactstreams.ops.ContinueWithErrorOp;
import com.github.bckfnn.reactstreams.ops.CounterOp;
import com.github.bckfnn.reactstreams.ops.DelegateOp;
import com.github.bckfnn.reactstreams.ops.DoneOp;
import com.github.bckfnn.reactstreams.ops.FilterOp;
import com.github.bckfnn.reactstreams.ops.FromArrayOp;
import com.github.bckfnn.reactstreams.ops.FromErrorOp;
import com.github.bckfnn.reactstreams.ops.FromIteratorOp;
import com.github.bckfnn.reactstreams.ops.FromValueOp;
import com.github.bckfnn.reactstreams.ops.LastOp;
import com.github.bckfnn.reactstreams.ops.MapManyOp;
import com.github.bckfnn.reactstreams.ops.MapOp;
import com.github.bckfnn.reactstreams.ops.NopOp;
import com.github.bckfnn.reactstreams.ops.SkipOp;
import com.github.bckfnn.reactstreams.ops.TakeOp;
import com.github.bckfnn.reactstreams.ops.WhenDoneValueOp;
import com.github.bckfnn.reactstreams.ops.ZipOp;

/**
 * Main class for using <code>RVX</code> classes.
 * @param <T> type of builder.
 */
public class Builder<T> implements Operations<T>, Publisher<T> {
    private Publisher<T> publisher;

    /**
     * Private constructor, used when creating a new Builder.
     * @param publisher the publisher.
     */
    protected Builder(Publisher<T> publisher) {
        this.publisher = publisher;
    }

    /**
     * Create and return a new <code>Builder</code> that emit a single value.
     * @param value the value.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    public static <T> Operations<T> from(T value) {
        return new Builder<T>(new FromValueOp<>(value));
    }

    /**
     * Create and return a new <code>Builder</code> that emit a series of values.
     * @param values the values.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    @SafeVarargs
    public static <T> Operations<T> from(T... values) {
        return new Builder<T>(new FromArrayOp<>(values));
    }

    /**
     * Create and return a new <code>Builder</code> that emit the values from a <code>Collection</code>.
     * @param collection the collection.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    public static <T> Operations<T> from(Collection<T> collection) {
        return new Builder<T>(new FromIteratorOp<T>(collection.iterator()));
    }

    /**
     * Create and return a new <code>Builder</code> that emit the supplied exception.
     * @param error the error.
     * @param <T> type of builder.
     * @return the new builder.
     */
    public static <T> Operations<T> error(Throwable error) {
        return new Builder<T>(new FromErrorOp<>(error));
    }

    /**
     * Create and return a new <code>Builder</code> that concatenate all the values from all the supplied <code>Publishers</code>.
     * @param list the list of publishers.
     * @param <T> type of the builder.
     * @return the new builder.
     */
    @SafeVarargs
    public static <T> Operations<T> concat(Publisher<T>... list) {
        return new Builder<T>(new ConcatOp<T>(list));
    }

    /**
     * Create and return a new <code>Builder</code> that emit all integers from 0.
     * @return the new builder.
     */
    public static Operations<Integer> counter() {
        return new Builder<>(new CounterOp(0));
    }

    /**
     * Create and return a new <code>Builder</code> that emit all integers from the specified <code>start</code> value.
     * @param start the start value.
     * @return the new builder.
     */
    public static Operations<Integer> counter(int start) {
        return new Builder<>(new CounterOp(start));
    }

    /**
     * Create and return a new <code>Builder</code> that zip two <code>Publishers</code> together by
     * emitting a <code>Tuple</code> with a value from each publisher.
     * @param p1 the first publisher.
     * @param p2 the second publisher.
     * @param <T1> type of the first publisher.
     * @param <T2> type of the second publisher.
     * @return the new builder.
     */
    public static <T1, T2> Operations<Tuple<T1, T2>> zip(Publisher<T1> p1, Publisher<T2> p2) {
        return new Builder<Tuple<T1, T2>>(new ZipOp<T1, T2>(p1, p2));
    }


    @Override
    public <R> Operations<R> then(Processor<T, R> processor) {
        publisher.subscribe(processor);
        return new Builder<R>(processor);
    }


    @Override
    public void then(Subscriber<T> subscriber) {
        publisher.subscribe(subscriber);
    }

    /*
     * map operations.
     */

    @Override
    public <R> Operations<R> map(final Func1<T, R> mapFunc) {
        return then(new MapOp<T, R>() {
            @Override
            public R map(T value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }

    @Override
    public <R> Operations<R> mapMany(final Func1<T, Operations<R>> mapFunc) {
        return then(new MapManyOp<T, R>() {
            @Override
            public Operations<R> map(T value) throws Throwable {
                return mapFunc.apply(value);
            }
        });
    }

    /*
     * Filter operations.
     */
    @Override
    public Operations<T> last() {
        return then(new LastOp<T>());
    }

    @Override
    public Operations<T> skip(int cnt) {
        return then(new SkipOp<T>(cnt));
    }

    @Override
    public Operations<T> take(int cnt) {
        return then(new TakeOp<T>(cnt));
    }

    @Override
    public Operations<T> nop() {
        return then(new NopOp<T>());
    }

    @Override
    public Operations<T> done() {
        return then(new DoneOp<T>());
    }

    @Override
    public Operations<T> filter(Func1<T, Boolean> func) {
        return then(new FilterOp<T>() {
            @Override
            public boolean check(T value) throws Throwable {
                return func.apply(value);
            }
        });
    }

    @Override
    public <R> Operations<R> whenDone(R value) {
        return then(new WhenDoneValueOp<T, R>(value));
    }

    @Override
    public <R> Operations<R> whenDone(Publisher<R> publisher) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Operations<T> continueWithError(Throwable error) {
        return then(new ContinueWithErrorOp<>(error));
    }

    @Override
    public Operations<T> delegate(Subscriber<T> x) {
        return then(new DelegateOp<T>(x));
    }

    @Override
    public Operations<T> stdout(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Operations<List<T>> toList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Operations<T> accumulate(T initial, final Func2<T, T, T> func) {
        return then(new AccumulatorOp<T>(initial) {
            @Override
            public T calc(T value, T nextValue) throws Throwable {
                return func.apply(value, nextValue);
            }
        });
    }

    @Override
    public void subscribe(Subscriber<T> s) {
        publisher.subscribe(s);
    }

    public void start(int elements) {
        subscribe(new NopOp<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                super.onSubscribe(s);
                s.request(elements);
            }

            public String toString() {
                return "StartOp[" + elements + "]";
            }
        });
    }
}

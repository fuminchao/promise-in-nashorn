package com.github.fuminchao.nashorn.bindings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Promise {

    private CountDownLatch latch = new CountDownLatch(1);
    private AtomicReference resolved = new AtomicReference();

    public Promise(BiConsumer<Consumer<Object>, Consumer<Object>> fn) {

        Consumer<Object> resolve = (obj) -> {

            if (obj instanceof Promise) {
                obj = ((Promise) obj).await();
            }

            resolved.set(obj);
            latch.countDown();
        };

        Consumer<Object> reject = (err) -> {

            latch.countDown();
            //TODO
        };

        Runnable run = () -> {

            try {
                fn.accept(resolve, reject);
            } catch (Exception e) {
                reject.accept(e);
            }

        };

        CompletableFuture.runAsync(run);

//        run.run();
    }

    public Promise then(Function<Object, Object> fn) {

        Promise prev = this;
        return new Promise((resolve, reject) -> {

            Object o = prev.await();

            try {
                resolve.accept(fn.apply(o));
            } catch (Exception e) {
                reject.accept(e);
            }
        });
    }

    public Object await() {

        try {
            latch.await();
        } catch (InterruptedException e) {

        }

        return resolved.get();
    }

    public static Promise resolve(Object o) {
        return new Promise((r, j) -> {
           r.accept(o);
        });
    }

    public static Promise all(List<Promise> allPromise) {

        return new Promise((resolve, reject) -> {

            Object[] results = new Object[allPromise.size()];

            AtomicInteger i = new AtomicInteger(0);
            allPromise
                    .stream()
                    .map(p -> new Pair<>(i.getAndIncrement(), p))
                    .collect(Collectors.toCollection(ArrayList::new))
                    .parallelStream()
                    .forEach(pr -> {

                        results[pr.key] = pr.value.await();
                    });

            resolve.accept(EngineUtils.toJsArray(results));
        });
    }

    public static Promise race(List<Promise> anyPromise) {

        return new Promise((resolve, reject) -> {

            AtomicBoolean noneIsDone = new AtomicBoolean(true);

            AtomicInteger i = new AtomicInteger(0);
            anyPromise
                    .parallelStream()
                    .forEach(pr -> {

                        Object r = pr.await();

                        if (noneIsDone.getAndSet(false)) {
                            resolve.accept(r);
                        }
                    });
        });
    }

    private static class Pair<K, V> {

        private final K key;
        private final V value;

        Pair(K k, V v) {
            this.key = k;
            this.value = v;
        }
    }
}

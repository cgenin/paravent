package net.christophe.genin.spring.boot.paravent.queue.core.util;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;


public class FuturesTest {

    @Test
    public void fromSingleWithResult() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = Futures.from(Single.just("test"));
        assertThat(future.get(1, TimeUnit.SECONDS)).isEqualTo("test");
    }

    @Test(expected = ExecutionException.class)
    public void fromSingleWithException() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = Futures.from(Single.<String>error(new IllegalStateException()));
        future.get(1, TimeUnit.SECONDS);
    }

    @Test(expected = ExecutionException.class)
    public void fromObservableWithException() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<List<String>> future = Futures.from(Observable.create(obs->{
            obs.onNext("test1");
            obs.onNext("test2");
            obs.onError(new IllegalStateException());
        }));
        future.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void fromObservableWithResult() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<List<String>> future = Futures.from(Observable.create(obs->{
            obs.onNext("test1");
            obs.onNext("test2");
            obs.onComplete();
        }));
        assertThat(future.get(1, TimeUnit.SECONDS)).hasSize(2).contains("test1", "test2");
    }
}

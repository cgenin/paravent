package net.christophe.genin.spring.boot.paravent.queue.core.util;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class Futures {

    public static <T> CompletableFuture<T> from(Single<T> single) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        single.subscribe(future::complete, future::completeExceptionally);
        return future;
    }

    public static <T> CompletableFuture<List<T>> from(Observable<T> single) {
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        final List<T> list = new ArrayList<>();
        single.subscribe(list::add, future::completeExceptionally, () -> future.complete(list));
        return future;
    }
}

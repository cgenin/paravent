package net.christophe.genin.spring.boot.paravent.queue.core.util;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.vavr.control.Either;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface SingleSync<T> {

    static <T> SingleSync<T> fromSingle(Single<T> single) {
        Objects.requireNonNull(single);
        final SingleSyncImpl<T> observer = new SingleSyncImpl<>();
        single.subscribe(observer);
        return observer;
    }


    Either<Throwable, T> either();

    class SingleSyncImpl<T> extends CompletableFuture<T>
            implements SingleObserver<T>, SingleSync<T> {

        private Either<Throwable, T> either = Either.left(new IllegalStateException("Not Disposed"));


        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(T t) {
            either = Either.right(t);
            this.complete(t);
        }


        @Override
        public void onError(Throwable e) {
            either = Either.left(e);
            this.complete(null);
        }

        public Either<Throwable, T> either() {
            this.join();
            return either;
        }
    }
}

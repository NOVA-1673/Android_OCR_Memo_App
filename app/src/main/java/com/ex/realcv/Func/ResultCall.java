package com.ex.realcv.Func;

public interface ResultCall<T> {
    final class Success<T> implements ResultCall<T> {
        public final T data;
        public Success(T data) { this.data = data; }
    }

    public final class SuccessCall implements ResultCall<Void> {
        public static final SuccessCall INSTANCE = new SuccessCall();
        private SuccessCall() {}
    }

    final class Error<T> implements ResultCall<T> {
        public final Throwable error;
        public Error(Throwable error) { this.error = error; }
    }
}

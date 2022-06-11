package com.github.lianjiatech.retrofit.spring.boot.core.reactive;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author 陈添明
 * @since 2022/6/9 8:53 下午
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MonoCallAdapterFactory extends CallAdapter.Factory {

    public static final MonoCallAdapterFactory INSTANCE = new MonoCallAdapterFactory();

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Mono.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "Mono return type must be parameterized"
                            + " as Mono<Foo> or Mono<? extends Foo>");
        }
        Type innerType = getParameterUpperBound(0, (ParameterizedType)returnType);

        if (getRawType(innerType) != Response.class) {
            // Generic type is not Response<T>. Use it for body-only adapter.
            return new BodyCallAdapter<>(innerType);
        }

        // Generic type is Response<T>. Extract T and create the Response version of the adapter.
        if (!(innerType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "Response must be parameterized" + " as Response<Foo> or Response<? extends Foo>");
        }
        Type responseType = getParameterUpperBound(0, (ParameterizedType)innerType);
        return new ResponseCallAdapter<>(responseType);
    }

    private static class BodyCallAdapter<R> implements CallAdapter<R, Mono<R>> {

        private final Type responseType;

        public BodyCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public Mono<R> adapt(Call<R> call) {
            return Mono.create(monoSink -> call.enqueue(new BodyCallback(monoSink)));
        }

        private class BodyCallback implements Callback<R> {

            private final MonoSink<R> monoSink;

            public BodyCallback(MonoSink<R> monoSink) {
                this.monoSink = monoSink;
            }

            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                if (response.isSuccessful()) {
                    monoSink.success(response.body());
                } else {
                    monoSink.error(new HttpException(response));
                }
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                monoSink.error(t);
            }
        }

    }

    private static class ResponseCallAdapter<R> implements CallAdapter<R, Mono<Response<R>>> {

        private final Type responseType;

        public ResponseCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public Mono<Response<R>> adapt(Call<R> call) {
            return Mono.create(monoSink -> call.enqueue(new ResponseCallback(monoSink)));
        }

        private class ResponseCallback implements Callback<R> {

            private final MonoSink<Response<R>> monoSink;

            public ResponseCallback(MonoSink<Response<R>> monoSink) {
                this.monoSink = monoSink;
            }

            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                monoSink.success(response);
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                monoSink.error(t);
            }
        }
    }
}

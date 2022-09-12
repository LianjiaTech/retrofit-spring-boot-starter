package com.github.lianjiatech.retrofit.spring.boot.core.reactive;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.github.lianjiatech.retrofit.spring.boot.core.InternalCallAdapterFactory;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.annotations.NonNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * @author 陈添明
 * @since 2022/6/10 8:08 上午
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Rxjava2SingleCallAdapterFactory extends CallAdapter.Factory implements InternalCallAdapterFactory {

    public static final Rxjava2SingleCallAdapterFactory INSTANCE = new Rxjava2SingleCallAdapterFactory();

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Single.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "Mono return type must be parameterized"
                            + " as Single<Foo> or Single<? extends Foo>");
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

    private class BodyCallAdapter<R> implements CallAdapter<R, Single<R>> {

        private final Type responseType;

        public BodyCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public Single<R> adapt(Call<R> call) {

            return Single.create(emitter -> call.enqueue(new BodyCallBack(emitter)));
        }

        private class BodyCallBack implements Callback<R> {

            private final SingleEmitter<R> emitter;

            public BodyCallBack(@NonNull SingleEmitter<R> emitter) {
                this.emitter = emitter;
            }

            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                if (response.isSuccessful()) {
                    emitter.onSuccess(response.body());
                } else {
                    emitter.onError(new HttpException(response));
                }
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                emitter.onError(t);
            }
        }
    }

    private class ResponseCallAdapter<R> implements CallAdapter<R, Single<Response<R>>> {

        private final Type responseType;

        public ResponseCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public Single<Response<R>> adapt(Call<R> call) {
            return Single.create(emitter -> call.enqueue(new ResponseCallBack(emitter)));
        }

        private class ResponseCallBack implements Callback<R> {

            private final SingleEmitter<Response<R>> emitter;

            public ResponseCallBack(@NonNull SingleEmitter<Response<R>> emitter) {
                this.emitter = emitter;
            }

            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                emitter.onSuccess(response);
            }

            @Override
            public void onFailure(Call<R> call, Throwable t) {
                emitter.onError(t);
            }
        }
    }
}

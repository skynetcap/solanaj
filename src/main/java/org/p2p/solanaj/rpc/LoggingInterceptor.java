package org.p2p.solanaj.rpc;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Logger;

public class LoggingInterceptor implements Interceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingInterceptor.class.getName());

    @NotNull
    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        LOGGER.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        System.out.println("Buffer = " + bodyToString(request));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        LOGGER.info(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }

    private static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
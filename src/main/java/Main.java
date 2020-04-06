import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10_000_000; i++) {
            var j = i;
            HttpClient.create()
                    .headers(h -> h.set(HttpHeaderNames.CONTENT_TYPE, "application/json"))
                    .tcpConfiguration(tcpClient -> setTcpOptions(tcpClient))
                    .get()
                    .uri("https://api.covid19api.com/")
                    .response((resp, respContent) -> respContent.aggregate().asString())
                    .doOnNext(respBody -> System.out.println(j + ": " + respBody.substring(0, 20)))
                    .single().block();
            Thread.sleep(1500);
        }

    }


    public static TcpClient setTcpOptions(TcpClient tcpClient) {
        return tcpClient
                .secure(spec -> spec.sslContext(SslContextBuilder.forClient()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(20_000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(20_000, TimeUnit.MILLISECONDS)))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000);
    }

}

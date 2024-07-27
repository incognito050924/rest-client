package io.incognito.rest.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLException;

import io.incognito.rest.client.util.Opt;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@RequiredArgsConstructor
public abstract class HttpClientConfigurer {
    protected final int connectionTimeoutSeconds;
    protected final int readTimeoutSeconds;
    protected final int writeTimeoutSeconds;
    protected final int maxContentLength;
    protected final int maxConnections;

    public abstract ConnectionObserver connectionObserver();
    public abstract ObjectMapper webClientObjectMapper();

    /**
     * HTTP Connection Pool 설정
     *
     * @return ConnectionProvider
     */
    public ConnectionProvider httpConnectionPool() {
        return ConnectionProvider
                .builder("custom-conn-pool")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Netty Http Client 설정
     *
     * @return HttpClient
     */
    public HttpClient httpApiClient() {
        return HttpClient.create(httpConnectionPool())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofSeconds(connectionTimeoutSeconds).toMillis())
                .option(ChannelOption.SO_RCVBUF, maxContentLength) // 수신 버퍼 크기
                .option(ChannelOption.SO_SNDBUF, maxContentLength) // 송신 버퍼 크기
                // 서버 비정상 세션 종료 확인 설정
                .option(EpollChannelOption.SO_KEEPALIVE, true) // 세션 종료 체크 여부
                .option(EpollChannelOption.TCP_KEEPIDLE, 300) // 최초 세션 종료 체크 시작 시간 (sec.)
                .option(EpollChannelOption.TCP_KEEPINTVL, 60) // 세션 종료 체크 기간 (interval sec.)
                .option(EpollChannelOption.TCP_KEEPCNT, 5) // 최대 세션 체크 횟수
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutSeconds))
                )
                .observe(Opt.of(connectionObserver()).orElse(ConnectionObserver.emptyListener()))
                .secure(spec -> {
                    try {
                        spec.sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build())
                                .handshakeTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                                .closeNotifyReadTimeout(Duration.ofSeconds(readTimeoutSeconds))
                                .closeNotifyFlushTimeout(Duration.ofSeconds(writeTimeoutSeconds));
                    } catch (final SSLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @SuppressWarnings("deprecation")
    public WebClient apiWebClient(final List<MimeType> serializeMimeTypes, final List<MimeType> deserializeMimeTypes) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpApiClient()))
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(maxContentLength);
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(webClientObjectMapper(), Optional.ofNullable(serializeMimeTypes).map(list -> list.toArray(new MimeType[0])).orElse(new MediaType[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(webClientObjectMapper(), Optional.ofNullable(deserializeMimeTypes).map(list -> list.toArray(new MimeType[0])).orElse(new MediaType[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8})));
                })
                .build();
    }
}

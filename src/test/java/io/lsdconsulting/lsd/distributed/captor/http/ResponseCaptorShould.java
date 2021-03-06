package io.lsdconsulting.lsd.distributed.captor.http;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

class ResponseCaptorShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final SourceTargetDeriver sourceTargetDeriver = mock(SourceTargetDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);
    private final HttpRequest httpRequest = mock(HttpRequest.class);
    private final ClientHttpResponse clientHttpResponse = mock(ClientHttpResponse.class);

    private final PathDeriver pathDeriver = new PathDeriver();
    private final InterceptedInteractionFactory interceptedInteractionFactory = new InterceptedInteractionFactory("profile");

    private final ResponseCaptor underTest = new ResponseCaptor(interceptedDocumentRepository, interceptedInteractionFactory, sourceTargetDeriver, pathDeriver, traceIdRetriever);

    private final String url = randomAlphanumeric(20);
    private final String body = randomAlphanumeric(20);
    private final String traceId = randomAlphanumeric(20);
    private final String target = randomAlphanumeric(20);
    private final String path = randomAlphanumeric(20);
    private final Map<String, Collection<String>> requestHeaders = Map.of("b3", List.of(traceId), "Target-Name", List.of(target));

    private final Response response = Response.builder()
            .request(Request.create(GET, url, requestHeaders, body.getBytes(), defaultCharset(), new RequestTemplate( )))
            .build();

    @Test
    public void takeTraceIdFromRequestHeaders() {
        given(traceIdRetriever.getTraceId(eq(requestHeaders))).willReturn(traceId);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(response);

        assertThat(interceptedInteraction.getTraceId(), is(traceId));
    }

    @Test
    public void deriveTargetFromRequestHeaders() {
       given(sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq(url))).willReturn(target);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(response);

        assertThat(interceptedInteraction.getTarget(), is(target));
    }

    @Test
    public void handleEmptyResponseBodyFromDeleteRequest() throws IOException {
        HttpHeaders httpHeaders = mock(HttpHeaders.class);
        given(httpRequest.getHeaders()).willReturn(httpHeaders);
        given(clientHttpResponse.getHeaders()).willReturn(httpHeaders);
        given(clientHttpResponse.getStatusCode()).willReturn(NO_CONTENT);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(httpRequest, clientHttpResponse, target, path, traceId);

        assertThat(interceptedInteraction.getBody(), is(emptyString()));
    }
}
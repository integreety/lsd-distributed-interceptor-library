package com.integreety.yatspec.e2e.captor.rabbit;

import com.integreety.yatspec.e2e.captor.http.derive.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteractionFactory;
import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;
import java.util.Map;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.PUBLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

class RabbitCaptorShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final PropertyServiceNameDeriver propertyServiceNameDeriver = mock(PropertyServiceNameDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);

    private final InterceptedInteractionFactory interceptedInteractionFactory = new InterceptedInteractionFactory("profile");
    private final HeaderRetriever headerRetriever = new HeaderRetriever();


    private final RabbitCaptor underTest = new RabbitCaptor(interceptedDocumentRepository, interceptedInteractionFactory, propertyServiceNameDeriver, headerRetriever, traceIdRetriever);

    private final String exchange = randomAlphabetic(20);
    private final String serviceName = randomAlphabetic(20);
    private final String traceId = randomAlphabetic(20);
    private final String body = randomAlphabetic(20);
    private final MessageProperties messageProperties = new MessageProperties();
    private final Message message = new Message(body.getBytes(), messageProperties);
    private final Map<String, List<String>> headers = Map.of("name", List.of("value"));

    @Test
    void captureAmqpInteraction() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        messageProperties.setHeader("name", "value");

        final InterceptedInteraction result = underTest.captureInteraction(exchange, message, PUBLISH);

        assertThat(result.getPath(), is(exchange));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getType(), is(PUBLISH));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(headers));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));
    }
}
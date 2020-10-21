package com.integreety.yatspec.e2e.integration.testapp.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "testClient", url = "http://localhost:${wiremock.server.port}")
public interface TestClient {

    @PostMapping("/external-objects?message=from_feign")
    void post(String message);
}
package com.integreety.yatspec.e2e.captor.http.mapper;

public class PropertyServiceNameDeriver {

    // TODO This should be configurable
    private static final String DEFAULT_SERVICE_NAME_SUFFIX_TO_REMOVE = " Service";

    private final String appName;

    public PropertyServiceNameDeriver(final String appName) {
        this.appName = appName
                .replaceAll(DEFAULT_SERVICE_NAME_SUFFIX_TO_REMOVE, "")
                .replaceAll(" ", "");
    }

    public String getServiceName() {
        return appName;
    }
}

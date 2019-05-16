package org.testcontainers.containers.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class Mail {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Content")
    private Content content;

    @JsonProperty("Content.Headers")
    private Map<String, List<String>> headers;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @EqualsAndHashCode
    @ToString
    public static class Content {
        @JsonProperty("Headers")
        private Map<String, List<String>> headers;
        @JsonProperty("Body")
        private String body;
    }
}

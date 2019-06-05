package org.testcontainers.containers.model;

import static org.testcontainers.containers.MailHogContainer.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

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

    public ZonedDateTime getDate() {
        List<String> date = content.getHeaders().get(MAIL_HEADER_DATE);
        return ZonedDateTime.parse(date.get(0).replaceFirst(" \\(.+\\)$", StringUtils.EMPTY), DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public String getSubject() {
        List<String> subjectList = content.getHeaders().get(MAIL_HEADER_SUBJECT);
        if (subjectList != null && !subjectList.isEmpty()) {
            return subjectList.get(0);
        } else {
            return StringUtils.EMPTY;
        }
    }

    public List<String> getTo() {
        return getHeaderProperty(MAIL_HEADER_TO);
    }

    public List<String> getCC() {
        return getHeaderProperty(MAIL_HEADER_CC);
    }

    public List<String> getHeaderProperty(String property) {
        List<String> header = content.getHeaders().get(property);
        if (header != null && header.size() == 1) {
            return Lists.newArrayList(header.get(0).split(", "));
        } else if(header != null && header.size() > 1) {
            return header;
        } else {
            return new ArrayList<>();
        }
    }
}

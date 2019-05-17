package org.testcontainers.containers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.testcontainers.containers.model.Mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import lombok.Getter;

public class MailHogContainer extends GenericContainer<MailHogContainer> {

    private static final String VERSION = "latest";

    @Getter
    private static final int SMTP_PORT = 1025;

    @Getter
    private static final int HTTP_PORT = 8025;
    private static final String ANY_ADDRESS = "0.0.0.0";
    private static final String MAILHOG_QUERY_PARAMETER_LIMIT = "limit";
    private static final String MAILHOG_QUERY_PARAMETER_KIND = "kind";
    private static final String MAILHOG_QUERY_PARAMETER_QUERY = "query";

    public MailHogContainer() {
        this(VERSION);
    }

    public MailHogContainer(String version) {
        super("mailhog/mailhog:" + version);
        withEnv("MH_SMTP_BIND_ADDR", ANY_ADDRESS + ":" + SMTP_PORT);
        withEnv("MH_UI_BIND_ADDR", ANY_ADDRESS + ":" + HTTP_PORT);
        withEnv("MH_API_BIND_ADDR", ANY_ADDRESS + ":" + HTTP_PORT);
        withExposedPorts(SMTP_PORT, HTTP_PORT);
    }

    public String getHttpEndpoint() {
        return String.format("http://%s:%d", getContainerIpAddress(), getHttpPort());
    }

    public String getSmtpEndpoint() {
        return String.format("%s:%d", getContainerIpAddress(), getSmtpPort());
    }

    public int getHttpPort() {
        return getMappedPort(HTTP_PORT);
    }

    public int getSmtpPort() {
        return getMappedPort(SMTP_PORT);
    }

    public List<Mail> getAllMessages() throws IOException, URISyntaxException {
        return getMessages(Integer.MAX_VALUE);
    }

    public List<Mail> getMessages(int limit) throws IOException, URISyntaxException {
        List<BasicNameValuePair> parameter = Collections.singletonList(new BasicNameValuePair(MAILHOG_QUERY_PARAMETER_LIMIT, Integer.toString(limit)));
        return getMessagesWithParameters(parameter);
    }

    public List<Mail> getMessagesFrom(String sender, int limit) throws URISyntaxException, IOException {
        ArrayList<BasicNameValuePair> query = Lists.newArrayList(
            new BasicNameValuePair(MAILHOG_QUERY_PARAMETER_KIND, "from"),
            new BasicNameValuePair(MAILHOG_QUERY_PARAMETER_QUERY, sender),
            new BasicNameValuePair(MAILHOG_QUERY_PARAMETER_LIMIT, Integer.toString(limit)));

        return getMessagesWithParameters(query);
    }

    public List<Mail> getNewestMessageFrom(String sender) throws URISyntaxException, IOException {
        return getMessagesFrom(sender, 1);
    }

    public List<Mail> getAllMessagesFrom(String sender) throws IOException, URISyntaxException {
        return getMessagesFrom(sender, Integer.MAX_VALUE);
    }

    public List<Mail> getMessagesWithParameters(List<? extends NameValuePair> parameters) throws URISyntaxException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        URIBuilder uri = new URIBuilder(getHttpEndpoint());

        boolean isSearch = parameters.stream()
            .map(NameValuePair::getName)
            .anyMatch(name -> Arrays.asList("kind", MAILHOG_QUERY_PARAMETER_QUERY).contains(name));

        if (isSearch) {
            uri.setPath("/api/v2/search");
        } else {
            uri.setPath("/api/v2/messages");
        }

        uri.setParameters(parameters.toArray(new NameValuePair[]{}));

        JsonNode jsonNode = mapper.readTree(uri.build().toURL());
        return Arrays.asList(mapper.treeToValue(jsonNode.get("items"), Mail[].class));
    }

}

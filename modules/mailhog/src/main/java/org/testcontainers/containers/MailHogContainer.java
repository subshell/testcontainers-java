package org.testcontainers.containers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.testcontainers.containers.model.Mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class MailHogContainer extends GenericContainer<MailHogContainer> {

    private static final String VERSION = "latest";

    private static final int SMTP_PORT = 1025;
    private static final int HTTP_PORT = 8025;
    private static final String ANY_ADDRESS = "0.0.0.0";

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

    public int getContainerHttpPort() {
        return HTTP_PORT;
    }

    public int getContainerSmtpPort() {
        return SMTP_PORT;
    }

    public List<Mail> getMessages() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        URL url = new URL(getHttpEndpoint() + "/api/v2/messages");
        JsonNode jsonNode = mapper.readTree(url);
        return Arrays.asList(mapper.treeToValue(jsonNode.get("items"), Mail[].class));
    }

    public List<Mail> getMailItems(String sender) throws URISyntaxException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        URIBuilder uri = new URIBuilder(getHttpEndpoint());
        uri.setPath("/api/v2/search");
        uri.setParameters(Lists.newArrayList(
            new BasicNameValuePair("kind", "from"),
            new BasicNameValuePair("query", sender)));

        JsonNode jsonNode = mapper.readTree(uri.build().toURL());
        return Arrays.asList(mapper.treeToValue(jsonNode.get("items"), Mail[].class));
    }
}

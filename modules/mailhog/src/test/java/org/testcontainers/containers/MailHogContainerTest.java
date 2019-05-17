package org.testcontainers.containers;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.model.Mail;

public class MailHogContainerTest {

    private static final String MAIL_HEADER_SUBJECT = "Subject";

    @Rule
    public MailHogContainer mailHog = new MailHogContainer();
    private Session session;

    @Before
    public void init() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", mailHog.getContainerIpAddress());
        prop.put("mail.smtp.port", mailHog.getSmtpPort());

        session = Session.getInstance(prop);
    }

    @Test
    public void testHttpResponse() {
        try {
            URL url = new URL(mailHog.getHttpEndpoint());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            MatcherAssert.assertThat("HTTP response code is not 200",
                connection.getResponseCode(),
                is(HttpStatus.SC_OK));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testMailSendSuccessful() throws MessagingException, IOException, URISyntaxException {
        String message = "Message text";
        String subject = MAIL_HEADER_SUBJECT;
        sendMessage("foo@foo.com", subject, message);

        List<Mail> messages = mailHog.getAllMessages();
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0).getContent().getBody(), is(message));
        assertThat(messages.get(0).getContent().getHeaders().get(subject).get(0), is(subject));
    }

    @Test
    public void testGetMailFromSender() throws MessagingException, IOException, URISyntaxException {
        String sender1 = "sender1@foo.com";
        String sender2 = "sender2@foo.com";
        String subject1 = "Test subject1";
        String subject2 = "Test subject2";
        String message1 = "Test message1";
        String message2 = "Test message2";

        sendMessage(sender1, subject1, message1);
        sendMessage(sender1, subject1, message1);
        sendMessage(sender2, subject2, message2);
        sendMessage(sender1, subject1, message1);

        List<Mail> mails = mailHog.getAllMessagesFrom(sender2);
        assertThat(mails.size(), is(1));
        assertThat(mails.get(0).getContent().getBody(), is(message2));
        assertThat(mails.get(0).getContent().getHeaders().get(MAIL_HEADER_SUBJECT).get(0), is(subject2));

        List<Mail> mailItemsSender2 = mailHog.getAllMessagesFrom(sender1);
        assertThat(mailItemsSender2.size(), is(3));
    }

    private void sendMessage(String sender, String subject, String message) throws MessagingException {
        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("foo@foo.com"));
        mimeMessage.setFrom(new InternetAddress(sender));
        mimeMessage.setSubject(subject);
        mimeMessage.setText(message);

        Transport.send(mimeMessage);
    }

}

package uk.gov.hmcts.cft.idam.notify;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdamNotificationClientTest {

    private final static String SIMPLE_KEY = MessageFormat.format("test_key-{0}-{1}", UUID.randomUUID().toString(), UUID.randomUUID().toString());

    IdamNotificationClient underTest = new IdamNotificationClient(SIMPLE_KEY);

    @Test
    void testExtractKeyName() {
        String test = MessageFormat.format("test_key-{0}-{1}", underTest.getServiceId(), underTest.getApiKey());
        assertEquals(Optional.of("test_key"), underTest.extractKeyName(test));
        test = MessageFormat.format("test_key--{0}-{1}", underTest.getServiceId(), UUID.randomUUID().toString());
        assertEquals(Optional.of("test_key"), underTest.extractKeyName(test));
        test = MessageFormat.format("{0}-{1}", underTest.getServiceId(), UUID.randomUUID().toString());
        assertEquals(Optional.empty(), underTest.extractKeyName(test));
        test = MessageFormat.format("-{0}-{1}", underTest.getServiceId(), UUID.randomUUID().toString());
        assertEquals(Optional.empty(), underTest.extractKeyName(test));
    }

    @Test
    void extractNotificationIdForNextPage_shouldExtractOlderThanValue() {
        String test = "https://test?reference=sidam_preview_dev&older_than=6790aa78-5dc9-4f50-9efb-8390f8adf560";
        assertEquals(Optional.of("6790aa78-5dc9-4f50-9efb-8390f8adf560"), underTest
            .extractNotificationIdForNextPage(test));
        test = "https://test?older_than=6790aa78-5dc9-4f50-9efb-8390f8adf560&reference=sidam_preview_dev";
        assertEquals(Optional.of("6790aa78-5dc9-4f50-9efb-8390f8adf560"), underTest
            .extractNotificationIdForNextPage(test));
    }

    @Test
    void extractNotificationIdForNextPage_shouldReturnEmptyIfNoValue() {
        assertEquals(Optional.empty(), underTest.extractNotificationIdForNextPage(null));
        assertEquals(Optional.empty(), underTest.extractNotificationIdForNextPage("https://test?older_than=&"));
    }

}

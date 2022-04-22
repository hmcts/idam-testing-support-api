package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationsServiceTest {

    @Mock
    NotificationClient notificationClient;

    @InjectMocks
    NotificationsService underTest;

    @BeforeEach
    public void setup() {
        underTest.setNotifyKey("test_key");
    }

    /**
     * @verifies extract older than value
     * @see NotificationsService#extractNotificationIdForNextPage(String)
     */
    @Test
    public void extractNotificationIdForNextPage_shouldExtractOlderThanValue() throws Exception {
        String test = "https://test?reference=sidam_preview_dev&older_than=6790aa78-5dc9-4f50-9efb-8390f8adf560";
        assertEquals(Optional.of("6790aa78-5dc9-4f50-9efb-8390f8adf560"), underTest
            .extractNotificationIdForNextPage(test));
        test = "https://test?older_than=6790aa78-5dc9-4f50-9efb-8390f8adf560&reference=sidam_preview_dev";
        assertEquals(Optional.of("6790aa78-5dc9-4f50-9efb-8390f8adf560"), underTest
            .extractNotificationIdForNextPage(test));
    }

    /**
     * @verifies return empty if no value
     * @see NotificationsService#extractNotificationIdForNextPage(String)
     */
    @Test
    public void extractNotificationIdForNextPage_shouldReturnEmptyIfNoValue() throws Exception {
        assertEquals(Optional.empty(), underTest.extractNotificationIdForNextPage(null));
        assertEquals(Optional.empty(), underTest.extractNotificationIdForNextPage("https://test?older_than=&"));
    }

    /**
     * @verifies find first notification for email
     * @see NotificationsService#findEmailInNotifications(String)
     */
    @Test
    public void findEmailInNotifications_shouldFindFirstNotificationForEmail() throws Exception {
        Notification notification = mock(Notification.class);
        when(notification.getEmailAddress()).thenReturn(Optional.of("test@email"));
        NotificationList currentPage = mock(NotificationList.class);
        when(currentPage.getNotifications()).thenReturn(Collections.singletonList(notification));
        when(notificationClient.getNotifications(any(), any(), any(), any())).thenReturn(currentPage);
        Optional<Notification> result = underTest.findEmailInNotifications("TEST@email");
        assertTrue(result.isPresent());
        assertEquals(result.get(), notification);
    }

    /**
     * @verifies return empty if the email is not found and there are no more pages to search
     * @see NotificationsService#findEmailInNotifications(String)
     */
    @Test
    public void findEmailInNotifications_shouldReturnEmptyIfTheEmailIsNotFoundAndThereAreNoMorePagesToSearch() throws Exception {
        NotificationList currentPage = mock(NotificationList.class);
        when(currentPage.getNotifications()).thenReturn(Collections.emptyList());
        when(currentPage.getNextPageLink())
            .thenReturn(Optional.of("http://test?older_than=1234"))
            .thenReturn(Optional.empty());
        when(notificationClient.getNotifications(any(), any(), any(), any())).thenReturn(currentPage);
        Optional<Notification> result = underTest.findEmailInNotifications("TEST@email");
        assertTrue(result.isEmpty());
    }

    /**
     * @verifies return empty if the email is not found and the page limit is reached
     * @see NotificationsService#findEmailInNotifications(String)
     */
    @Test
    public void findEmailInNotifications_shouldReturnEmptyIfTheEmailIsNotFoundAndThePageLimitIsReached() throws Exception {
        NotificationList currentPage = mock(NotificationList.class);
        when(currentPage.getNotifications()).thenReturn(Collections.emptyList());
        when(currentPage.getNextPageLink())
            .thenReturn(Optional.of("http://test?older_than=1234"));
        when(notificationClient.getNotifications(any(), any(), any(), any())).thenReturn(currentPage);
        Optional<Notification> result = underTest.findEmailInNotifications("TEST@email");
        assertTrue(result.isEmpty());
    }

    /**
     * @verifies return latest notification
     * @see NotificationsService#findLatestNotification(String)
     */
    @Test
    public void findLatestNotification_shouldReturnLatestNotification() throws Exception {
        Notification notification = mock(Notification.class);
        when(notification.getEmailAddress()).thenReturn(Optional.of("test@email"));
        NotificationList currentPage = mock(NotificationList.class);
        when(currentPage.getNotifications()).thenReturn(Collections.singletonList(notification));
        when(notificationClient.getNotifications(any(), any(), any(), any())).thenReturn(currentPage);
        Optional<Notification> result = underTest.findLatestNotification("TEST@email");
        assertTrue(result.isPresent());
        assertEquals(result.get(), notification);
    }

    /**
     * @verifies throw http status code exception
     * @see NotificationsService#findLatestNotification(String)
     */
    @Test
    public void findLatestNotification_shouldThrowHttpStatusCodeException() throws Exception {
        when(notificationClient.getNotifications(any(), any(), any(), any())).thenThrow(new NotificationClientException("test-exception"));
        try {
            underTest.findLatestNotification("TEST@email");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, hsce.getStatusCode());
            assertEquals("NotificationClientException; test-exception", hsce.getMessage());
        } catch (Exception e) {
            fail();
        }
    }
}

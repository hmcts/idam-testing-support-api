package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.notify.IdamNotificationClient;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

import java.util.Optional;

import static uk.gov.hmcts.cft.idam.notify.IdamNotificationClient.ALL_STATUSES;
import static uk.gov.hmcts.cft.idam.notify.IdamNotificationClient.EMAIL_TYPE;

@Slf4j
@Service
public class NotificationsService {

    @Value("${notify.maxPages}")
    private int maxPages;

    private final IdamNotificationClient notificationClient;

    @Value("${featureFlags.addEmailToNotifyReference:false}")
    boolean addEmailToNotifyReference;

    public NotificationsService(IdamNotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    /**
     * Find latest notification.
     * @should return latest notification
     * @should throw http status code exception
     */
    public Optional<Notification> findLatestNotification(String email) throws Exception {
        try {
            return findEmailInNotifications(email);
        } catch (NotificationClientException e) {
            log.error("Exception while finding notification for '{}'", email, e);
            throw SpringWebClientHelper.exception(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Find email in notifications.
     * @should find first notification for email
     * @should return empty if the email is not found and there are no more pages to search
     * @should return empty if the email is not found and the page limit is reached
     */
    protected Optional<Notification> findEmailInNotifications(String searchEmail) throws NotificationClientException {
        String olderThanNotificationId = null;
        int page = 0;
        while (page < maxPages) {
            String ref = notificationClient.getKeyName();
            if (addEmailToNotifyReference) {
                ref += "::" + searchEmail.toLowerCase();
            }
            NotificationList currentPage = notificationClient
                .getNotifications(ALL_STATUSES, EMAIL_TYPE, ref, olderThanNotificationId);
            if (CollectionUtils.isNotEmpty(currentPage.getNotifications())) {
                Optional<Notification> firstMatch = currentPage.getNotifications().stream()
                    .filter(n -> n.getEmailAddress().isPresent() && n.getEmailAddress().get()
                        .equalsIgnoreCase(searchEmail)).findFirst();
                if (firstMatch.isPresent()) {
                    return firstMatch;
                }
            }
            Optional<String> notificationIdForNextPage =
                notificationClient.extractNotificationIdForNextPage(currentPage.getNextPageLink().orElse(null));
            if (notificationIdForNextPage.isPresent()) {
                olderThanNotificationId = notificationIdForNextPage.get();
            } else {
                break;
            }
            page++;
        }

        return Optional.empty();
    }

}

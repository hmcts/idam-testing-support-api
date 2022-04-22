package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.NotificationList;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class NotificationsService {

    private static final String NOTIFY_KEY_REFERENCE_REGEX = "^[a-z]*[_a-z]*[_a-z]*";
    private static final Pattern OLDER_THAN_PATTERN = Pattern.compile("older_than=([A-Za-z0-9\\-]+)&?");

    private static final String ALL_STATUSES = null;
    private static final String EMAIL_TYPE = "email";
    private static final int MAX_PAGES = 20;

    private final NotificationClient notificationClient;

    private final String notifyReference;

    public NotificationsService(NotificationClient notificationClient, @Value("${notify.key}") String notifyKey) {
        this.notificationClient = notificationClient;
        this.notifyReference = extractNotifyReference(notifyKey).orElse(null);
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
        while (page < MAX_PAGES) {
            NotificationList currentPage = notificationClient
                .getNotifications(ALL_STATUSES, EMAIL_TYPE, notifyReference, olderThanNotificationId);
            if (CollectionUtils.isNotEmpty(currentPage.getNotifications())) {
                Optional<Notification> firstMatch = currentPage.getNotifications().stream()
                    .filter(n -> n.getEmailAddress().isPresent() && n.getEmailAddress().get()
                        .equalsIgnoreCase(searchEmail)).findFirst();
                if (firstMatch.isPresent()) {
                    return firstMatch;
                }
            }
            Optional<String> notificationIdForNextPage =
                extractNotificationIdForNextPage(currentPage.getNextPageLink().orElse(null));
            if (notificationIdForNextPage.isPresent()) {
                olderThanNotificationId = notificationIdForNextPage.get();
            } else {
                break;
            }
            page++;
        }

        return Optional.empty();
    }

    /**
     * Extract notify reference.
     * @should extract notify reference
     * @should return empty if no value
     */
    protected Optional<String> extractNotifyReference(String value) {
        if (StringUtils.isNotEmpty(value)) {
            Matcher referenceMatcher = Pattern.compile(NOTIFY_KEY_REFERENCE_REGEX).matcher(value);
            if (referenceMatcher.find()) {
                return Optional.ofNullable(StringUtils.trimToNull(referenceMatcher.group()));
            }
        }
        return Optional.empty();
    }

    /**
     * Extract older than id for next page.
     * @should extract older than value
     * @should return empty if no value
     */
    protected Optional<String> extractNotificationIdForNextPage(String url) {
        if (StringUtils.isNotEmpty(url)) {
            Matcher olderThanMatcher = OLDER_THAN_PATTERN.matcher(url);
            if (olderThanMatcher.find()) {
                return Optional.of(olderThanMatcher.group(1));
            }
        }
        return Optional.empty();
    }

}

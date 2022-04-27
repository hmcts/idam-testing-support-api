package uk.gov.hmcts.cft.idam.notify;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.StringUtils;
import uk.gov.service.notify.NotificationClient;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdamNotificationClient extends NotificationClient {

    public static final String ALL_STATUSES = null;
    public static final String EMAIL_TYPE = "email";

    private static final Pattern OLDER_THAN_PATTERN = Pattern.compile("older_than=([A-Za-z0-9\\-]+)&?");

    private final String keyName;

    public IdamNotificationClient(String apiKey) {
        super(apiKey);
        this.keyName = extractKeyName(apiKey).orElse(null);
    }

    public String getKeyName() {
        return keyName;
    }

    protected Optional<String> extractKeyName(String notifyKey) {
        if (StringUtils.isNotEmpty(notifyKey)) {
            String[] parts = notifyKey.split(this.getServiceId());
            if (parts.length == 2) {
                return Optional.ofNullable(StringUtils.trimToNull(CharMatcher.is('-').trimTrailingFrom(parts[0])));
            }
        }
        return Optional.empty();
    }

    public Optional<String> extractNotificationIdForNextPage(String url) {
        if (StringUtils.isNotEmpty(url)) {
            Matcher olderThanMatcher = OLDER_THAN_PATTERN.matcher(url);
            if (olderThanMatcher.find()) {
                return Optional.of(olderThanMatcher.group(1));
            }
        }
        return Optional.empty();
    }

}

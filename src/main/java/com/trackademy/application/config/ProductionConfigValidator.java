package com.trackademy.application.config;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductionConfigValidator {

    private static final String LOCAL_JWT_SECRET = "trackademy-local-dev-secret-change-me";

    private final String appEnvironment;
    private final String jwtSecret;
    private final boolean whatsappEnabled;
    private final String whatsappAccessToken;
    private final String whatsappPhoneNumberId;
    private final String whatsappWebhookVerifyToken;
    private final String whatsappAppSecret;
    private final String whatsappOfficialNumber;

    public ProductionConfigValidator(
            @ConfigProperty(name = "app.environment", defaultValue = "local") String appEnvironment,
            @ConfigProperty(name = "app.auth.jwt.secret", defaultValue = LOCAL_JWT_SECRET) String jwtSecret,
            @ConfigProperty(name = "app.whatsapp.enabled", defaultValue = "false") boolean whatsappEnabled,
            @ConfigProperty(name = "app.whatsapp.meta.access-token") Optional<String> whatsappAccessToken,
            @ConfigProperty(name = "app.whatsapp.meta.phone-number-id") Optional<String> whatsappPhoneNumberId,
            @ConfigProperty(name = "app.whatsapp.meta.webhook-verify-token") Optional<String> whatsappWebhookVerifyToken,
            @ConfigProperty(name = "app.whatsapp.meta.app-secret") Optional<String> whatsappAppSecret,
            @ConfigProperty(name = "app.whatsapp.official-number") Optional<String> whatsappOfficialNumber
    ) {
        this.appEnvironment = appEnvironment;
        this.jwtSecret = jwtSecret;
        this.whatsappEnabled = whatsappEnabled;
        this.whatsappAccessToken = whatsappAccessToken.orElse("");
        this.whatsappPhoneNumberId = whatsappPhoneNumberId.orElse("");
        this.whatsappWebhookVerifyToken = whatsappWebhookVerifyToken.orElse("");
        this.whatsappAppSecret = whatsappAppSecret.orElse("");
        this.whatsappOfficialNumber = whatsappOfficialNumber.orElse("");
    }

    void validate(@Observes StartupEvent event) {
        if (!isProduction()) {
            return;
        }

        List<String> missing = new ArrayList<>();
        requireStrongJwtSecret(missing);
        requireWhatsappConfigIfConfigured(missing);

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Invalid Trackademy production configuration. Fix these variables: "
                            + String.join(", ", missing)
            );
        }
    }

    private boolean isProduction() {
        return LaunchMode.current() == LaunchMode.NORMAL
                && ("production".equalsIgnoreCase(appEnvironment)
                        || "prod".equalsIgnoreCase(appEnvironment));
    }

    private void requireStrongJwtSecret(List<String> missing) {
        if (isBlank(jwtSecret) || LOCAL_JWT_SECRET.equals(jwtSecret) || jwtSecret.length() < 32) {
            missing.add("APP_AUTH_JWT_SECRET");
        }
    }

    private void requireWhatsappConfigIfConfigured(List<String> missing) {
        if (!isWhatsappConfigured()) {
            return;
        }

        requirePresent(missing, "APP_WHATSAPP_META_ACCESS_TOKEN", whatsappAccessToken);
        requirePresent(missing, "APP_WHATSAPP_META_PHONE_NUMBER_ID", whatsappPhoneNumberId);
        requirePresent(missing, "APP_WHATSAPP_META_WEBHOOK_VERIFY_TOKEN", whatsappWebhookVerifyToken);
        requirePresent(missing, "APP_WHATSAPP_META_APP_SECRET", whatsappAppSecret);
        requirePresent(missing, "APP_WHATSAPP_OFFICIAL_NUMBER", whatsappOfficialNumber);
    }

    private boolean isWhatsappConfigured() {
        return whatsappEnabled
                || !isBlank(whatsappAccessToken)
                || !isBlank(whatsappPhoneNumberId)
                || !isBlank(whatsappWebhookVerifyToken)
                || !isBlank(whatsappAppSecret)
                || !isBlank(whatsappOfficialNumber);
    }

    private void requirePresent(List<String> missing, String variableName, String value) {
        if (isBlank(value)) {
            missing.add(variableName);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

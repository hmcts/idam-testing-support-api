ARG APP_INSIGHTS_AGENT_VERSION=3.4.4

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4

ADD --chown=hmcts:hmcts build/libs/idam-testing-support-api.jar \
                        lib/applicationinsights-agent-3.4.4.jar /opt/app/

EXPOSE 5000/tcp
CMD [ "idam-testing-support-api.jar" ]

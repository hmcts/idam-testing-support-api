ARG APP_INSIGHTS_AGENT_VERSION=3.4.4

# Application image

FROM hmctspublic.azurecr.io/base/java:21-distroless

ADD --chown=hmcts:hmcts build/libs/idam-testing-support-api.jar \
                        lib/applicationinsights.json /opt/app/

EXPOSE 5000/tcp
CMD [ "idam-testing-support-api.jar" ]

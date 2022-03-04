ARG APP_INSIGHTS_AGENT_VERSION=2.6.1

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4

COPY lib/AI-Agent.xml /opt/app/
COPY lib/applicationinsights-agent-2.6.1.jar /opt/app/
COPY build/libs/idam-testing-support-api.jar /opt/app/

EXPOSE 5000/tcp
CMD [ "idam-testing-support-api.jar" ]

# idam-testing-support-api


The application exposes health endpoints (http://localhost:8080/health) and metrics endpoint
(http://localhost:8080/metrics).

## Notes

JUnit 5 is enabled by default in the project. Please refrain from using JUnit4 and use the next generation

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/spring-boot-template` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `4550` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4550/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Cleanup of users

Test users are cleaned up as part of the cleanup process for the session they were created in. This allows
the testing support api to cleanup all the resources created during the session in the correct order (i.e. users must be deleted
before roles). There are different cleanup strategies for users, that you can select based on the requirements for the environment.

* ALWAYS_DELETE (default)
* SKIP_RECENT_LOGINS

ALWAYS_DELETE means that after the session expiry time any test data that has been created for that session will be deleted.

SKIP_RECENT_LOGINS means that after the session expiry time any test data that has been created for that session will deleted,
except for any users that have had a login within a certain time period. For any user that has recently logged in then their testing
entity is set to DETACHED and their cleanup will no longer be managed by idam-testing-support-api (because they are no longer connected
to a session).

The configuration is controlled by the following attributes:

* `cleanup.session.lifespan` (Duration) - After this duration a session can be selected for removal.
* `cleanup.user.strategy` (ALWAYS_DELETE, SKIP_RECENT_LOGINS) - as above
* `cleanup.user.recent-login-duration` (Duration) - When the cleanup strategy is set to SKIP_RECENT_LOGINS then this attribute is the window within
which the user's last login time is considered "recent".

Note that the `cleanup.user.recent-login-duration` cannot exceed the `cleanup.session.lifespan` and if it is set to a larger value then it will be overridden
to be half the duration of `cleanup.session.lifespan`. If you were allowed to set the recent login duration to be larger than the session lifespan then the behaviour
would be the same as ALWAYS_DELETE.

Also note that the IDAM lastLoginDate is only accurate to within an hour, so it would make sense to not attempt to create a session lifespan of less than 2 hours in an environment
where SKIP_RECENT_LOGINS is the cleanup strategy.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details


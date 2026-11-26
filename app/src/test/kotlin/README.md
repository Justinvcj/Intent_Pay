# Unit tests

Pure-JVM JUnit 4 tests (no Robolectric, Mockito or MockK).

Run locally: `./gradlew :app:testDebugUnitTest`

Tests marked `@Ignore` depend on PR C3 (parser widening); re-enable once it merges.
Reports: `app/build/reports/tests/testDebugUnitTest/index.html`

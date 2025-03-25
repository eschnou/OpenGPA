# OpenGPA Developer Guide

## Build & Test Commands
- Full build: `mvn clean package -Pproduction`
- Run all tests: `mvn test`
- Integration tests: `mvn -Pit verify`
- Single test class: `mvn test -Dtest=TestClassName`
- Single test method: `mvn test -Dtest=TestClassName#testMethodName`
- Run server: `java -jar opengpa-server/target/opengpa-server-0.4-SNAPSHOT.jar`

## Code Style Guidelines
- Java 21 required
- Use Lombok annotations (`@Data`, `@Builder`, etc.) to reduce boilerplate
- Follow standard Spring Boot and Java conventions
- Use builder pattern for complex objects
- Static factory methods for common operations
- CamelCase for class/method names; descriptive naming with proper suffixes:
  - Action classes: `[Name]Action`
  - Tests: `[Name]Test` or `[Name]IT` for integration tests
- Error handling:
  - Within Actions: Wrap errors in `ActionResult` objects with `Status.FAILURE`
  - Server/services: Use exceptions, add custom exceptions to GlobalExceptionHandler
- Tests use JUnit 5 and Mockito, with comprehensive test cases
- Include Javadoc comments for classes and public methods

This is a multi-module Maven project. When modifying, maintain consistency with existing patterns in the codebase.
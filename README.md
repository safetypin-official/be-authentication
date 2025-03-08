# Authentication Microservice
[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=safetypin-official_be-authentication)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-dark.svg)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=bugs)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=coverage)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=safetypin-official_be-authentication&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=safetypin-official_be-authentication)

## Overview

The **Authentication Microservice** is a Spring Boot-based REST API that handles user authentication and authorization. It supports both traditional email-based registration/login as well as social authentication (e.g., Google, Apple). The service includes features such as OTP (One-Time Password) verification, password reset simulation, and a simple content posting endpoint for verified users.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Running Tests](#running-tests)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)

## Features

- **Email Registration & Login:** Supports registration and login using email and password.
- **Social Authentication:** Simulated endpoints for registration and login using social providers.
- **OTP Verification:** Generates and verifies OTPs for user account validation.
- **Password Reset:** Simulated password reset functionality for email-based users.
- **Content Posting:** A secured endpoint that only allows verified users to post content.
- **Dev Data Seeder:** Automatically seeds development data when running under the `dev` profile.
- **Robust Testing:** Comprehensive unit and integration tests using JUnit 5, Spring Boot Test, and TestContainers.

## Tech Stack

- **Java:** 21
- **Spring Boot:** 3.4.2
- **Maven:** Build automation and dependency management
- **Spring Security:** For authentication and password encoding
- **Spring Data JPA:** For ORM and database access
- **PostgresSQL & H2:** Database support (PostgresSQL for production; H2 for development/testing)
- **JUnit 5 & TestContainers:** For testing and integration testing

## Prerequisites

- **Java 21**
- **Maven 3.6+**
- A running PostgresSQL instance (for production) or H2 (for development/testing)

## Installation

1. **Clone the repository:**
```
   git clone https://github.com/yourusername/authentication-microservice.git  
   cd authentication-microservice
```
2. **Build the project using Maven:**
```
   mvn clean install
```
3. **Run the application:**
```
   mvn spring-boot:run
```
   The service will start on the default port (typically 8080).

## Configuration

- **application.properties:** Configure your database, server port, and other environment-specific settings.
- **Profiles:** Use the `dev` profile for development. The `DevDataSeeder` will automatically seed sample user data when running under this profile:

```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## API Endpoints

### Public Endpoints

- **GET `/`**  
  Returns a simple "Hello, World!" greeting.

### Authentication Endpoints

- **POST `/api/auth/register-email`**  
  Registers a new user using email.
    - **Request Body:** JSON containing `email`, `password`, `name`, and `birthdate`.
    - **Response:** Success message with user data.

- **POST `/api/auth/register-social`**  
  Registers or logs in a user using social authentication.
    - **Request Body:** JSON containing `provider`, `socialToken`, `email`, `name`, `birthdate`, and `socialId`.
    - **Response:** Success message with user data.

- **POST `/api/auth/login-email`**  
  Authenticates a user using email and password.
    - **Parameters:** `email`, `password`.
    - **Response:** User data on successful login.

- **POST `/api/auth/login-social`**  
  Logs in a user via social authentication.
    - **Parameter:** `email`.
    - **Response:** User data on successful social login.

- **POST `/api/auth/verify-otp`**  
  Verifies the OTP for account validation.
    - **Parameters:** `email`, `otp`.
    - **Response:** Message indicating success or failure of OTP verification.

- **POST `/api/auth/forgot-password`**  
  Simulates password reset for email-registered users.
    - **Request Body:** JSON containing `email`.
    - **Response:** Message indicating that reset instructions have been sent.

- **POST `/api/auth/post`**  
  Allows posting of content for verified users.
    - **Parameters:** `email`, `content`.
    - **Response:** Success or failure message based on user verification.

- **GET `/api/auth/dashboard`**  
  Returns dashboard data (currently a placeholder).
    - **Response:** An empty JSON object.

## Running Tests

To run all unit and integration tests, execute:
```
mvn test
```

Tests are written using JUnit 5 and cover controllers, services, repository interactions, and utility components such as OTP generation and validation.

## Development

- **Code Style:** The project adheres to standard Java coding conventions and uses Lombok to reduce boilerplate.
- **Continuous Integration:** Integration with CI tools is recommended. Test coverage is ensured using Maven Surefire and Failsafe plugins.
- **Debugging:** Utilize Spring Boot DevTools for hot-reloading during development.

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a new feature branch (git checkout -b feature/YourFeature).
3. Commit your changes (git commit -m 'Add some feature').
4. Push to the branch (git push origin feature/YourFeature).
5. Open a Pull Request.

Please ensure that your code adheres to the existing coding style and that all tests pass before submitting your PR.

## License

This project is licensed under the
Creative Commons Attribution-NoDerivatives 4.0 International (CC BY-ND 4.0)
License. See the LICENSE file for details.

## Acknowledgements

- Thanks to the Spring Boot team and the open-source community for their continuous contributions.
- Special thanks to contributors who have helped improve the project.

## Author
SafetyPin Team
- Darrel Danadyaksa Poli - 2206081995
- Fredo Melvern Tanzil - 2206024713
- Sefriano Edsel Jieftara Djie - 2206818966=
- Alma Putri Nashrida - 2206814671
- Andi Salsabila Ardian - 2206083571
- Muhammad Raihan Akbar - 2206827674


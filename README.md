# FundoNotes

# 📝 Fundoo Notes — Spring Boot Backend Application

**A Secure, Scalable, and Feature-Rich Notes Management REST API built using Java, Spring Boot, Spring Security, MySQL, Redis, RabbitMQ, Spring Batch, Spring AOP, and Spring Cloud Gateway.**

## 📖 1. About the Project

**Fundoo Notes** is a comprehensive Notes Management Backend Application inspired by **Google Keep**, designed to help users securely create, organize, manage, and retrieve personal notes.

The application provides RESTful APIs for user authentication, profile management, note creation, editing, searching, labeling, archiving, pinning, trash management, and scheduling reminders.

Beyond basic CRUD operations, the project integrates several technologies commonly used in enterprise Java applications, including Redis for caching, RabbitMQ for asynchronous message processing, Spring Batch for bulk data operations, and Spring AOP for performance monitoring and logging.

The project also incorporates a separate Spring Cloud API Gateway that acts as a centralized entry point for accessing the backend APIs.

Fundoo Notes was developed incrementally through **18 use cases**, starting with user registration and authentication and progressing toward more advanced backend concepts such as caching, message queues, batch processing, scheduled tasks, API routing, security verification, and automated integration testing.

The project focuses on designing a **secure, modular, maintainable, and extensible backend system** that demonstrates practical experience with the Java and Spring ecosystem.

---

## 🎯 2. Project Objectives

The main objectives of Fundoo Notes are:

- Develop a fully functional Notes Management REST API using Java and Spring Boot.
- Implement secure authentication and authorization using Spring Security and JWT.
- Design relational database entities and relationships using Spring Data JPA and Hibernate.
- Enforce user-level data ownership so users can access only their own notes.
- Implement complete CRUD operations and note lifecycle management.
- Support note organization through labels, search, pinning, and archiving.
- Provide automated email reminders and notification processing.
- Improve application performance through Redis caching.
- Implement asynchronous communication using RabbitMQ.
- Monitor service execution using Spring AOP.
- Handle bulk data processing and scheduled cleanup through Spring Batch.
- Introduce API Gateway architecture using Spring Cloud Gateway.
- Validate functionality through unit tests, Postman, and automated integration smoke tests.
- Apply Git branching and version-control practices throughout development.

---

## 🛠️ 3. Technology Stack

| Category | Technologies |
|---|---|
| Programming Language | Java 17 |
| Backend Framework | Spring Boot 3.5.x |
| API Development | Spring Web, REST APIs, Spring MVC |
| Authentication & Authorization | Spring Security, JSON Web Token (JWT) |
| Database | MySQL 8 |
| ORM & Persistence | Spring Data JPA, Hibernate |
| Input Validation | Jakarta Bean Validation |
| Caching | Redis, Spring Cache |
| Messaging | RabbitMQ, Spring AMQP |
| Batch Processing | Spring Batch |
| Scheduling | Spring Scheduling |
| Logging & Monitoring | Spring AOP, SLF4J |
| API Gateway | Spring Cloud Gateway, WebFlux |
| Email | Spring Mail, Mailpit |
| Build Tool | Apache Maven |
| Containerization | Docker |
| API Testing | Postman |
| Unit Testing | JUnit 5, Mockito |
| Integration Smoke Testing | Python 3 |
| Version Control | Git, GitHub |
| Development IDE | IntelliJ IDEA |
| Configuration | application.properties, environment variables |

---

## 🏗️ 4. Project Architecture

The project uses a layered backend architecture with a separate API Gateway application.

### Architecture Flow

```text
             CLIENT / POSTMAN / FUTURE FRONTEND
                           |
                           v
                SPRING CLOUD API GATEWAY
                       Port: 8081
                           |
                           v
                 FUNDOO NOTES BACKEND
                       Port: 8080
                           |
                 SPRING SECURITY + JWT
                           |
                      CONTROLLERS
                           |
                       SERVICES
                           |
                     REPOSITORIES
                           |
                         MYSQL
```

### Additional Components

```text
                    FUNDOO NOTES BACKEND
                             |
          +------------------+------------------+
          |                  |                  |
          v                  v                  v
        REDIS             RABBITMQ         SPRING BATCH
        Cache             Messaging        Import / Export
                                               |
                                               v
                                           MYSQL / CSV

          +------------------+------------------+
          |                                     |
          v                                     v
      SPRING AOP                           SPRING MAIL
  Logging & Monitoring                 Reminder Emails
                                                |
                                                v
                                             MAILPIT
```

### Architecture Components

**API Gateway Layer**

Spring Cloud Gateway runs as a separate Spring Boot application on port `8081`. It forwards incoming API requests to the Fundoo Notes backend running on port `8080`.

**Security Layer**

Spring Security and JWT provide authentication and protect user-specific endpoints.

**Controller Layer**

REST controllers accept HTTP requests, validate request data, and return responses.

**Service Layer**

Service classes contain application business logic, enforce user ownership, and coordinate database operations.

**Repository Layer**

Spring Data JPA repositories interact with MySQL through Hibernate.

**Caching Layer**

Redis caches frequently requested label data to reduce repeated database queries.

**Messaging Layer**

RabbitMQ separates reminder-event publishing from email notification processing.

**Batch Processing Layer**

Spring Batch supports CSV import, CSV export, and scheduled cleanup of expired trashed notes.

**Monitoring Layer**

Spring AOP records service-method execution status and duration without adding repetitive logging logic throughout the business layer.

---

## ✨ 5. Key Features

### 🔐 User Management & Security

- User registration and login
- Password encryption
- JWT token generation and verification
- Protected REST APIs
- User profile retrieval and updates
- Email verification
- Forgot-password and reset-password workflows
- User-specific authorization and data ownership

### 📝 Notes Management

- Create notes
- View personal notes
- Update existing notes
- Soft-delete notes
- Permanently delete notes
- Pin and unpin notes
- Archive and unarchive notes
- Move notes to Trash
- Restore trashed notes
- Search notes by keywords

### 🏷️ Label Management

- Create custom labels
- View labels
- Rename labels
- Delete labels
- Assign labels to notes
- Remove labels from notes
- Filter notes using labels

### ⏰ Reminders & Notifications

- Set future reminders
- View upcoming reminders
- Remove reminders
- Identify due reminders using scheduled jobs
- Publish reminder events using RabbitMQ
- Process reminder events asynchronously
- Send reminder notifications through email
- Test delivered emails using Mailpit

### 🚀 Performance & Background Processing

- Redis caching for label retrieval
- Cache invalidation after label changes
- Spring AOP execution-time monitoring
- Spring Batch bulk CSV import
- Spring Batch CSV export
- Automatic cleanup of expired trashed notes
- Scheduled background processing

### 🌐 API Gateway & Testing

- Centralized API routing through Spring Cloud Gateway
- JWT-protected requests forwarded to the backend
- Gateway request logging
- Gateway health checks
- Postman API testing
- Unit testing using JUnit and Mockito
- Automated Python smoke tests

---

## 📚 6. Implementation of All 18 Use Cases

The project was developed across 18 incremental use cases.

| UC | Use Case | Implementation |
|---|---|---|
| **UC1** | User Registration | Register users with validated details and encrypted passwords |
| **UC2** | User Login & JWT Authentication | Authenticate users and generate JWT access tokens |
| **UC3** | User Profile Management | Retrieve and update authenticated user details |
| **UC4** | Email Verification & Password Recovery | Verify user email addresses and support forgot/reset password flows |
| **UC5** | Notes CRUD Operations | Create, read, update, and delete personal notes |
| **UC6** | Note Lifecycle Management | Pin, archive, trash, restore, and permanently delete notes |
| **UC7** | Notes Search | Search active notes using keywords |
| **UC8** | Label Management | Create labels, associate labels with notes, and filter by labels |
| **UC9** | Notes Reminders | Set, view, and remove note reminders |
| **UC10** | Email Reminder Notifications | Send automated reminder emails using Spring Mail |
| **UC11** | Redis Caching | Cache user labels and invalidate cached data when labels change |
| **UC12** | RabbitMQ Messaging | Publish and consume reminder events asynchronously |
| **UC13** | Spring AOP | Implement service logging, activity logging, and execution-time measurement |
| **UC14** | Spring Batch CSV Import | Import multiple notes from uploaded CSV files |
| **UC15** | Scheduled Trash Cleanup | Automatically delete notes exceeding the configured Trash retention period |
| **UC16** | Spring Batch CSV Export | Export authenticated users' active notes to CSV |
| **UC17** | Spring Cloud API Gateway | Provide centralized API routing through a separate Gateway application |
| **UC18** | Integration Testing & Project Finalization | Validate authentication, APIs, Gateway routing, export, and security using automated smoke tests |

---

## 🔒 7. Authentication and Authorization

The project uses **Spring Security and JWT** to secure application endpoints.

### Authentication Flow

1. The user registers through the registration API.
2. The password is securely hashed before being stored in MySQL.
3. The user logs in using their registered credentials.
4. The backend validates the credentials and generates a JWT.
5. The client sends the JWT in the `Authorization` header.
6. Spring Security validates the token.
7. Authenticated requests are forwarded to the appropriate controller.
8. Service-level ownership checks ensure that users cannot retrieve or modify other users' notes.

### Authorization Header

```http
Authorization: Bearer <JWT_TOKEN>
```

JWT signing keys are supplied through environment variables rather than being hardcoded in the source code.

---

## 🗄️ 8. Database Design

Fundoo Notes uses **MySQL** as its relational database and **Spring Data JPA with Hibernate** for persistence.

### Main Entities

**User**

Stores registered user information, authentication-related data, and profile information.

**Note**

Stores the note title, description, timestamps, owner, note status, reminder details, and label associations.

**Label**

Stores user-created labels used to organize notes.

**Account Token**

Supports account-related token workflows such as email verification and password recovery.

### Relationships

- One user can create multiple notes.
- Every note belongs to one user.
- One user can create multiple labels.
- Notes can be associated with multiple labels.
- Each label can be associated with multiple notes.
- A join table maintains note-label relationships.

### Spring Batch Metadata

Spring Batch maintains additional tables for job instances, executions, steps, and processing statistics.

These tables make it possible to inspect the execution status of import, export, and cleanup jobs.

---

## ⚡ 9. Redis Caching

Redis is integrated using **Spring Data Redis and Spring Cache**.

### Purpose

To reduce repeated database queries and improve the performance of frequently accessed data.

### Implementation

- Cache user-specific label lists.
- Use user IDs as cache keys.
- Return cached label data for repeated requests.
- Evict the relevant cache entry when labels are created, renamed, or deleted.
- Configure a time-to-live for cached entries.

### Concepts Demonstrated

- Cache-aside behavior through Spring Cache annotations
- `@Cacheable`
- `@CacheEvict`
- Cache expiration
- User-specific cache isolation
- Cache consistency after write operations

---

## 📨 10. RabbitMQ Asynchronous Messaging

RabbitMQ is used to decouple reminder scheduling from email delivery.

### Reminder Processing Flow

```text
Reminder becomes due
         |
         v
Spring Scheduler
         |
         v
RabbitMQ Publisher
         |
         v
RabbitMQ Exchange
         |
         v
Reminder Queue
         |
         v
RabbitMQ Consumer
         |
         v
Reminder Notification Service
         |
         v
Spring Mail
         |
         v
Mailpit / SMTP
```

### Implementation

- The scheduler identifies due reminders.
- `RabbitTemplate` publishes reminder messages.
- RabbitMQ routes messages to the configured queue.
- A consumer receives the messages using `@RabbitListener`.
- The consumer verifies the note's reminder status.
- Spring Mail sends the notification.
- The application records the reminder delivery timestamp.

### Concepts Demonstrated

- Producer-consumer architecture
- Message queues
- Exchanges and routing keys
- Asynchronous background processing
- Failure handling
- Duplicate-message protection through database state checks

This is a learning implementation; production-grade delivery would require stronger reliability guarantees such as publisher confirms, dead-letter queues, and robust retry policies.

---

## 📊 11. Spring AOP — Logging and Monitoring

Spring AOP is used to separate monitoring behavior from business logic.

### Implementation

- Track service-method execution.
- Measure method execution duration.
- Log successful and failed service operations.
- Log selected note-modification activities.
- Preserve original service results and exceptions.
- Avoid logging passwords, JWTs, and private note contents.

### Spring AOP Concepts

- `@Aspect`
- `@Pointcut`
- `@Around`
- `@AfterReturning`
- Join points
- Cross-cutting concerns
- Execution-time monitoring

This demonstrates how monitoring logic can be implemented centrally without modifying every service method.

---

## 📦 12. Spring Batch Processing

Spring Batch is used for structured bulk processing.

### UC14 — CSV Notes Import

The application supports uploading a CSV file containing multiple notes.

**Processing Flow:**

```text
CSV Upload
    |
    v
ItemReader
    |
    v
ItemProcessor
    |
    v
ItemWriter
    |
    v
MySQL Database
```

The batch job reads CSV records, validates them, converts them into Note entities, and saves them in chunks.

Imported notes are assigned to the authenticated user.

### UC15 — Scheduled Trash Cleanup

A scheduled Spring Batch job automatically deletes trashed notes after the configured retention period.

**Default retention:** 30 days.

The cleanup job processes eligible notes and records execution status using Spring Batch metadata.

### UC16 — CSV Notes Export

The application allows users to download their active notes as a CSV file.

The export includes note IDs, titles, descriptions, pinned status, and timestamps.

Archived notes, trashed notes, and other users' notes are excluded.

### Concepts Demonstrated

- Jobs and steps
- ItemReader, ItemProcessor, ItemWriter
- Tasklets
- Chunk-based processing
- Job parameters
- JobRepository
- Scheduled batch jobs
- Execution metadata
- CSV processing

---

## 🌐 13. Spring Cloud API Gateway

The project includes a separate **Spring Cloud API Gateway** application.

### Purpose

To provide a centralized entry point for accessing the backend REST APIs.

### Applications

| Application | Port |
|---|---|
| Fundoo Notes Backend | `8080` |
| Fundoo API Gateway | `8081` |

### Request Flow

```text
Postman / Client
      |
      v
http://localhost:8081
      |
      v
Spring Cloud Gateway
      |
      v
http://localhost:8080
      |
      v
Spring Security + REST Controllers
      |
      v
Service and Repository Layers
```

### Features Demonstrated

- Request routing
- Path-based route matching
- Reverse-proxy behavior
- Gateway request logging
- Request correlation IDs
- Health monitoring
- Forwarding authentication headers

The backend remains responsible for JWT verification and authorization. The Gateway and backend are built and executed as separate Maven-based Spring Boot applications.

---

## 🔗 14. REST API Endpoints

The following are representative APIs implemented across the project.

### User APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Authenticate and obtain JWT |
| GET | `/api/users/me` | Get authenticated user profile |
| PUT | `/api/users/me` | Update user profile |
| GET | `/api/users/auth-check` | Check authentication |

Additional endpoints support email verification and password recovery.

### Notes APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/notes` | Create a note |
| GET | `/api/notes` | Get active notes |
| GET | `/api/notes/{id}` | Get a specific note |
| PUT | `/api/notes/{id}` | Update a note |
| DELETE | `/api/notes/{id}` | Soft-delete a note |
| GET | `/api/notes/search?keyword=spring` | Search active notes |
| GET | `/api/notes/reminders` | View upcoming reminders |
| PUT | `/api/notes/{id}/reminder` | Set reminder |
| DELETE | `/api/notes/{id}/reminder` | Remove reminder |

Additional note endpoints support pinning, archiving, trash management, restoration, and permanent deletion.

### Label APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/labels` | Create a label |
| GET | `/api/labels` | Get user labels |
| PUT | `/api/labels/{id}` | Rename a label |
| DELETE | `/api/labels/{id}` | Delete a label |
| GET | `/api/labels/{id}/notes` | Get notes associated with a label |
| PUT | `/api/notes/{noteId}/labels/{labelId}` | Assign label to note |
| DELETE | `/api/notes/{noteId}/labels/{labelId}` | Remove label from note |

### Spring Batch APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/notes/import` | Import notes from CSV |
| GET | `/api/notes/export` | Export active notes to CSV |

All protected APIs can be accessed through the Gateway at:

```text
http://localhost:8081
```

---

## 🧪 15. Testing and Quality Assurance

Testing is performed using **JUnit 5, Mockito, Postman, and automated Python smoke tests**.

### Unit Testing

Unit tests validate service behavior, reminder processing, Batch processors, Spring AOP, and scheduled cleanup components.

### Postman API Testing

Postman is used to test:

- Registration and login
- JWT authentication
- Notes CRUD
- Labels
- Search
- Reminders
- CSV imports and exports
- API Gateway routing
- Unauthorized requests

### Automated UC18 Smoke Testing

A Python integration smoke-test script validates real HTTP interactions with the backend and Gateway.

**UC18 smoke-test result:**

| Metric | Result |
|---|---|
| Total Tests | 11 |
| Passed | 11 |
| Failed | 0 |

### Verified Smoke-Test Scenarios

1. Login using the backend.
2. Read notes directly through the backend.
3. Read notes through the API Gateway.
4. Compare backend and Gateway responses.
5. Reject requests without JWT.
6. Reject invalid JWTs.
7. Retrieve user labels.
8. Retrieve upcoming reminders.
9. Search notes.
10. Export notes to CSV.
11. Check API Gateway health.

**Result: 11/11 smoke tests passed.**

This confirms the tested API flows work together. It does not replace broader security, performance, and production-readiness testing.

---

## 🐳 16. Docker and Supporting Services

Docker is used for local development infrastructure.

| Service | Port | Responsibility |
|---|---|---|
| MySQL | 3306 | Persistent storage |
| Redis | 6379 | Caching |
| RabbitMQ | 5672 | Message processing |
| RabbitMQ Management | 15672 | Queue monitoring |
| Mailpit SMTP | 1025 | Local email handling |
| Mailpit Web UI | 8025 | Email inspection |
| Fundoo Backend | 8080 | REST APIs |
| API Gateway | 8081 | Centralized routing |

Redis, RabbitMQ, and Mailpit can be started as Docker containers, while the backend and Gateway run as separate Spring Boot applications.

---

## 📁 17. Project Structure

```text
fundoo-notes/
│
├── src/
│   ├── main/
│   │   ├── java/com/fundoonotes/
│   │   │   ├── aop/
│   │   │   ├── batch/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── messaging/
│   │   │   ├── repository/
│   │   │   ├── scheduler/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── FundooNotesApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/fundoonotes/
│
├── fundoo-api-gateway/
│   ├── src/
│   │   ├── main/java/com/fundoonotes/gateway/
│   │   │   ├── config/
│   │   │   ├── filter/
│   │   │   └── FundooApiGatewayApplication.java
│   │   └── main/resources/
│   │       └── application.properties
│   └── pom.xml
│
├── scripts/
│   └── smoke_test.py
│
├── pom.xml
├── .gitignore
└── README.md
```

---

## ⚙️ 18. How to Run the Project Locally

### Prerequisites

- Java 17
- Maven
- MySQL
- Docker
- Git
- Python 3 for smoke tests
- Postman for API testing

### Step 1 — Set Up MySQL

Create the application database:

```sql
CREATE DATABASE IF NOT EXISTS fundoo_notes;
```

Configure your MySQL connection through the backend's properties and environment variables.

### Step 2 — Start Supporting Services

Start Redis, RabbitMQ, and Mailpit using your local Docker setup.

### Step 3 — Configure Backend Environment Variables

The backend requires:

```text
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

The JWT signing secret must be a valid Base64-encoded value compatible with the application's JWT configuration.

Do not commit passwords or signing secrets to GitHub.

### Step 4 — Run the Backend

```bash
cd fundoo-notes
mvn spring-boot:run
```

Backend base URL:

```text
http://localhost:8080
```

### Step 5 — Run the API Gateway

Open another terminal:

```bash
cd fundoo-notes/fundoo-api-gateway
mvn spring-boot:run
```

Gateway base URL:

```text
http://localhost:8081
```

### Step 6 — Run Automated Smoke Tests

From the repository root:

```bash
python3 scripts/smoke_test.py
```

The script prompts for a registered user's credentials and tests the application's major API flows.

### Step 7 — Run Maven Unit Tests

Backend:

```bash
mvn clean test
```

Gateway:

```bash
cd fundoo-api-gateway
mvn clean test
```

---

## 🧠 19. Technical Concepts Demonstrated

This project demonstrates hands-on implementation of several important software engineering concepts.

### Core Java & Spring

- Object-oriented programming
- Dependency injection and inversion of control
- RESTful API design
- Controller-Service-Repository architecture
- DTO-based request and response handling
- Input validation and exception handling
- Spring bean lifecycle and configuration

### Security

- Password hashing
- JWT authentication
- Stateless API security
- Authorization and user ownership
- Environment-based secret management

### Database and Persistence

- Relational database modeling
- Entity relationships
- Spring Data repositories
- JPA and Hibernate
- Database transactions
- Query methods and custom queries

### Performance and Scalability

- Redis caching
- Cache invalidation
- Asynchronous messaging
- Background jobs
- Chunk processing
- API Gateway routing
- Separation of application responsibilities

### Reliability and Monitoring

- Exception propagation
- Scheduled processing
- Message consumption
- Service-method execution monitoring
- Activity logging
- Spring Batch job metadata
- Integration smoke tests

### Development Practices

- Git feature branching
- Maven dependency management
- Docker-based development infrastructure
- Incremental feature development
- Unit testing and API testing
- Debugging Spring Boot startup issues
- Troubleshooting dependencies and environment variables

---

## 🚀 20. Future Enhancements

Potential improvements include:

- React-based frontend for note management
- Responsive user interface inspired by Google Keep
- Note sharing and collaboration
- File and image attachments
- Rich-text note editing
- Pagination and advanced filtering
- Full-text indexing using Elasticsearch
- Centralized observability using Micrometer, Prometheus, and Grafana
- Distributed tracing
- Production-grade RabbitMQ retry and dead-letter handling
- Automated CI/CD pipelines
- Containerized deployment of backend and Gateway
- Improved database migration management using Flyway or Liquibase
- Cloud deployment

---

## 🏁 21. Project Outcome

**Fundoo Notes demonstrates the development of a complete Notes Management backend using Java and the Spring ecosystem.**

Across 18 use cases, the project progresses from foundational REST API development to security, relational database persistence, asynchronous messaging, caching, batch processing, application monitoring, API routing, and integration testing.

The application provides a foundation for a full-stack notes management system, while demonstrating the practical application of backend development concepts commonly used in real-world enterprise Java projects.

**Final automated integration smoke-test result: 11/11 passed.**

---

**Project Type:** Backend Development / REST API / Spring Boot Learning Project

**Architecture:** Layered Monolithic Backend with a Separate API Gateway

**Primary Language:** Java 17

**Backend Framework:** Spring Boot

**Database:** MySQL

**Total Use Cases:** 18

**Development Approach:** Incremental Implementation with Git Feature Branches

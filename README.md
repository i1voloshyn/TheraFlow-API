# TheraFlow API

### A backend application for managing a private therapy practice

**TheraFlow** is a REST API built with **Java and Spring Boot**, designed to help independent therapists organize their work and support their patients.

The goal is to bring patient management, therapy tasks, and communication with guardians into one application.

> **Project status:** The application is under active development. Features are being added and improved.

## Current Features

- Account registration and email verification.
- Login with JWT authentication.
- Password changes.
- Creating, viewing, and updating therapist profiles.
- Managing therapist information and addresses.

## Technology Stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Backend | Spring Boot, Spring Web |
| Authentication | Spring Security, JWT |
| Persistence | JPA / Hibernate, PostgreSQL |
| Database migrations | Flyway |
| Email | Spring Mail |
| Testing | JUnit, Mockito, Testcontainers, GreenMail |
| Build and development | Maven, Docker Compose |

## Planned Features

- **Patient management** — organize patient profiles and therapy records.
- **Tasks for patients** — assign exercises and track their completion between sessions.
- **Communication with guardians** — share updates and discuss patient progress.
- **Video lessons** — a possible future addition to support patients remotely.

## About This Repository

This repository contains the backend API for TheraFlow. It is a personal project developed to practice Java and Spring while building an application for everyday therapy practice.

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Message deduplication system for chequera and recibo processing
- New `ChequeraMessageCheckClient` and `ReciboMessageCheckClient` for message tracking
- UUID-based message tracking in DTOs
- Manual acknowledgment mode for RabbitMQ consumers
- Enhanced logging for message processing

### Changed
- Updated Spring Boot to version 3.4.4
- Updated Kotlin to version 2.1.20
- Updated Spring Cloud to version 2024.0.1
- Updated Guava to version 33.4.6-jre
- Updated Springdoc OpenAPI to version 2.8.6
- Improved error handling in message consumers
- Enhanced email address handling with testing mode

### Removed
- Removed tester queue and related functionality
- Removed redundant logging statements

### Fixed
- Fixed potential null pointer issues in message processing
- Fixed email address handling in testing mode

### Security
- Added UUID-based message tracking for better security
- Improved error handling to prevent message loss

## [Previous Versions]

[Previous version history will be added here as new versions are released] 
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
```bash
# Build the project
./gradlew build

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Assemble APK
./gradlew assembleDebug

# Clean build
./gradlew clean
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Run specific module tests
./gradlew :app:test

# Run single test class
./gradlew :app:testDebugUnitTest --tests "com.emathias.periodic.service.ScheduledItemProcessorTest"
```

## Project Setup Requirements

### AWS Credentials Configuration
Before building, you must configure AWS credentials for Bedrock AI services:

1. Copy `local.properties.template` to `local.properties`
2. Fill in AWS credentials:
   ```properties
   aws.region=us-east-1
   aws.access_key_id=YOUR_AWS_ACCESS_KEY_ID
   aws.secret_access_key=YOUR_AWS_SECRET_ACCESS_KEY
   ```
3. Ensure IAM user has `bedrock:InvokeModel` permission for `amazon.nova-lite-v1:0`

See `AWS_SECURITY_SETUP.md` for detailed security configuration.

## Architecture Overview

**Periodic** is an AI-enhanced task scheduling Android app with the following key components:

### Core Architecture Patterns
- **Dependency Injection**: Hilt for all components
- **Database**: Room with entities, DAOs, and type converters
- **UI**: Jetpack Compose with Material3, MVVM pattern
- **Background Processing**: WorkManager with cron-based scheduling
- **API Layer**: OkHttp-based service communicating with localhost:8080 backend
- **AI Integration**: Amazon Bedrock for natural language processing

### Key Data Flow
1. **Scheduled Items**: Created via AI or manual input, stored with cron expressions
2. **Background Processing**: `ScheduledItemWorker` runs every 15 minutes, processes due items
3. **Todo Generation**: Scheduled items automatically create todo items when executed
4. **History Tracking**: Execution history prevents duplicate processing

### Main Entities
- `ScheduledItem`: Core scheduling entity with cron expressions
- `TodoItem`: Simple todos generated from scheduled items  
- `ScheduledItemHistory`: Tracks individual item executions
- `ScheduledItemProcessHistory`: Tracks overall processing runs

### Configuration Modules
- `ApiModule`: HTTP client and API service configuration
- `RoomModule`: Database setup with fallback to destructive migration
- `AwsConfig`: Bedrock client configuration with proper timeouts

### Service Layer
- `ScheduledItemProcessor`: Core business logic for processing scheduled items
- `BedrockAiService`: Direct AWS Bedrock integration
- `AiEnhancedTodoService`: High-level AI functionality wrapper
- `ScheduledItemApiService`: Repository pattern implementation for API calls

### UI Organization
- Navigation drawer with two main screens (Scheduled Items, Todo List)
- ViewModels handle state management and API communication
- Unidirectional data flow with sealed event classes
- AI-powered creation dialogs with natural language input

## Development Notes

### Cron Expression Handling
- Uses `cron-utils` library with Unix 5-field format
- Type converters handle Room database storage
- `ExecutionTime` calculates next occurrence from `startsAt` timestamp

### Background Processing
- WorkManager integration with Hilt dependency injection
- 15-minute periodic processing plus one-time startup execution
- Prevents duplicate execution through history tracking

### Testing Structure
- Fake implementations in `test/` directory for DAOs and API service
- `ScheduledItemProcessorTest` demonstrates comprehensive testing approach
- Test database uses in-memory Room database

### Security Considerations
- AWS credentials stored in encrypted SharedPreferences
- Never commit `local.properties` (gitignored)
- Minimal IAM permissions for Bedrock access only
- Proper timeout configurations for AWS API calls
# WealthPilot AI - Digital Wealth Advisory

This project is a single-instance MVP for WealthPilot AI, providing digital wealth advisory services.

## Prerequisites
- Java 17
- Maven 3.8+ (or use the included wrapper)
- Anthropic API Key for the Portfolio Advisor AI

## Configuration

1. Locate the `.env` file at the root of the project. (If it doesn't exist, create it based on `application-local.yml` or standard format).
2. Set your Anthropic API Key:
   ```env
   ANTHROPIC_API_KEY=your-actual-anthropic-api-key
   ```
   **DO NOT** commit this file to source control. It is already included in `.gitignore`.

## Running Locally

To run the application locally, use the Maven wrapper:

```bash
./mvnw clean spring-boot:run
```

Once the application starts, navigate to `http://localhost:8080` in your web browser to access the interactive dashboard.

## Deployment to AWS Elastic Beanstalk

This application is ready to be deployed to AWS Elastic Beanstalk (Java 17 Corretto platform).

### Deployment Steps:

1. Build the executable JAR:
   ```bash
   ./mvnw clean package
   ```
2. Initialize the Elastic Beanstalk environment (only needed the first time):
   ```bash
   eb init -p corretto-17 wealthpilot-ai
   ```
3. Create the environment (only needed the first time):
   ```bash
   eb create wealthpilot-env
   ```
4. Set the Anthropic API key securely in the Elastic Beanstalk environment:
   ```bash
   eb setenv ANTHROPIC_API_KEY=your-actual-anthropic-api-key
   ```
5. Deploy the application:
   ```bash
   eb deploy
   ```
6. Verify the deployment:
   ```bash
   eb open
   ```
   The application will automatically perform health checks against `/api/health`.

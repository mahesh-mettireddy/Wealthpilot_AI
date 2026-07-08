# WealthPilot AI - Digital Wealth Advisory

🚀 **Live Application:** [http://wealthpilot-env.eba-nxyswcfx.us-east-1.elasticbeanstalk.com](http://wealthpilot-env.eba-nxyswcfx.us-east-1.elasticbeanstalk.com)

WealthPilot AI is a next-generation digital wealth advisory platform designed for modern investors. It leverages Google's Gemini AI API to provide personalized portfolio recommendations, financial projections, and an interactive conversational Co-pilot that understands your entire financial context.

## 🌟 Key Features
- **AI-Powered Risk Profiling:** Determine your exact risk tolerance (Conservative, Moderate, Aggressive).
- **Personalized Portfolio Generation:** Gemini AI analyzes your profile, goals, horizon, and monthly investment to generate a custom-tailored mutual fund portfolio dynamically.
- **Conversational AI Co-pilot:** A floating chat widget that allows you to ask the AI questions directly about your generated portfolio. The Co-pilot has full context of your recommended funds, projections, and chat history.
- **Dynamic AI Model Resolution:** The backend automatically maps your Google API key to the correct Gemini model (e.g. `gemini-1.5-flash`), fully supporting both legacy `AIza...` and modern `AQ...` authentication keys.
- **Real-time Financial Projections:** Visualized growth charts over your investment horizon.

## 🛠 Prerequisites
- Java 17
- Maven 3.8+ (or use the included wrapper)
- A valid **Google Gemini API Key**

## ⚙️ Configuration

1. Set your Gemini API Key locally for development:
   ```env
   GEMINI_API_KEY=your-actual-gemini-api-key
   ```
   **DO NOT** commit your API key to source control.

## 💻 Running Locally

To run the application locally, use the Maven wrapper:

```bash
./mvnw clean spring-boot:run
```

Navigate to `http://localhost:8080` in your web browser to access the dashboard.

## ☁️ Deployment to AWS Elastic Beanstalk

This application is deployed on AWS Elastic Beanstalk (Java 17 Corretto platform).

### Deployment Steps:

1. Build the executable JAR:
   ```bash
   ./mvnw clean package
   ```
2. Deploy the application:
   ```bash
   eb deploy
   ```
3. Set the Gemini API key securely in the Elastic Beanstalk console:
   - Go to **Configuration -> Updates, monitoring, and routing**.
   - Add an environment property: `GEMINI_API_KEY = [Your Key]`.
   - Click Apply.
4. Verify the deployment:
   ```bash
   eb open
   ```

Smart Live Currency Converter
Overview

Smart Live Currency Converter is a full-stack web application that provides real-time currency conversion integrated with cryptocurrency data, financial news, smart alerts, and an interactive chatbot.
It delivers instant, analytics-driven financial insights with a modern dark-themed user interface and real-time backend updates using WebSocket.

Objectives

Implement a Spring Boot backend that fetches and broadcasts exchange rates every 10 seconds.

Provide real-time conversions, crypto price tracking, and financial news updates.

Allow users to:

Register and log in securely.

Save favorite currencies.

Set alerts for specific rate thresholds.

View conversion history.

Receive browser notifications when alerts are triggered.

Ensure secure JWT-based authentication and persistent storage.

Features
Feature	Description
Live Forex Conversion	Converts currencies with real-time rate updates every few seconds.
Cryptocurrency Tracker	Displays real-time prices for BTC, ETH, and LTC using the CoinGecko API.
Smart Alerts	Users can set rate-based alerts and receive browser notifications when triggered.
Financial News Feed	Shows live finance and cryptocurrency-related headlines.
Historical Trends	Displays rate history charts (1 Week, 1 Month, 1 Year) using Chart.js.
Chatbot Assistant	Allows interaction through a chatbot for conversions, news, or trends.
Secure Authentication	JWT-based login and registration for secure access.
Conversion History	Stores and displays past conversions with rate details and timestamps.
[Browser Frontend] <--> HTTPS REST (Auth, Alerts, News, History)
        |
        |---- WebSocket (STOMP) <--- broadcasts live rates
        |
[Backend: Spring Boot] --- Redis (Cache)
        |
        +--- PostgreSQL (Users, Alerts, History)
        |
        +--- External APIs (ExchangeRates, CoinGecko, NewsAPI)
Technology Stack
Layer	Technologies
Backend	Java 17, Spring Boot, Spring Security (JWT), Spring WebSocket (STOMP), Maven, JUnit
Frontend	HTML, CSS, JavaScript, Chart.js
Database	PostgreSQL (Production), H2 (Development)
Cache	Redis
APIs	ExchangeRatesAPI / OpenExchangeRates (Forex), CoinGecko (Crypto), NewsAPI (News)
Deployment	Docker, Docker Compose
Development Tools	IntelliJ IDEA, Visual Studio Code
Use Case Summary

Main Actors:

User: Registers, logs in, performs currency conversions, sets alerts, views historical data and news.

System: Fetches, stores, and broadcasts rate updates; triggers alerts when conditions are met.

Database Schema
Users Table

Stores user information such as email, password, and name.

Indexed by email for faster login.

Includes created_at and updated_at timestamps.

Alerts Table

Stores user-created alerts with conditions (>, <, etc.).

Linked to the Users table through a foreign key.

Used to send notifications when exchange rates meet specified conditions.

History Table

Logs each currency conversion with details such as currencies involved, amount, and rate.

Enables users to review their conversion history.

Setup Instructions
1. Clone the Repository
git clone https://github.com/yourusername/smart-live-currency-converter.git
cd smart-live-currency-converter

2. Backend Setup
cd backend
mvn clean install
mvn spring-boot:run

3. Frontend Setup
cd frontend
# Serve locally using a simple web server (e.g., VSCode Live Server)

4. Environment Variables

Create a .env or application.properties file with the following keys:

API_KEY_EXCHANGERATES=your_api_key
API_KEY_COINGECKO=your_api_key
API_KEY_NEWSAPI=your_api_key
JWT_SECRET=your_secret
VAPID_PUBLIC_KEY=your_vapid_public_key
VAPID_PRIVATE_KEY=your_vapid_private_key

5. Run with Docker (Optional)
docker-compose up --build

Limitations

Trend predictions are statistical estimates and not financial advice.

Web Push notifications require HTTPS and valid VAPID keys.

External APIs may have usage limits and require valid API keys for production use.

Contributors
Name	Registration Number
Aalif Hassan	RA2411026010893
Joshik R	RA2411026010885
Aditya Raj	RA2411026010888
Umika Kilari	RA2411026010980

Guide: Dr. Naveen P
Course: 21CSC203P – Advanced Programming Practice
Institution: SRM Institute of Science and Technology, Kattankulathur
Semester: 3rd Semester
Date: October 2025

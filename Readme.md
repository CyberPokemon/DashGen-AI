# DashGen AI

**DashGen AI** is an AI-powered Natural Language to Dashboard
Generator.\
It allows users to ask questions about a PostgreSQL database in plain
English and automatically generates:

-   SQL queries using **Google Gemini**
-   Data tables
-   Data visualizations (charts)
-   Executive summaries
-   Business recommendations

The system converts natural language into insights and visual dashboards
instantly.

------------------------------------------------------------------------

# Project Authors

-   Imon Mallik
-   Koushiki Kundu
-   Soumi Sahu

------------------------------------------------------------------------

# Project Overview

DashGen AI works in three major stages:

1.  **User Query (Natural Language)**
2.  **AI Generates SQL Query**
3.  **SQL Executes on PostgreSQL Database**
4.  **AI Analyzes Results**
5.  **Dashboard + Summary + Recommendation Generated**

This allows non-technical users to analyze database data without writing
SQL.

------------------------------------------------------------------------

# System Architecture

User Question → Gemini AI → SQL Generation → PostgreSQL Database → Data
Retrieval → AI Insight Generation → Dashboard UI

------------------------------------------------------------------------

# Features

-   Natural Language to SQL
-   AI-powered data insights
-   Automatic chart selection
-   Executive business summary
-   Interactive dashboard
-   PostgreSQL database integration
-   FastAPI backend
-   Gemini AI integration
-   Chart.js visualizations

------------------------------------------------------------------------

# Tech Stack

## Frontend

-   HTML
-   CSS
-   JavaScript
-   Chart.js

## Backend

-   FastAPI
-   Python

## AI

-   Google Gemini (via LangChain)

## Database

-   PostgreSQL

## Deployment

-   Render

------------------------------------------------------------------------

# Project Structure

    DashGen-AI/
    │
    ├── frontend
    │   ├── index.html
    │   ├── style.css
    │   └── script.js
    │
    ├── backend
    │   ├── app.py
    │   ├── main.py
    │   ├── db_connect.py
    │   ├── prompts.py
    │   └── requirements.txt
    │
    └── README.md

------------------------------------------------------------------------

# How It Works

## Step 1 --- User Input

User asks a question such as:

Example:

    Show total sales by region

------------------------------------------------------------------------

## Step 2 --- SQL Generation

Gemini AI converts the question into a SQL query based on the database
schema.

Example:

    SELECT region, SUM(sales)
    FROM orders
    GROUP BY region

------------------------------------------------------------------------

## Step 3 --- Data Fetching

The SQL query runs on the PostgreSQL database and returns results.

Example:

    [
     {"region": "East", "sales": 12000},
     {"region": "West", "sales": 15000}
    ]

------------------------------------------------------------------------

## Step 4 --- AI Analysis

Gemini analyzes the dataset and generates:

-   Executive summary
-   Recommended chart type
-   Business recommendation

------------------------------------------------------------------------

## Step 5 --- Dashboard Visualization

The frontend displays:

-   Data Table
-   Chart Visualization
-   AI Summary
-   AI Recommendation

------------------------------------------------------------------------

# Installation Guide

## 1. Clone the Repository

    git clone https://github.com/CyberPokemon/DashGen-AI.git
    cd DashGen-AI

------------------------------------------------------------------------

## 2. Install Dependencies

    pip install -r requirements.txt

------------------------------------------------------------------------

## 3. Setup Environment Variables

Create a `.env` file:

    GEMAI_API_KEY=your_gemini_api_key
    DB_URL=your_postgresql_connection_string

------------------------------------------------------------------------

## 4. Run the Backend

    uvicorn app:app --reload

Server will start at:

    http://127.0.0.1:8000

------------------------------------------------------------------------

## 5. Open the Frontend

Simply open:

    index.html

in your browser.

------------------------------------------------------------------------

# API Endpoints

## Health Check

    GET /

Response:

    {
     "message": "AI Dashboard API is running"
    }

------------------------------------------------------------------------

## Generate Dashboard

    POST /generate-dashboard

Request:

    {
     "question": "Show sales by region"
    }

Response:

    {
     "status": "success",
     "data": {
       "summary": "...",
       "table_data": [...],
       "chart_type": "bar",
       "call_to_action": "..."
     }
    }

------------------------------------------------------------------------

# Example User Queries

Users can ask questions like:

-   Total sales by region
-   Monthly revenue trend
-   Top 5 products by sales
-   Customer distribution by country
-   Yearly revenue growth

------------------------------------------------------------------------

# Future Improvements

-   Drag & Drop dashboard builder
-   Multi-chart dashboards
-   User authentication
-   Role-based data access
-   Export dashboard as PDF
-   Natural language follow-up questions
-   Streaming responses


------------------------------------------------------------------------


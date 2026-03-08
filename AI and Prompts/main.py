import os
import json
from dotenv import load_dotenv
from langchain_google_genai import ChatGoogleGenerativeAI
from prompts import get_formatting_prompt1,get_formatting_prompt2
from pydantic import BaseModel, Field
from db_connect import get_db_schema_string,execute_sql_to_json
from typing import List, Any # Add this import at the top

load_dotenv()

class DashboardResponse(BaseModel):
    sql: str = Field(description="The SQL query to run against the database")
    chart_type: str = Field(description="The type of chart: 'bar', 'line', 'pie', or 'kpi'")
    title: str = Field(description="A clean title for the chart")
    insight: str = Field(description="A 1-sentence executive summary of the data")


class AnalysisResponse(BaseModel):
    executive_summary: str = Field(description="2-sentence business summary")
    # Using List[dict] or List[Any] fixes the "missing field" error
    formatted_data: List[dict] = Field(description="The JSON data results as a list of objects")
    chart_type: str = Field(description="The confirmed chart type")
    call_to_action: str = Field(description="A business recommendation")

def get_llm():
    api_key = os.environ.get("GEMAI_API_KEY")
    if not api_key:
        raise ValueError("GEMAI_API_KEY not found.")
    return ChatGoogleGenerativeAI(
        model="gemini-2.5-flash", 
        google_api_key=api_key,
        temperature=0
    )

def generate_dashboard_logic(user_prompt):
    llm = get_llm()
    
    # --- STAGE 1: SQL Generation ---
    structured_model1 = llm.with_structured_output(DashboardResponse)
    schema = get_db_schema_string()
    full_prompt = f"{get_formatting_prompt1(schema)}\n\nUser Request: {user_prompt}"
    sql_response = structured_model1.invoke(full_prompt)
    
    # --- STAGE 2: Database Fetching ---
    full_json_data = execute_sql_to_json(sql_response.sql)
    
    if not full_json_data:
        return {
            "summary": "No records found in the database.",
            "table_data": [],
            "chart_type": "kpi",
            "data_points": 0
        }

    # --- STAGE 3: Insight Synthesis (Using a Preview) ---
    # We only send the first 10 rows for analysis to avoid 'NoneType' errors
    if len(full_json_data)>10:
        data_preview = full_json_data[:10] 
    else:
        data_preview=full_json_data
    
    structured_model2 = llm.with_structured_output(AnalysisResponse)
    full_prompt2 = get_formatting_prompt2(user_prompt, json.dumps(data_preview))
    
    analysis = structured_model2.invoke(full_prompt2)

    # --- SAFETY CHECK ---
    # If the AI still returns None, we provide a fallback
    if analysis is None:
        return {
            "summary": "Data retrieved successfully, but analysis could not be generated.",
            "table_data": full_json_data,
            "chart_type": sql_response.chart_type,
            "data_points": len(full_json_data)
        }

    # --- FINAL RETURN ---
    return {
        "summary": analysis.executive_summary,
        "table_data": full_json_data, # Return the WHOLE table for the UI
        "chart_type": analysis.chart_type,
        "data_points": len(full_json_data),
        "call_to_action": analysis.call_to_action
    }
   

# user_query=input("Enter your question")
# logic = generate_dashboard_logic(user_query)
# print("--- EXECUTIVE DASHBOARD ---")
# print(logic)
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
    
    # STAGE 1
    structured_model1 = llm.with_structured_output(DashboardResponse)
    schema = get_db_schema_string()
    full_prompt = f"{get_formatting_prompt1(schema)}\n\nUser Request: {user_prompt}"
    response = structured_model1.invoke(full_prompt)
    
    # STAGE 2
    json_data = execute_sql_to_json(response.sql)
    
    # Safety Check: If no data, return a custom response or handle gracefully
    if not json_data:
        return {
            "executive_summary": "No data was found for the requested period.",
            "formatted_data": [],
            "chart_type": "kpi",
            "call_to_action": "Try broadening your search filters."
        }

    # STAGE 3
    structured_model2 = llm.with_structured_output(AnalysisResponse)
    full_prompt2 = get_formatting_prompt2(user_prompt, json.dumps(json_data))
    return structured_model2.invoke(full_prompt2)

user_query = "Show me which insurers are struggling with pending claims, and what is the total value at risk"
logic = generate_dashboard_logic(user_query)
print("--- EXECUTIVE DASHBOARD ---")
print(f"Summary: {logic.executive_summary}")
print(f"Action: {logic.call_to_action}")
print(f"Data Points: {len(logic.formatted_data)}")
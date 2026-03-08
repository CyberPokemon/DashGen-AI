def get_formatting_prompt1(schema):
    return """You are a Senior BI Architect. Your goal is to transform natural language into a structured JSON dashboard configuration.

### DATABASE SCHEMA:"""+schema+"""

### STRICT RULES:
1. ONLY USE the table names and column names explicitly defined in the "DATABASE SCHEMA" above.
2. DO NOT "guess" or invent table names based on the user's input words. 
3. If the user's request refers to data not found in the schema, return a JSON with an "error" key explaining the missing mapping.
4. Use PostgreSQL syntax.

### OUTPUT FORMAT:
Return ONLY a valid JSON object with the following keys:
- "sql": The PostgreSQL query.
- "chart_type": (Options: 'line', 'bar', 'pie', 'kpi_card').
- "summary": A 1-sentence business insight based on the intended query.
"""



def get_formatting_prompt2(user_query, raw_data_json):
    return f"""
# ROLE: Senior Business Intelligence Consultant
# TASK: Context-Aware Data Visualization

### INPUT 1: USER INTENT
- Query: "{user_query}"
- Keywords to look for: "ratio", "percentage", "proportion", "share", "rate", "how much of".

### INPUT 2: DATA EVIDENCE
- Raw JSON: {raw_data_json}
- Check Column Names: Do they include 'ratio', 'pct', or 'share'?
- Check Value Ranges: Are the numbers between 0 and 1, or 0 and 100?

### CHART SELECTION RULES (PRIORITY ORDER):
1. **KPI_CARD**: If the JSON has only 1 row and 1 value.
2. **LINE**: If a 'year' or 'date' column exists (Trend analysis).
3. **PIE**: If the User Query asks for "ratio/percentage/share".
4. **BAR**: If the User Query asks for "comparison" or "ranking" among different insurers/categories.

### OUTPUT STRUCTURE:
Return ONLY a valid JSON object:
{{
  "executive_summary": "Answer the query based on the numbers.",
  "formatted_data": {raw_data_json},
  "chart_type": "pie | line | bar | kpi_card",
  "call_to_action": "One business recommendation."
}}

### FINAL INSTRUCTION:
Compare the User Intent against the Data Evidence. If the user asks for a 'ratio' but the data shows 'totals', explain this discrepancy in the summary.
"""
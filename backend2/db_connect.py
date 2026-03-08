import psycopg2
import os
import json
from dotenv import load_dotenv
import pandas as pd
from psycopg2.extras import RealDictCursor

load_dotenv()

def db_connect():
    return psycopg2.connect(os.getenv("DB_URL"))


# db_connect.py - Ensure this pattern is followed
def get_db_schema_string():
    try:
        conn = db_connect()
        with conn.cursor() as cur:
            # This query pulls all table and column names from Postgres
            cur.execute("""
                SELECT table_name, column_name 
                FROM information_schema.columns 
                WHERE table_schema = 'public'
            """)
            rows = cur.fetchall()
            
            # Format into a readable string for the AI
            schema_map = {}
            for table, col in rows:
                if table not in schema_map:
                    schema_map[table] = []
                schema_map[table].append(col)
            
            return json.dumps(schema_map) # THIS is what {schema} becomes
    except Exception as e:
        return f"Error: {str(e)}"
    
def execute_sql_to_json(sql_query):
    """Executes SQL and returns a list of JSON-like dictionaries."""
    try:
        conn = db_connect()
        # RealDictCursor makes each row a dictionary: {"column": "value"}
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(sql_query)
            results = cur.fetchall()
        conn.close()
        return results
    except Exception as e:
        return {"error": str(e)}
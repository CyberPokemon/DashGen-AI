from fastapi import FastAPI
from pydantic import BaseModel
from main import generate_dashboard_logic
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="AI Dashboard API",
    description="Natural language to SQL dashboard generator",
    version="1.0"
)

# Enable CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # allow all origins (good for development)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class QueryRequest(BaseModel):
    question: str


@app.get("/")
def home():
    return {"message": "AI Dashboard API is running"}


@app.post("/generate-dashboard")
def generate_dashboard(request: QueryRequest):
    try:
        result = generate_dashboard_logic(request.question)
        return {
            "status": "success",
            "data": result
        }

    except Exception as e:
        return {
            "status": "error",
            "message": str(e)
        }
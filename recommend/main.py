from fastapi import FastAPI, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from recommend import LightGCNService
from train import train_model
from prepare_dataset import prepare_dataset

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

service = LightGCNService()

@app.post("/recommend/prepare_dataset")
def prepare(background_tasks: BackgroundTasks):
    background_tasks.add_task(prepare_dataset)
    return {"status": "completed", "task": "prepare_dataset"}

@app.post("/recommend/train")
def train(background_tasks: BackgroundTasks):
    background_tasks.add_task(train_model)
    return {"status": "completed", "task": "train_model"}

@app.get("/recommend/{account_id}")
def recommend(account_id: str, k: int = 10):
    result = service.recommend(account_id, k)
    return {"accountId": account_id, "recommendations": result}

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8086,
        reload=True
    )
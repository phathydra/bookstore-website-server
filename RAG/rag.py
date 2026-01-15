from pymongo import MongoClient
from sentence_transformers import SentenceTransformer
import numpy as np
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from embeded import embed_faqs

mongo_uri = "mongodb+srv://tlcn_user:tlcn_bookstore@cluster0.jmrvw.mongodb.net/"
client = MongoClient(mongo_uri)
db = client["BookStore"]
embedded_collection = db["faq_embeded"]

model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")

app = FastAPI(title="Bookstore FAQ Retrieval API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def retrieve_answer(user_question, top_k=1):
    """Tìm câu trả lời gần nhất trong MongoDB"""
    question_vector = model.encode(user_question)
    question_norm = np.linalg.norm(question_vector)

    docs = list(embedded_collection.find({}))
    best_doc = None
    best_score = -1

    for doc in docs:
        doc_vector = np.array(doc["embedding"])
        similarity = np.dot(question_vector, doc_vector) / (question_norm * np.linalg.norm(doc_vector))
        if similarity > best_score:
            best_score = similarity
            best_doc = doc

    if best_doc:
        return best_doc["faq_id"]
    else:
        return "Không tìm thấy câu trả lời phù hợp."
    

@app.post("/embed")
async def trigger_embedding():
    result = embed_faqs()
    return result
    
@app.post("/ask")
async def ask_question(request: Request):
    body = await request.json()
    question = body.get("question", "").strip()

    if not question:
        return {"error" : "No question found"}
    
    answer = retrieve_answer(question)
    return {"answer" : answer}

if __name__ == "__main__":
    uvicorn.run("rag:app", host="0.0.0.0", port=8085, reload=True)
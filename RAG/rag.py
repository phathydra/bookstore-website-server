from pymongo import MongoClient
from sentence_transformers import SentenceTransformer
import numpy as np

mongo_uri = "mongodb+srv://tlcn_user:tlcn_bookstore@cluster0.jmrvw.mongodb.net/"
client = MongoClient(mongo_uri)
db = client["BookStore"]
embedded_collection = db["faq_embeded"]

model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")

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
        return best_doc["content"]
    else:
        return "Không tìm thấy câu trả lời phù hợp."

if __name__ == "__main__":
    while True:
        user_input = input("\nKhách hàng: ")
        if user_input.lower() in ["exit", "quit", "bye"]:
            print("Kết thúc hội thoại.")
            break

        answer = retrieve_answer(user_input)
        print("Chatbot:\n", answer)
from pymongo import MongoClient
from langchain_text_splitters import RecursiveCharacterTextSplitter
from sentence_transformers import SentenceTransformer
from tqdm import tqdm

mongo_uri = "mongodb+srv://tlcn_user:tlcn_bookstore@cluster0.jmrvw.mongodb.net/"
client = MongoClient(mongo_uri)
db = client["BookStore"]
faq_collection = db["faq"]
embedded_collection = db["faq_embeded"]

model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")

splitter = RecursiveCharacterTextSplitter(
    chunk_size=300,
    chunk_overlap=50,
    separators=["\n", ".", "?", "!", ";"]
)
def embed_faqs():
    faqs = list(faq_collection.find({}))
    embedded_collection.delete_many({})

    for faq in tqdm(faqs, desc="Embedding FAQs"):
        faq_id = str(faq["_id"])
        question = faq.get("question", "")
        answer = faq.get("answer", "")
        content = f"Q: {question}\nA: {answer}"

        chunks = splitter.split_text(content)

        for chunk in chunks:
            embedding_vector = model.encode(chunk).tolist()
            embedded_collection.insert_one({
                "faq_id": faq_id,
                "content": chunk,
                "embedding": embedding_vector
            })
    return {
        "status": "success",
    }
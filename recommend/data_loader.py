from pymongo import MongoClient

EVENT_WEIGHTS = {
    "VIEW": 1,
    "CLICK_SUMMARY": 2,
    "ADD_TO_CART": 3,
    "ORDER_SUCCESS": 5
}

ALLOWED_EVENTS = {"VIEW", "CLICK_SUMMARY", "ADD_TO_CART", "ORDER_SUCCESS"}

def load_interactions():
    mongo_uri = "mongodb+srv://tlcn_user:tlcn_bookstore@cluster0.jmrvw.mongodb.net/"
    client = MongoClient(mongo_uri)
    db = client["BookStore"]
    collection = db["user_interactions"]

    interactions = []

    for doc in collection.find():
        event = doc.get("eventType")

        if event not in ALLOWED_EVENTS:
            continue

        account_id = doc.get("accountId")
        book_id = doc.get("bookId")

        if not account_id or not book_id:
            continue

        if event == "ORDER_SUCCESS":
            order_items = doc.get("orderData", {}).get("items", [])
            for item in order_items:
                bid = item.get("bookId")
                if bid:
                    interactions.append((account_id, bid, EVENT_WEIGHTS[event]))
            continue

        interactions.append((account_id, book_id, EVENT_WEIGHTS[event]))

    client.close()
    return interactions

import torch
from recbole.model.general_recommender import LightGCN
from recbole.config import Config
from recbole.data import create_dataset
from recbole.data.interaction import Interaction


class LightGCNService:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._initialized = False
        return cls._instance

    def __init__(self):
        if self._initialized:
            return

        config = Config(
            model="LightGCN",
            dataset="bookstore",
            config_file_list=["lightgcn.yaml"] if __import__("os").path.exists("lightgcn.yaml") else []
        )

        self.dataset = create_dataset(config)
        self.model = LightGCN(config, self.dataset).to(config["device"])
        
        model_path = "saved/lightgcn-model.pth"
        self.model.load_state_dict(torch.load(model_path, map_location=config["device"]))
        self.model.eval()

        self._initialized = True
        print("LightGCN Service đã sẵn sàng!")

    def recommend(self, user_id: str, k: int = 10):
        if user_id not in self.dataset.field2token_id["user_id"]:
            return []

        uid = self.dataset.field2token_id["user_id"][user_id]

        interaction = Interaction({
            "user_id": torch.tensor([uid], device=self.model.device)
        })

        with torch.no_grad():
            scores = self.model.full_sort_predict(interaction).cpu().numpy().flatten()

        topk_indices = scores.argsort()[::-1][:k + 50]

        interacted_items = set(
            self.dataset.inter_feat[self.dataset.inter_feat["user_id"] == uid]["item_id"].to_numpy()
        )

        recommendations = []
        for item_idx in topk_indices:
            item_token = self.dataset.id2token("item_id", item_idx)
            if item_token not in interacted_items:
                recommendations.append(item_token)
            if len(recommendations) >= k:
                break

        return recommendations
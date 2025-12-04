from recbole.config import Config
from recbole.data import create_dataset, data_preparation
from recbole.model.general_recommender import LightGCN
from recbole.trainer import Trainer
from recbole.utils import init_seed
import warnings
import os
import torch

warnings.filterwarnings("ignore", category=FutureWarning)

def train_model():
    config = Config(
        model="LightGCN",
        dataset="bookstore",
        config_file_list=["lightgcn.yaml"],
        config_dict={
        "checkpoint_dir": "saved",
        "save_model": False,
        "save_checkpoint": False,
        }
    )

    init_seed(config["seed"], config["reproducibility"])

    dataset = create_dataset(config)
    print("Dataset loaded, interaction fields:", dataset.inter_feat.columns)

    train_data, valid_data, test_data = data_preparation(config, dataset)

    model = LightGCN(config, train_data.dataset).to(config["device"])

    trainer = Trainer(config, model)
    best_valid_score, best_valid_result = trainer.fit(
        train_data, valid_data,
        show_progress=True
    )

    print("Best valid result:", best_valid_result)
    print("Number of users:", dataset.user_num)
    print("Number of items:", dataset.item_num)
    print("Number of interactions:", dataset.inter_num)

    os.makedirs("saved", exist_ok=True)
    save_path = "saved/lightgcn-model.pth"
    torch.save(model.state_dict(), save_path)
    print(f"ĐÃ LƯU MODEL THÀNH CÔNG VÀO: {save_path}")

if __name__ == "__main__":
    train_model()
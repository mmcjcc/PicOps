"""PicOps ML sidecar.

One model (OpenCLIP ViT-B/32) provides:
- /analyze: image bytes -> 512-d embedding + zero-shot tags
- /embed-text: free-text query -> 512-d embedding (for semantic search)

Everything runs locally on CPU; photos never leave the compose network.
"""
import io
import os

import numpy as np
import open_clip
import torch
from fastapi import FastAPI, Request
from insightface.app import FaceAnalysis
from PIL import Image

MODEL = "ViT-B-32"
PRETRAINED = "laion2b_s34b_b79k"

app = FastAPI()
model, _, preprocess = open_clip.create_model_and_transforms(
    MODEL, pretrained=PRETRAINED, cache_dir=os.environ.get("HF_HOME"))
tokenizer = open_clip.get_tokenizer(MODEL)
model.eval()

# Face detection + 512-d ArcFace embeddings (same stack Immich uses).
face_app = FaceAnalysis(name="buffalo_l",
                        root=os.environ.get("HF_HOME", "/models"),
                        providers=["CPUExecutionProvider"])
face_app.prepare(ctx_id=0, det_size=(640, 640))
FACE_MIN_SCORE = 0.55

# Zero-shot tag vocabulary: broad, personal-photo-oriented.
LABELS = [
    "dog", "cat", "bird", "horse", "wildlife", "fish",
    "beach", "ocean", "lake", "river", "mountains", "forest", "desert",
    "snow", "sunset", "night sky", "fireworks",
    "city", "architecture", "food", "flowers", "garden",
    "people", "portrait", "group photo", "baby", "wedding", "birthday",
    "sports", "car", "boat", "airplane", "concert",
    "camping", "hiking", "holiday", "art", "document", "screenshot",
]
with torch.no_grad():
    _label_emb = model.encode_text(tokenizer([f"a photo of {l}" for l in LABELS]))
    _label_emb /= _label_emb.norm(dim=-1, keepdim=True)

TAG_THRESHOLD = 0.20
MAX_TAGS = 6


@app.post("/analyze")
async def analyze(request: Request):
    data = await request.body()
    img = Image.open(io.BytesIO(data)).convert("RGB")
    with torch.no_grad():
        emb = model.encode_image(preprocess(img).unsqueeze(0))
        emb /= emb.norm(dim=-1, keepdim=True)
        sims = (emb @ _label_emb.T).squeeze(0)
    top = torch.topk(sims, min(MAX_TAGS * 2, len(LABELS)))
    tags = [
        {"tag": LABELS[i], "score": round(float(sims[i]), 4)}
        for i in top.indices
        if float(sims[i]) >= TAG_THRESHOLD
    ][:MAX_TAGS]
    return {"embedding": emb.squeeze(0).tolist(), "tags": tags}


@app.post("/embed-text")
async def embed_text(payload: dict):
    with torch.no_grad():
        emb = model.encode_text(tokenizer([str(payload.get("text", ""))]))
        emb /= emb.norm(dim=-1, keepdim=True)
    return {"embedding": emb.squeeze(0).tolist()}


@app.post("/faces")
async def faces(request: Request):
    data = await request.body()
    img = Image.open(io.BytesIO(data)).convert("RGB")
    arr = np.asarray(img)[:, :, ::-1]  # RGB -> BGR for insightface
    found = face_app.get(arr)
    return {"faces": [
        {
            "bbox": [max(0, int(v)) for v in f.bbox],
            "score": round(float(f.det_score), 4),
            "embedding": f.normed_embedding.tolist(),
        }
        for f in found if float(f.det_score) >= FACE_MIN_SCORE
    ]}


@app.get("/health")
def health():
    return {"ok": True, "model": f"{MODEL}/{PRETRAINED}", "faces": "buffalo_l"}

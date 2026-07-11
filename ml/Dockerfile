# PicOps ML sidecar: OpenCLIP ViT-B/32 + InsightFace (CPU) behind FastAPI.
FROM python:3.11-slim
# build-essential: insightface compiles Cython extensions at install time
# libglib2.0-0 / libgomp1: opencv-headless + onnxruntime runtime libs
RUN apt-get update && apt-get install -y --no-install-recommends \
      build-essential g++ libglib2.0-0 libgomp1 \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu \
 && pip install --no-cache-dir -r requirements.txt
COPY app.py .
# model weights cache lives on a volume so restarts don't re-download
ENV HF_HOME=/models
EXPOSE 8000
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]

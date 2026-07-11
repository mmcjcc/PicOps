# PicOps ML sidecar: OpenCLIP ViT-B/32 on CPU behind a tiny FastAPI.
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu \
 && pip install --no-cache-dir -r requirements.txt
COPY app.py .
# model weights cache lives on a volume so restarts don't re-download
ENV HF_HOME=/models
EXPOSE 8000
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]

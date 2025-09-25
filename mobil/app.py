from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import torch.nn.functional as F
import requests
import random
import csv

app = Flask(__name__)

# Model ve tokenizer yükleniyor
model_name = "savasy/bert-base-turkish-sentiment-cased"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSequenceClassification.from_pretrained(model_name)

# Etiketler dinamik belirleniyor
num_labels = model.config.num_labels
if num_labels == 3:
    labels = ["Negatif", "Nötr", "Pozitif"]
elif num_labels == 2:
    labels = ["Negatif", "Pozitif"]
else:
    raise ValueError(f"Beklenmeyen sınıf sayısı: {num_labels}")

# TMDB bilgileri
TMDB_API_KEY = "6899ac0ff0fb9dbbf05efbec9c8187b2"
TMDB_API_URL = "https://api.themoviedb.org/3"

emotion_to_genre = {
    "Pozitif": 35,    # Komedi
    "Negatif": 18,    # Dram
    "Nötr": 10749     # Romantik
}

def analyze_sentiment(text, neutral_threshold=0.55):
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True)
    with torch.no_grad():
        outputs = model(**inputs)
        probs = F.softmax(outputs.logits, dim=1)[0]
        probs_list = probs.tolist()

    if num_labels == 3:
        negatif, notr, pozitif = probs_list
        if max(negatif, pozitif) < neutral_threshold:
            duygu = "Nötr"
        else:
            duygu = "Negatif" if negatif > pozitif else "Pozitif"
    elif num_labels == 2:
        negatif, pozitif = probs_list
        duygu = "Negatif" if negatif > pozitif else "Pozitif"
    else:
        raise ValueError("Geçersiz çıktı boyutu.")

    return duygu, {label: round(prob, 3) for label, prob in zip(labels, probs_list)}

def get_movies_by_genre(genre_id):
    page = random.randint(1, 10)
    url = f"{TMDB_API_URL}/discover/movie"
    params = {
        "api_key": TMDB_API_KEY,
        "with_genres": genre_id,
        "sort_by": "popularity.desc",
        "language": "tr-TR",
        "page": page
    }
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        return data.get("results", [])[:5]
    except requests.RequestException as e:
        print(f"TMDB API isteği hatası: {e}")
        return []

@app.route('/')
def home():
    return "Merhaba! /analyze endpointine POST isteği atarak duygu analizi yapabilirsin."

@app.route('/analyze', methods=['POST'])
def analyze():
    data = request.get_json(force=True, silent=True)
    if not data:
        return jsonify({"error": "Geçerli JSON verisi gönderilmedi."}), 400

    user_text = data.get('text', '').strip()
    if not user_text:
        return jsonify({"error": "Lütfen 'text' alanını gönderiniz ve boş bırakmayınız."}), 400

    duygu, olasiliklar = analyze_sentiment(user_text)
    genre_id = emotion_to_genre.get(duygu, 35)

    movies = get_movies_by_genre(genre_id)

    return jsonify({
        "sentiment": duygu,
        "probabilities": olasiliklar,
        "movies": [
            {
                "title": movie.get("title", "Bilinmiyor"),
                "release_date": movie.get("release_date", "Bilinmiyor"),
                "overview": movie.get("overview", "Açıklama yok")
            }
            for movie in movies
        ]
    })

def read_csv_samples(file_path):
    samples = []
    with open(file_path, encoding="utf-8") as f:
        reader = csv.reader(f)
        # Başlık satırı varsa atla, yoksa doğrudan oku:
        header = next(reader)
        # Eğer header "text" ve "label" değilse, onu da örneğin atla:
        if header[0].lower() != "text" or header[1].lower() != "label":
            f.seek(0)  # Başlıksa geri al
            reader = csv.reader(f)
        for row in reader:
            if len(row) >= 2:
                samples.append((row[0], row[1]))
    return samples

def calculate_accuracy(samples):
    correct = 0
    for text, true_label in samples:
        pred_label, _ = analyze_sentiment(text)
        if pred_label == true_label:
            correct += 1
    return correct / len(samples) if samples else 0

if __name__ == "__main__":
    csv_path = "aa.csv"  # Dosya adı
    test_samples = read_csv_samples(csv_path)
    accuracy = calculate_accuracy(test_samples)
    print(f"CSV'den okunan {len(test_samples)} örnekle model doğruluk oranı: {accuracy*100:.2f}%")

    # Flask uygulamasını başlat ve sürekli açık kalmasını sağla
    app.run(host="0.0.0.0", port=5000, debug=True)
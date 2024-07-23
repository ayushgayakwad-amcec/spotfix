import firebase_admin
from firebase_admin import credentials, db
import tweepy
from better_profanity import profanity
from datetime import datetime, timedelta
import requests
import pytz

cred = credentials.Certificate('../spotfix-twitter-bot/spotfix-ambition-firebase-adminsdk-hve5p-4fd0a8e263.json')
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://spotfix-ambition-default-rtdb.firebaseio.com/'
})

api_key = '48A9o1xIgR0HUVrBLcXWkjstd'
api_secret_key = 'CmIg01fTeqbks7wDMY9tUCMPiIHdzX7huqwSAbuMhW3sp3a4jp'
access_token = '1814600800553086976-OMfisyFQj4Ne5eUNtPKkNr9Dcu69no'
access_token_secret = 't61K8biRGQCcdiOicYtgH8nGdq1c8aehTM8R728ussRFn'
bearer_token = 'AAAAAAAAAAAAAAAAAAAAAFDBuwEAAAAA%2BtCYgiUhAGfIqQd4ABw0RF0yurU%3DS8oVpFtCUye5Z29nR4ZfpqUA4fyLMn2tqhBl1vCzIogcDownqV'

client = tweepy.Client(
    bearer_token=bearer_token,
    consumer_key=api_key,
    consumer_secret=api_secret_key,
    access_token=access_token,
    access_token_secret=access_token_secret
)

profanity.load_censor_words()

hashtags_dict = {
    'water': ["#WaterIssue", "#LocalIssue", "#WaterCrisis", "#Community"],
    'electricity': ["#PowerOutage", "#ElectricityIssue", "#LocalIssue", "#Community"],
    'construction': ["#ConstructionIssue", "#LocalIssue", "#Building", "#Community"],
    'sanitation': ["#SanitationIssue", "#Cleanliness", "#LocalIssue", "#Community"],
}

def current_utc_time():
    return datetime.now(pytz.utc)

def get_location_name(lat, lng):
    url = f"https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat={lat}&lon={lng}"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        return data.get("display_name", "Unknown location")
    return "Unknown location"

def post_tweets(category):
    ref = db.reference(category)
    issues = ref.get()
    
    if issues:
        for key, data in issues.items():
            name = data.get('name')
            latitude = data.get('latitude')
            longitude = data.get('longitude')
            desc = data.get('desc')
            status = data.get('status')
            reported_time = data.get('reported_time')

            if reported_time:
                reported_time = datetime.strptime(reported_time, '%Y-%m-%d %H:%M:%S').replace(tzinfo=pytz.utc)
            
            if status == 'unsolved':
                if reported_time and current_utc_time() - reported_time > timedelta(hours=12):
                    if not profanity.contains_profanity(desc):
                        hashtags = " ".join(hashtags_dict.get(category, ["#LocalIssue"]))
                        tweet = f"⚠️ REMINDER ⚠️\n\nThe issue reported: '{desc}' \n\nLocation: {latitude}, {longitude} \n\n{hashtags}\n\nThis issue has been unresolved for over 12 hours. Please address it urgently!"
                        
                        try:
                            response = client.create_tweet(text=tweet)
                            print(f"Reminder tweet posted successfully! Tweet ID: {response.data['id']}")
                        except tweepy.TweepyException as e:
                            print(f"Error posting reminder tweet: {e}")
                    else:
                        print(f"Profanity detected in issue {key}: {desc}")
                else:
                    if not profanity.contains_profanity(desc):
                        hashtags = " ".join(hashtags_dict.get(category, ["#LocalIssue"]))
                        location_name = get_location_name(latitude, longitude)
                        tweet = f"⚠️ NEW ISSUE REPORTED ⚠️\n\n{desc} \n\nLocation: {location_name} ({latitude}, {longitude}) \n\n{hashtags}"
                        
                        try:
                            response = client.create_tweet(text=tweet)
                            print(f"Tweet posted successfully! Tweet ID: {response.data['id']}")
                        except tweepy.TweepyException as e:
                            print(f"Error posting tweet: {e}")
                    else:
                        print(f"Profanity detected in issue {key}: {desc}")
            else:
                print(f"Issue {key} is already solved or not applicable for reminders. Skipping...")

categories = ['water', 'electricity', 'construction', 'sanitation']
for category in categories:
    post_tweets(category)

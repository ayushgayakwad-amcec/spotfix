import firebase_admin
from firebase_admin import credentials, db
import tweepy
from better_profanity import profanity
import os

# Initialize Firebase
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

# Initialize better_profanity
profanity.load_censor_words()

# Define hashtags
hashtags_dict = {
    'water': ["#WaterIssue", "#LocalIssue", "#WaterCrisis", "#Community"],
    'electricity': ["#PowerOutage", "#ElectricityIssue", "#LocalIssue", "#Community"],
    'construction': ["#ConstructionIssue", "#LocalIssue", "#Building", "#Community"],
    'sanitation': ["#SanitationIssue", "#Cleanliness", "#LocalIssue", "#Community"],
}

# Function to post tweets
def post_tweets(category):
    ref = db.reference(category)
    issues = ref.get()
    
    if issues:
        for key, data in issues.items():
            name = data.get('name')
            latitude = data.get('latitude')
            longitude = data.get('longitude')
            desc = data.get('desc')
            status = data.get('status')  # Get the status of the issue
            
            # Check if the status is 'unsolved'
            if status == 'unsolved':
                if not profanity.contains_profanity(desc):
                    hashtags = " ".join(hashtags_dict.get(category, ["#LocalIssue"]))
                    tweet = f"⚠️ NEW ISSUE REPORTED ⚠️\n\n{desc} \n\nLocation: {latitude}, {longitude} \n\n{hashtags}"
                    
                    try:
                        response = client.create_tweet(text=tweet)
                        print(f"Tweet posted successfully! Tweet ID: {response.data['id']}")
                    except tweepy.TweepyException as e:
                        print(f"Error posting tweet: {e}")
                else:
                    print(f"Profanity detected in issue {key}: {desc}")
            else:
                print(f"Issue {key} is already solved. Skipping...")

# Post tweets for each category
categories = ['water', 'electricity', 'construction', 'sanitation']
for category in categories:
    post_tweets(category)
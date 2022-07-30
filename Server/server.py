from firebase_admin import initialize_app, messaging, credentials
from firebase_admin.messaging import Notification
from json import dumps
from os import environ as env

cred = credentials.Certificate(env.get("FIREBASE_SDK_CERT"))
app = initialize_app(cred)

m = messaging.Message(
        data = {
            "Hello": "World!"
        },
        notification = Notification(
            title = "Stefan",
            body = "Hola baby",
            image=None
        ),
        topic="testing")
print(messaging.send(m, app=app))
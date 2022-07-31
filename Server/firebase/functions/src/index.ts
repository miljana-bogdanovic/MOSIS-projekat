import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { MessagingPayload } from "firebase-admin/lib/messaging/messaging-api";
import { firestore } from "firebase-admin";

admin.initializeApp({
    credential: admin.credential.applicationDefault()
});
const messagingService = admin.messaging();

export const partyCreated = functions.firestore
    .document("/parties/{partyId}")
    .onCreate((snapshot, _) => {
        const party = snapshot.data();

        const message = `${ party.organizer.firstName } ${ party.organizer.lastName} is throwing a new party '${ party.name }'!`;

        const msg : MessagingPayload = {
            notification: {
                sound: "default",
                title: "New party!",
                body: message
            },
            data: { 
                body: JSON.stringify(party),
                "notification-type": "new-party"
            }
        };

        messagingService
            .sendToTopic(`new-party-${ party.organizer.id }`, msg)
            .then((response) => functions.logger.debug(response))
            .catch((er) => functions.logger.error(er));

        firestore()
            .collection("users")
            .doc(party.organizer.id)
            .update({
                "partyNo": firestore.FieldValue.increment(1),
                "parties": firestore.FieldValue.arrayUnion(party.id)
            })
            .then(res => functions.logger.info("Update successful!", res))
            .catch(er => functions.logger.error(er));

        return "OK";
});

import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { v4 as uuidv4 } from "uuid";
import * as util from "../util";

export async function rejectedToSelected(eventId: string) {
	// move a user from the rejected list to the selected list
	// (the list of ppl not selected by the lotto, to the list of ppl selected by it)

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		logger.error(
			`Failed to move from rejected to selected in ${eventId}. Event does not exist.`,
		);
		return;
	}

	const eventDoc = event.data()!;

	// the rejectedList was already randomized when the lottery was
	// drawn in drawLottery(), so we'll just pick the last user in it

	if (eventDoc.rejectedList.length === 0) {
		logger.error(
			`Failed to move from rejected to selected in ${eventId}. Rejection list empty.`,
		);
		return;
	}

	const userId = eventDoc.rejectedList.at(-1)!;

	await eventRef.update({
		rejectedList: FieldValue.arrayRemove(userId),
	});

	await eventRef.update({
		selectedList: FieldValue.arrayUnion(userId),
	});

	// send the lucky user a notification informing them they've
	// been reconsidered for joining the event

	// note that the sendNotification util function already takes care of
	// not sending the push notification if the user has that disabled

	const notificationId = uuidv4();

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;
	const notificationRef = notificationCollection.doc(notificationId);

	notificationRef.create(
		util.enforceFull<NotificationDocument>({
			eventId: eventId,

			targetUsers: [userId],

			title: eventDoc.eventName,
			message: "You've been reconsidered for an event!",

			kind: {
				kind: "LOTTERY",
				selected: true,
			},
		}),
	);

	const notification = await notificationRef.get();
	const notificationDoc = notification.data()!;

	await util.sendNotification(userId, notificationId, notificationDoc);
}

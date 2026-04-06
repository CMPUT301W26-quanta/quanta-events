import { FieldPath, FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import * as util from "../util";

export async function deleteEvent(eventId: string) {
	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		logger.error(`Failed to delete event ${eventId}. Event does not exist.`);
		return;
	}

	const eventDoc = event.data()!;

	// remove the event's image

	if (eventDoc.imageId !== null) {
		await util.removeImage(eventDoc.imageId);
	}

	// remove the event's subcollections

	const commentCollection = eventRef.collection("comments") as CommentDocCollection;
	await db.recursiveDelete(commentCollection);

	// remove the event

	await eventRef.delete();

	// remove notifications belonging to the now deleted event;
	// collect their IDs

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;
	const notifications = await notificationCollection.get();

	const deletedNotificationIds: string[] = [];

	const notificationUpdatePromises = notifications.docs.map((notification) => {
		const notificationDoc = notification.data()!;

		if (notificationDoc.eventId !== eventId) return;

		// delete this notification

		deletedNotificationIds.push(notification.id);
		return notification.ref.delete();
	});

	// remove the deleted event from user's enteredEvents,
	// and the deleted notifications from their undismissedNotifications

	const userCollection = db.collection("users") as UserDocCollection;
	const users = await userCollection.get();

	const userUpdatePromises: Promise<any>[] = [];

	for (const user of users.docs) {
		const userDoc = user.data()!;

		if (userDoc.entrant?.enteredEvents.includes(eventId)) {
			userUpdatePromises.push(
				user.ref.update(
					new FieldPath("entrant", "enteredEvents"),
					FieldValue.arrayRemove(eventId),
				),
			);
		}

		if (userDoc.entrant?.history.includes(eventId)) {
			userUpdatePromises.push(
				user.ref.update(
					new FieldPath("entrant", "history"),
					FieldValue.arrayRemove(eventId),
				),
			);
		}

		for (const notificationId of deletedNotificationIds) {
			if (userDoc.entrant?.undismissedNotifications.includes(notificationId)) {
				userUpdatePromises.push(
					user.ref.update(
						new FieldPath("entrant", "undismissedNotifications"),
						FieldValue.arrayRemove(notificationId),
					),
				);
			}
		}
	}

	// await all the updates

	await Promise.all(notificationUpdatePromises);
	await Promise.all(userUpdatePromises);
}

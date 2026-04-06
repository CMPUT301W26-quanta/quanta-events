import { FieldPath, FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const createNotificationInterface = util.standardForm(
	z.object({
		eventId: z.uuid(), // target event id

		title: z.string(), // notification title
		message: z.string(), // notification body

		waited: z.boolean(), // send to waited
		cancelled: z.boolean(), // send to cancelled
		selected: z.boolean(), // send to selected
		final: z.boolean(), // send to final
	}),
);

export async function createNotification(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		createNotificationInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);
	await util.requireRole(userData, "organizer");

	const notificationId = uuidv4();

	logger.info(
		`Creating notification ${notificationId} on event ${data.eventId}.`,
	);

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "The target event does not exist.");
	}

	const eventDoc = event.data()!;

	if (eventDoc.organizer !== userId) {
		throw new HttpsError(
			"permission-denied",
			"Must be the organiser of the event.",
		);
	}

	// create a list of recipient IDs

	const recipients: string[] = [];

	if (data.waited) {
		const list = eventDoc?.waitList ?? [];
		for (const id of list) if (!recipients.includes(id)) recipients.push(id);
	}

	if (data.cancelled) {
		const list = eventDoc?.cancelledList ?? [];
		for (const id of list) if (!recipients.includes(id)) recipients.push(id);
	}

	if (data.selected) {
		const list = eventDoc?.selectedList ?? [];
		for (const id of list) if (!recipients.includes(id)) recipients.push(id);
	}

	if (data.final) {
		const list = eventDoc?.finalList ?? [];
		for (const id of list) if (!recipients.includes(id)) recipients.push(id);
	}

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;
	const notificationRef = notificationCollection.doc(notificationId);

	try {
		await notificationRef.create(
			util.enforceFull<NotificationDocument>({
				eventId: data.eventId,

				targetUsers: recipients,

				title: data.title,
				message: data.message,

				kind: {
					kind: "MESSAGE",
					waited: data.waited,
					cancelled: data.cancelled,
					selected: data.selected,
					final: data.final,
				},
			}),
		);
	} catch (e) {
		logger.error("Failed to send notification because it already exists");
		throw new HttpsError("already-exists", "Notification already exists");
	}

	const notification = await notificationRef.get();
	const notificationDoc = notification.data()!;

	util.sendBatchNotifications(recipients, notificationId, notificationDoc);

	// save the notification to the organizer's sent notifications list

	const userCollection = db.collection("users") as UserDocCollection;
	const userRef = userCollection.doc(userId);

	await userRef.update(
		new FieldPath("organizer", "sentNotifications"),
		FieldValue.arrayUnion(notificationId),
	);

	return { notificationId };
}

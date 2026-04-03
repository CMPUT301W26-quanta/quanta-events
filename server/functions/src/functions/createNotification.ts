import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import {
	CollectionReference,
	FieldValue,
	getFirestore,
} from "firebase-admin/firestore";

const createNotificationInterface = util.standardForm(
	z.object({
		message: z.string(),
		title: z.string(),
		eventId: z.uuid(),
		waited: z.boolean(),
		cancelled: z.boolean(),
		selected: z.boolean(),
		final: z.boolean(),
	}),
);

export async function createNotification(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		createNotificationInterface,
		request,
	);

	const { message, title, eventId, waited, cancelled, selected, final } = data;

	const userData = await util.verifyUser(userId, deviceId);
	await util.requireRole(userData, "organizer");

	const notificationId = uuidv4();

	const db = getFirestore();

	const eventDocuments = db.collection("events") as CollectionReference<
		EventDocument,
		EventDocument
	>;
	const eventDocument = (await eventDocuments.doc(data.eventId).get()).data();

	// Store all the recipients in some collection
	const recipients: string[] = [];
	if (waited) {
		const waitedList = eventDocument?.waitList as string[];
		logger.info("This is the waited list", { waitedList });
		for (const entrantId of waitedList) {
			if (!recipients.includes(entrantId)) {
				recipients.push(entrantId);
			}
		}
	}
	if (cancelled) {
		const cancelledList = eventDocument?.cancelledList as string[];
		logger.info("This is the cancelled list", { cancelledList });
		for (const entrantId of cancelledList) {
			if (!recipients.includes(entrantId)) {
				recipients.push(entrantId);
			}
		}
	}
	if (final) {
		const finalList = eventDocument?.finalList as string[];
		logger.info("This is the final list", { finalList });
		for (const entrantId of finalList) {
			if (!recipients.includes(entrantId)) {
				recipients.push(entrantId);
			}
		}
	}
	if (selected) {
		const selectedList = eventDocument?.selectedList as string[];
		logger.info("This is the selected list", { selectedList });
		for (const entrantId of selectedList) {
			if (!recipients.includes(entrantId)) {
				recipients.push(entrantId);
			}
		}
	}

	try {
		await db
			.collection("notifications")
			.doc(notificationId)
			.create(
				util.enforceFull<NotificationDocument>({
					message,
					title,
					eventId,
					kind: {
						kind: "MESSAGE",
						waited,
						cancelled,
						selected,
						final,
					},
					targetUsers: recipients,
				}),
			);
	} catch (_) {
		throw new HttpsError("already-exists", "Notification already exists");
	}

	util.sendBatchNotifications(recipients, data.title || "", data.message || "");

	// Store the notification in organizer's sent notifications array
	await db
		.collection("users")
		.doc(userId)
		.update({
			"organizer.sentNotifications": FieldValue.arrayUnion(notificationId),
		});

	return { notificationId };
}

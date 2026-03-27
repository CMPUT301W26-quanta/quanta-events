import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import { CollectionReference, getFirestore } from "firebase-admin/firestore";
import { EventDocument } from "../schema";
import { UserDocument } from "../schema";
import { getMessaging, Message } from "firebase-admin/messaging";

const createNotificationInterface = util.standardForm(
	z.object({
		message: z.string().optional(),
		title: z.string().optional(),
		eventId: z.uuid(),
		waited: z.boolean(),
		cancelled: z.boolean(),
		selected: z.boolean(),
	}),
);

export async function createNotification(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		createNotificationInterface,
		request,
	);

	const { message, title, eventId, waited, cancelled, selected } = data;

	const userData = await util.verifyUser(userId, deviceId);
	await util.requireRole(userData, "organizer");

	const notificationId = uuidv4();

	const db = getFirestore();

	try {
		await db.collection("notifications").doc(notificationId).create({
			message,
			title,
			eventId,
			waited,
			cancelled,
			selected,
		});
	} catch (_) {
		throw new HttpsError("already-exists", "Notification already exists");
	}

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
	if (selected) {
		const finalList = eventDocument?.finalList as string[];
		logger.info("This is the final list", { finalList });
		for (const entrantId of finalList) {
			if (!recipients.includes(entrantId)) {
				recipients.push(entrantId);
			}
		}
	}

	// Get the FCM tokens by querying the user objects in the recipient list
	const deviceTokens: string[] = [];
	const userDocuments = db.collection("users") as CollectionReference<
		UserDocument,
		UserDocument
	>;
	for (const entrantId of recipients) {
		const userDocument = (await userDocuments.doc(entrantId).get()).data();
		const token = userDocument?.notifToken;
		const receiveNotifications = userDocument?.entrant?.receiveNotifications;

		if (token != null && receiveNotifications) {
			deviceTokens.push(token);
			logger.info("This is a token", { token });
		}
	}

	console.log("Tokens here", { deviceTokens });

	// Send notification to the recipients
	for (const t of deviceTokens) {
		const finalNotification: Message = {
			token: t,
			data: {
				title: String(data.title),
				body: String(data.message),
			},
			android: {
				priority: "high",
			},
			apns: {
				headers: {
					"apns-priority": "5",
				},
			},
		};
		await getMessaging()
			.send(finalNotification)
			.then((response: string) => {
				logger.info("Message sent succesfully");
			})
			.catch((error: string) => {
				console.log("Message failed to send", error);
			});
	}

	logger.info("Everything worked.", { notificationId });
	return { notificationId };
}

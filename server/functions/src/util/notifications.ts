import { DocumentSnapshot, getFirestore } from "firebase-admin/firestore";
import { getMessaging, Message } from "firebase-admin/messaging";
import { logger } from "firebase-functions";

export async function sendNotification(
	targetId: string,
	title: string,
	body: string,
) {
	const db = getFirestore();
	const targetDoc = (await db
		.collection("users")
		.doc(targetId)
		.get()) as DocumentSnapshot<UserDocument, UserDocument>;

	if (!targetDoc.exists) {
		logger.error(
			`Failed to send notification, user ${targetId} does not exist.`,
		);
		return;
	}

	const target = targetDoc.data() as UserDocument;

	if (target.notifToken === undefined || target.notifToken === null) {
		logger.error(
			`Failed to send notification, user ${targetId} does not have a notifToken assigned.`,
		);
		return;
	}

	const message: Message = {
		token: target.notifToken,
		notification: {
			title,
			body,
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

	// Send only if receiveNotifications is enabled
	if (target.entrant?.receiveNotifications) {
		await getMessaging()
		.send(message)
		.then((_response: string) => {
			logger.info("Message sent succesfully");
		})
		.catch((error: string) => {
			logger.error("Message failed to send", error);
		});
	}
}

export async function sendBatchNotifications(
	targetIds: string[],
	title: string,
	body: string,
) {
	// Send notification to the recipients
	for (const targetId of targetIds) {
		sendNotification(targetId, title, body);
	}
}

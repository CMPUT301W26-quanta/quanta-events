import { FieldPath, FieldValue, getFirestore } from "firebase-admin/firestore";
import { getMessaging, Message } from "firebase-admin/messaging";
import { logger } from "firebase-functions";

async function sendAndroidNotification(
	notifToken: string,
	notificationTitle: string,
	notificationBody: string,
) {
	const message: Message = {
		token: notifToken,

		notification: {
			title: notificationTitle,
			body: notificationBody,
		},

		android: { priority: "high" },
		apns: { headers: { "apns-priority": "5" } },
	};

	await getMessaging()
		.send(message)
		.then((res) => {
			logger.info("Successfully sent a notification");
		})
		.catch((err) => {
			logger.info("Failed to send a notification", err);
		});
}

export async function sendNotification(
	recipientId: string,
	notificationId: string,
	notificationDoc: NotificationDocument,
) {
	const db = getFirestore();

	const userCollection = db.collection("users") as UserDocCollection;
	const userRef = userCollection.doc(recipientId);
	const user = await userRef.get();

	if (!user.exists) {
		logger.error(
			`Failed to send a notification; user ${recipientId} not found.`,
		);
		return;
	}

	const userDoc = user.data()!;

	if (userDoc.notifToken === undefined || userDoc.notifToken === null) {
		logger.error(
			`Failed to send a notification; user ${recipientId} has no notifToken.`,
		);
		return;
	}

	if (userDoc.entrant === undefined || userDoc.entrant === null) {
		logger.error(
			`Failed to send a notification; user ${recipientId} is not an entrant.`,
		);
		return;
	}

	// add the notification to the user's undismissed notifications list,
	// so that it will appear on their home page

	await userRef.update(
		new FieldPath("entrant", "undismissedNotifications"),
		FieldValue.arrayUnion(notificationId),
	);

	// send an android notification only if the recipient has enabled receiving push notifications

	if (userDoc.entrant.receiveNotifications) {
		sendAndroidNotification(
			userDoc.notifToken,
			notificationDoc.title,
			notificationDoc.message,
		);
	}
}

export async function sendBatchNotifications(
	recipientIds: string[],
	notificationId: string,
	notificationDoc: NotificationDocument,
) {
	for (const recipientId of recipientIds)
		sendNotification(recipientId, notificationId, notificationDoc);
}

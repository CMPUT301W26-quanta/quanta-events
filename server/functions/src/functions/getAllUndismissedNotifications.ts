import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import * as util from "../util";

const getAllUndismissedNotificationsInterface = util.standardForm(z.object({}));

export async function getAllUndismissedNotifications(
	request: CallableRequest,
): Promise<ExternalUndismissedNotification[]> {
	const { userId, deviceId } = util.parseInterface(
		getAllUndismissedNotificationsInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);

	const entrant = util.requireRole(userData, "entrant");

	logger.info(`Get all undismissed notifications of user ${userId}`);

	const db = getFirestore();

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;

	const notifications: ExternalUndismissedNotification[] = [];

	for (const undismissedNotificationId of entrant.entrant
		.undismissedNotifications) {
		const notificationRef = notificationCollection.doc(
			undismissedNotificationId,
		);
		const notification = await notificationRef.get();

		if (!notification.exists) {
			logger.error(`Failed to fetch notification ${undismissedNotificationId}`);
			continue; // skip non-existent notifications
		}

		const notificationDoc = notification.data()!;
		notifications.push(
			util.enforceFull<ExternalUndismissedNotification>({
				notificationId: undismissedNotificationId,
				eventId: notificationDoc.eventId,
				title: notificationDoc.title,
				message: notificationDoc.message,
				kind: notificationDoc.kind.kind,
				lotterySelected:
					notificationDoc.kind.kind === "LOTTERY"
						? notificationDoc.kind.selected
						: null,
			}),
		);
	}

	return notifications;
}

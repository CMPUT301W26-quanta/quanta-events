import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import * as util from "../util";

const getAllNotificationsInterface = util.standardForm(
	z.object({
		sentById: z.uuid(),
	}),
);

export async function getAllNotifications(
	request: CallableRequest,
): Promise<ExternalNotification[]> {
	const { userId, deviceId, data } = util.parseInterface(
		getAllNotificationsInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);
	await util.requireRole(userData, "admin");

	logger.info(`Get all notifications sent by ${data.sentById}.`);

	const db = getFirestore();

	const sendersEventsIds = (
		await db.collection("events").where("organizer", "==", data.sentById).get()
	).docs.map((doc) => doc.id);

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;

	const notificationDocs: ExternalNotification[] = [];

	if (sendersEventsIds.length !== 0) {
		const notifications = (await notificationCollection.where("eventId", "in", sendersEventsIds).get()).docs;

		for (const notification of notifications) {
			const notificationDoc = notification.data()!;

			if (notificationDoc.kind.kind !== "MESSAGE")
				continue;

			notificationDocs.push(util.enforceFull<ExternalNotification>({
				title: notificationDoc.title,
				message: notificationDoc.message
			}));
		}
	}

	return notificationDocs;
}

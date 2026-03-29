import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import { NotificationDocument } from "../schema";
import * as util from "../util";

const getAllNotificationsInterface = util.standardForm(
	z.object({
		sentById: z.uuid(),
	}),
);

export async function getAllNotifications(
	request: CallableRequest,
): Promise<NotificationDocument[]> {
	const { userId, deviceId, data } = util.parseInterface(
		getAllNotificationsInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);
	await util.requireRole(userData, "admin");

	const db = getFirestore();

	const sendersEventsIds = (
		await db.collection("events").where("organizer", "==", data.sentById).get()
	).docs.map((doc) => doc.id);

	const notifications =
		sendersEventsIds.length == 0
			? []
			: ((
					await db
						.collection("notifications")
						.where("eventId", "in", sendersEventsIds)
						.get()
				).docs.map((doc) => ({ ...doc.data() })) as NotificationDocument[]);

	logger.info(`Get all notifications sent by ${data.sentById}.`);
	return notifications;
}

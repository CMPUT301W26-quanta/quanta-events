import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const getNotificationInterface = util.standardForm(
	z.object({
		notificationId: z.uuid(),
	}),
);

export async function getNotification(
	request: CallableRequest,
): Promise<NotificationDocument> {
	const { userId, deviceId, data } = util.parseInterface(
		getNotificationInterface,
		request,
	);

	const { notificationId } = data;

	await util.verifyUser(userId, deviceId);

	const db = getFirestore();

	const notificationDoc = await db
		.collection("notifications")
		.doc(notificationId)
		.get();

	if (!notificationDoc.exists) {
		throw new HttpsError("not-found", "Notification not found");
	}

	logger.info("Notification found", { notificationId });
	return notificationDoc.data() as NotificationDocument;
}

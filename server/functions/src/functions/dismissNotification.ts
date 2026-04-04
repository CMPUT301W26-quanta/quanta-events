import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { logger } from "firebase-functions";
import { FieldPath, FieldValue, getFirestore } from "firebase-admin/firestore";

const dismissNotificationInterface = util.standardForm(
	z.object({ notificationId: z.uuid() }),
);

export async function dismissNotification(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		dismissNotificationInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);

	logger.info(`Dismissing notification ${data.notificationId}.`);

	const db = getFirestore();

	if (userData.entrant === undefined || userData.entrant === null) {
		throw new HttpsError("failed-precondition", "User is not an entrant.");
	}

	if (
		!userData.entrant.undismissedNotifications.includes(data.notificationId)
	) {
		throw new HttpsError(
			"not-found",
			"Notification not found in undismissed notifications.",
		);
	}

	const userCollection = db.collection("users") as UserDocCollection;
	const userRef = userCollection.doc(userId);

	await userRef.update(
		new FieldPath("entrant", "undismissedNotifications"),
		FieldValue.arrayRemove(data.notificationId),
	);

	return {};
}

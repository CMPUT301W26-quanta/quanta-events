import { FieldPath, FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const cancelSelectedInterface = util.standardForm(
	z.object({ eventId: z.uuid() }),
);

export async function cancelSelected(request: CallableRequest): Promise<{}> {
	const { userId, deviceId, data } = util.parseInterface(
		cancelSelectedInterface,
		request,
	);
	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "organizer");

	logger.info(`Cancelling selected entrants on event ${data.eventId}.`);

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event does not exist");
	}

	const eventDoc = event.data()!;

	if (userId !== eventDoc.organizer) {
		throw new HttpsError(
			"failed-precondition",
			"You must be the organizer of the event.",
		);
	}

	// get notifications for this event

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;
	const notifications = await notificationCollection.get();

	const lotterySelectedNotificationIds: string[] = [];

	for (const notification of notifications.docs) {
		const notificationDoc = notification.data()!;

		if (
			notificationDoc.kind.kind === "LOTTERY" &&
			notificationDoc.kind.selected
		) {
			lotterySelectedNotificationIds.push(notification.id);
		}
	}

	// remove the selected by lottery notification from the users
	// in the selectedList

	const userCollection = db.collection("users") as UserDocCollection;

	for (const userId of eventDoc.selectedList) {
		// remove selected for lottery notifications from the users being
		// removed from the selectedList

		const userRef = userCollection.doc(userId);
		const user = await userRef.get();

		if (!user.exists) {
			logger.error(
				`Failed to remove lottery selected notification for user ${userId}. User does not exist.`,
			);
			continue;
		}

		for (const notificationId of lotterySelectedNotificationIds) {
			await userRef.update(
				new FieldPath("entrant", "undismissedNotifications"),
				FieldValue.arrayRemove(notificationId),
			);
		}

		// add the user to the cancelledList

		await eventRef.update({ cancelledList: FieldValue.arrayUnion(userId) });
	}

	// reset the selectedList

	const numberOfSelected = eventDoc.selectedList.length;
	await eventRef.update(
		util.enforcePartial<EventDocument>({ selectedList: [] }),
	);

	// add new users from the wait list to the selected list,
	// while there are any

	for (let i = 0; i < numberOfSelected; i++) {
		if (eventDoc.rejectedList.length === 0) break;

		await util.rejectedToSelected(data.eventId);
	}

	return {};
}

// reject an invite to join an event
// after being selected by the lottery

import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const inviteRejectInterface = util.standardForm(
	z.object({ eventId: z.uuid() }),
);

export async function inviteReject(request: CallableRequest): Promise<{}> {
	const { userId, deviceId, data } = util.parseInterface(
		inviteRejectInterface,
		request,
	);
	await util.verifyUser(userId, deviceId);

	logger.info(`Rejecting ${userId}'s invitation to ${data.eventId}.`);

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "The event does not exist.");
	}

	const eventDoc = event.data()!;

	if (!eventDoc.selectedList.includes(userId)) {
		throw new HttpsError(
			"failed-precondition",
			"User is not on the selected list.",
		);
	}

	// remove the user from the selected list
	// (the list of ppl selected by the lotto to be able to join)

	await eventRef.update({
		selectedList: FieldValue.arrayRemove(userId),
	});

	// add the user to the cancelled list
	// (the list of ppl who rejected their invite)

	await eventRef.update({
		cancelledList: FieldValue.arrayUnion(userId),
	});

	// move a user from the rejected list to the selected list
	// (the list of ppl not selected by the lotto, to the list of ppl selected by it)

	if (eventDoc.rejectedList.length > 0) {
		// the rejectedList was already randomized when the lottery was
		// drawn in drawLottery(), so we'll just pick the last user in it

		const userId = eventDoc.rejectedList.at(-1)!;

		await eventRef.update({
			selectedList: FieldValue.arrayUnion(userId),
		});

		await eventRef.update({
			rejectedList: FieldValue.arrayRemove(userId),
		});

		const notificationId = uuidv4();

		const notificationCollection = db.collection(
			"notifications",
		) as NotificationDocCollection;
		const notificationRef = notificationCollection.doc(notificationId);

		notificationRef.create(
			util.enforceFull<NotificationDocument>({
				eventId: data.eventId,

				targetUsers: [userId],

				title: eventDoc.eventName,
				message: "You've been reconsidered for an event!",

				kind: {
					kind: "LOTTERY",
					selected: true,
				},
			}),
		);

		const notification = await notificationRef.get();
		const notificationDoc = notification.data()!;

		await util.sendNotification(userId, notificationId, notificationDoc);
	}

	return {};
}

// reject an invite to join an event
// after being selected by the lottery

import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
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
		await util.rejectedToSelected(data.eventId);
	}

	return {};
}

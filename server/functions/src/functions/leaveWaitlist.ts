import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const leaveWaitlistInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
	}),
);

export async function leaveWaitlist(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		leaveWaitlistInterface,
		request,
	);

	const { eventId } = data;

	const userData = await util.verifyUser(userId, deviceId);

	await util.requireRole(userData, "entrant");

	const db = getFirestore();

	const eventDoc = await db.collection("events").doc(eventId).get();

	if (!eventDoc.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const event = eventDoc.data() as EventDocument;

	if (!event.waitList?.includes(userId)) {
		throw new HttpsError(
			"failed-precondition",
			"User is not on the waitlist for this event",
		);
	}

	const now = new Date();
	const registrationEnd = event.registrationEndTime.toDate();

	if (now > registrationEnd) {
		throw new HttpsError(
			"failed-precondition",
			"Registration has closed — cannot leave waitlist",
		);
	}

	await db
		.collection("events")
		.doc(eventId)
		.update({
			waitList: FieldValue.arrayRemove(userId),
		});

	await db
		.collection("users")
		.doc(userId)
		.update({
			"entrant.enteredEvents": FieldValue.arrayRemove(eventId),
		});

	logger.info("User left waitlist", { userId, eventId });
	return {};
}

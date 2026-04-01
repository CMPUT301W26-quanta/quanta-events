import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const deleteEventInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
	}),
);

export async function deleteEvent(request: CallableRequest) {
	const payload = util.parseInterface(deleteEventInterface, request);

	const userData = await util.verifyUser(payload.userId, payload.deviceId);

	logger.info(`Deleting event ${payload.data.eventId}.`);

	const db = getFirestore();

	const eventRef = db.collection("events").doc(payload.data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const eventDoc = event.data() as EventDocument;

	if (eventDoc.organizer !== payload.userId) {
		util.requireRole(userData, "admin");
	}

	if (eventDoc.imageId !== null) {
		await util.removeImage(eventDoc.imageId);
	}

	await eventRef.delete();

	return {};
}

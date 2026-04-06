import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const deleteEventInterface = util.standardForm(z.object({ eventId: z.uuid() }));

export async function deleteEvent(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		deleteEventInterface,
		request,
	);

	// note: organizers can not delete their own events
	// (per the user stories, only admins can remove/delete events)
	// that's why we require the admin role; not organizer

	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "admin");

	logger.info(`Deleting event ${data.eventId}.`);

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	await util.deleteEvent(data.eventId);

	return {};
}

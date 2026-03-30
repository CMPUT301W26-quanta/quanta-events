import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const getWaitlistInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
	}),
);

export async function getWaitlist(
	request: CallableRequest,
): Promise<{ waitList: string[] }> {
	const { userId, deviceId, data } = util.parseInterface(
		getWaitlistInterface,
		request,
	);

	const { eventId } = data;

	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "organizer");
	const db = getFirestore();

	const eventDoc = await db.collection("events").doc(eventId).get();

	if (!eventDoc.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const { organizer, waitList = [] } = eventDoc.data() as EventDocument;

	if (organizer !== userId) {
		throw new HttpsError(
			"permission-denied",
			"User is not the organizer of this event",
		);
	}

	logger.info("Got waitlist", { eventId, count: waitList.length });

	return { waitList };
}

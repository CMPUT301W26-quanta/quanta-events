import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const getWaitlistCountInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
	}),
);

export async function getWaitlistCount(
	request: CallableRequest,
): Promise<{ count: number }> {
	const { userId, deviceId, data } = util.parseInterface(
		getWaitlistCountInterface,
		request,
	);

	const { eventId } = data;

	await util.verifyUser(userId, deviceId);

	const db = getFirestore();

	const eventDoc = await db.collection("events").doc(eventId).get();

	if (!eventDoc.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const event = eventDoc.data() as EventDocument;

	const count = event.waitList?.length ?? 0;

	logger.info("Got waitlist count", { eventId, count });
	return { count };
}

import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
// import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const coInviteRejectInterface = util.standardForm(
	z.object({ eventId: z.uuid() }),
);

export async function coInviteReject(request: CallableRequest): Promise<{}> {
	const { userId, deviceId, data } = util.parseInterface(
		coInviteRejectInterface,
		request,
	);
	await util.verifyUser(userId, deviceId);

	logger.info(`Rejecting ${userId}'s invitation to co-organize ${data.eventId}.`);

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "The event does not exist.");
	}

	// const eventDoc = event.data()!;

	logger.info("Reject something");

	return {};
}

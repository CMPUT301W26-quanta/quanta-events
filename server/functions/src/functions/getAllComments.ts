import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import { ExternalComment } from "../schema";
import * as util from "../util";

const getAllCommentsInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
	}),
);

export async function getAllComments(
	request: CallableRequest,
): Promise<ExternalComment[]> {
	const payload = util.parseInterface(getAllCommentsInterface, request);
	await util.verifyUser(payload.userId, payload.deviceId);

	logger.info(`Retrieving all comments from event ${payload.data.eventId}.`);

	const db = getFirestore();

	const eventRef = db.collection("events").doc(payload.data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	return (await eventRef.collection("comments").get()).docs.map(
		util.commentDocToExternalComment,
	);
}

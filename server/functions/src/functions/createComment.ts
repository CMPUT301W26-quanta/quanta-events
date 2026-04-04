import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const createCommentInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
		message: z.string(),
		postTime: z.string()
	}),
);

export async function createComment(request: CallableRequest): Promise<string> {
	const payload = util.parseInterface(createCommentInterface, request);
	await util.verifyUser(payload.userId, payload.deviceId);

	const commentId = uuidv4();

	logger.info(`Creating comment ${commentId} on event ${payload.data.eventId}`);

	const db = getFirestore();

	const eventRef = db.collection("events").doc(payload.data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const commentRef = await eventRef.collection("comments").doc(commentId);

	commentRef.create({
		senderId: payload.userId,
		message: payload.data.message,
		postTime: payload.data.postTime
	});

	return commentId;
}

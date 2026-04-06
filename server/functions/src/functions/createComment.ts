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
		postTime: z.string(),
	}),
);

export async function createComment(request: CallableRequest): Promise<string> {
	const { userId, deviceId, data } = util.parseInterface(
		createCommentInterface,
		request,
	);
	await util.verifyUser(userId, deviceId);

	const commentId = uuidv4();

	logger.info(`Creating comment ${commentId} on event ${data.eventId}`);

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const commentCollection = eventRef.collection(
		"comments",
	) as CommentDocCollection;
	const commentRef = commentCollection.doc(commentId);

	commentRef.create(
		util.enforceFull<CommentDocument>({
			senderId: userId,
			message: data.message,
			postTime: data.postTime,
		}),
	);

	return commentId;
}

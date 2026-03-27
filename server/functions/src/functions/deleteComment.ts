import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { CommentDocument, EventDocument } from "../schema";

const deleteCommentInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
		commentId: z.uuid(),
	}),
);

export async function deleteComment(request: CallableRequest): Promise<{}> {
	const payload = util.parseInterface(deleteCommentInterface, request);
	const userData = await util.verifyUser(payload.userId, payload.deviceId);

	logger.info(
		`Deleting comment ${payload.data.commentId} from event ${payload.data.eventId}.`,
	);

	const db = getFirestore();

	const eventRef = db.collection("events").doc(payload.data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const eventDoc = event.data() as EventDocument;

	const commentRef = eventRef
		.collection("comments")
		.doc(payload.data.commentId);

	const comment = await commentRef.get();
	const commentDoc = comment.data() as CommentDocument;

	if (!comment.exists) {
		throw new HttpsError("not-found", "Comment not found");
	}

	if (![commentDoc.senderId, eventDoc.organizer].includes(payload.userId)) {
		util.requireRole(userData, "admin");
	}

	await commentRef.delete();

	return {};
}

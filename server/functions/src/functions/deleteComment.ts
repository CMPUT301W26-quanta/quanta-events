import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const deleteCommentInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
		commentId: z.uuid(),
	}),
);

export async function deleteComment(request: CallableRequest): Promise<{}> {
	const payload = util.parseInterface(deleteCommentInterface, request);
	await util.verifyUser(payload.userId, payload.deviceId);

	logger.info(
		`Deleting comment ${payload.data.commentId} from event ${payload.data.eventId}.`,
	);

	const db = getFirestore();

	const eventRef = db.collection("events").doc(payload.data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const commentRef = eventRef
		.collection("comments")
		.doc(payload.data.commentId);

	const comment = await commentRef.get();

	if (!comment.exists) {
		throw new HttpsError("not-found", "Comment not found");
	}

	commentRef.delete();

	return {};
}

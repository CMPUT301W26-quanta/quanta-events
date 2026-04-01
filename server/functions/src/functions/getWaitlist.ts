import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

type ListType = "waitList" | "cancelledList" | "finalList" | "rejectedList" | "selectedList";

const getWaitlistInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
    listType: z.enum(["waitList", "cancelledList", "finalList", "rejectedList", "selectedList"]),
	}),
);

export async function getWaitlist(
	request: CallableRequest,
): Promise<{ listRequested: string[]; listType: ListType }> {
	const { userId, deviceId, data } = util.parseInterface(
		getWaitlistInterface,
		request,
	);

	const { eventId, listType } = data;

	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "organizer");
	const db = getFirestore();

	const eventDoc = await db.collection("events").doc(eventId).get();

	if (!eventDoc.exists) {
		throw new HttpsError("not-found", "Event not found");
	}

	const { organizer, [listType]: listRequested = [] } = eventDoc.data() as EventDocument;

	if (organizer !== userId) {
		throw new HttpsError(
			"permission-denied",
			"User is not the organizer of this event",
		);
	}

	logger.info("Got requested list", { eventId, listType, count: listRequested.length });

	return { listRequested, listType };
}

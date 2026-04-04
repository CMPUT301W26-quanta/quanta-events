import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

type ListType = "waitList" | "cancelledList" | "finalList" | "rejectedList" | "selectedList";

interface UserInfo {
	userId: string;
	name: string;
	email: string;
	phone: string;
}

const getWaitlistInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
		listType: z.enum(["waitList", "cancelledList", "finalList", "rejectedList", "selectedList"]),
	}),
);

export async function getWaitlist(
	request: CallableRequest,
): Promise<{ users: UserInfo[]; listType: ListType }> {
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

	const { organizer, [listType]: userIds = [] } = eventDoc.data() as EventDocument;
	if (organizer !== userId) {
		throw new HttpsError(
			"permission-denied",
			"User is not the organizer of this event",
		);
	}

	const users = await fetchUserDetails(db, userIds);

	logger.info("Got requested list", { eventId, listType, count: users.length });
	return { users, listType };
}

async function fetchUserDetails(
	db: FirebaseFirestore.Firestore,
	userIds: string[],
): Promise<UserInfo[]> {
	if (userIds.length === 0) return [];

	const userRefs = userIds.map((id) => db.collection("users").doc(id));
	const userDocs = await db.getAll(...userRefs);

	return userDocs
		.filter((doc) => doc.exists)
		.map((doc) => {
			const { name, email, phone } = doc.data() as { name: string; email: string; phone: string };
			return { userId: doc.id, name, email, phone };
		});
}

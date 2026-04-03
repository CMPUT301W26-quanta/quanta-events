import * as z from "zod";
import * as util from "../util";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { CollectionReference, getFirestore } from "firebase-admin/firestore";
import { v4 as uuidv4 } from "uuid";
import { logger } from "firebase-functions";

const createInvitationInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
		invitee: z.uuid(),
	}),
);

export async function createInvitation(request: CallableRequest) {
	const {
		userId,
		deviceId,
		data: { eventId, invitee },
	} = util.parseInterface(createInvitationInterface, request);
	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "organizer");

	const db = getFirestore();

	const eventDocuments = db.collection("events") as CollectionReference<
		EventDocument,
		EventDocument
	>;
	const eventDocument = (await eventDocuments.doc(eventId).get()).data()!;

	const inviteNotif: NotificationDocument = {
		eventId,
		title: eventDocument.eventName,
		message: "You were invited to a private event!",
		targetUsers: [invitee],
		kind: {
			kind: "INVITE",
		},
	};

	try {
		await db.collection("notifications").doc(uuidv4()).create(inviteNotif);
	} catch (e) {
		logger.error("Notification cannot be created, as it already exists", e);
		throw new HttpsError("already-exists", "Notification already exists");
	}

	util.sendNotification(
		inviteNotif.targetUsers[0],
		inviteNotif.title,
		inviteNotif.message,
	);
}

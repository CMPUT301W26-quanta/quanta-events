import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const createInvitationInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
		invitee: z.uuid(),
	}),
);

export async function createInvitation(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		createInvitationInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "organizer");

	const notificationId = uuidv4();

	logger.info(
		`Creating invite ${notificationId} on event ${data.eventId} to user ${data.invitee}.`,
	);

	const db = getFirestore();

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);
	const event = await eventRef.get();

	if (!event.exists) {
		throw new HttpsError("not-found", "The target event does not exist");
	}

	const eventDoc = event.data()!;

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;
	const notificationRef = notificationCollection.doc(notificationId);

	try {
		await notificationRef.create(
			util.enforceFull<NotificationDocument>({
				eventId: data.eventId,

				targetUsers: [data.invitee],

				title: eventDoc.eventName,
				message: "You were invited to a private event!",

				kind: { kind: "INVITE" },
			}),
		);
	} catch (e) {
		logger.error(
			"Failed to send invite notification because it already exists.",
			e,
		);
		throw new HttpsError(
			"already-exists",
			"Invite notification already exists",
		);
	}

	const notification = await notificationRef.get();
	const notificationDoc = notification.data()!;

	await util.sendNotification(data.invitee, notificationId, notificationDoc);

	return {};
}

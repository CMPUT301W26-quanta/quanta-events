import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import {
	DocumentReference,
	FieldValue,
	getFirestore,
	Timestamp,
} from "firebase-admin/firestore";

const drawLotteryInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
	}),
);

export async function drawLottery(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		drawLotteryInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);

	// PRE: Require organizer
	util.requireRole(userData, "organizer");

	const { eventId } = data;

	const db = getFirestore();
	const eventRef = db.collection("events").doc(eventId) as DocumentReference<
		EventDocument,
		EventDocument
	>;
	let selectedUsers: string[];
	let rejectedUsers: string[];
	let eventName: string;
	try {
		const result = await db.runTransaction(async (transaction) => {
			const eventDoc = await transaction.get(eventRef);

			if (!eventDoc.exists) {
				throw new HttpsError("not-found", "The event does not exist");
			}

			const event = eventDoc.data() as EventDocument;

			// PRE: Require event owned by organizer
			if (event.organizer !== userId) {
				throw new HttpsError(
					"failed-precondition",
					"Event is not owned by this user",
				);
			}

			// PRE: Event has not yet had a lottery drawn
			if (event.drawn || false) {
				throw new HttpsError(
					"failed-precondition",
					"Event has already drawn lottery",
				);
			}

			// PRE: Passed its registration end date
			const now = Timestamp.now();
			if (event.registrationEndTime > now) {
				throw new HttpsError(
					"failed-precondition",
					"Event is still open for registration",
				);
			}

			// POST: Lottery marked as being drawn
			transaction.update(
				eventRef,
				util.enforcePartial<EventDocument>({ drawn: true }),
			);

			const waitlist = event.waitList;

			// Randomize order
			for (let i = waitlist.length - 1; i > 0; i--) {
				const j = Math.floor(Math.random() * (i + 1));
				[waitlist[i], waitlist[j]] = [waitlist[j], waitlist[i]];
			}

			// Slice winners
			const selected = waitlist.slice(0, event.eventCapacity);
			const rejected = waitlist.slice(event.eventCapacity);

			// POST: Users are selected and copied to the selected list, all other users are moved to the rejected list
			transaction.update(
				eventRef,
				util.enforcePartial<EventDocument>({
					selectedList: selected,
					rejectedList: rejected,
				}),
			);

			return {
				selectedUsers: selected,
				rejectedUsers: rejected,
				eventName: event.eventName,
			};
		});

		selectedUsers = result.selectedUsers;
		rejectedUsers = result.rejectedUsers;
		eventName = result.eventName;

		logger.info("Successfully ran drawLottery transaction");
	} catch (err) {
		logger.error("Failed to run drawLottery transaction", err);
		if (err instanceof HttpsError) {
			throw err;
		} else {
			throw new HttpsError("internal", "Transaction failure");
		}
	}

	for (const user of selectedUsers.concat(rejectedUsers)) {
		try {
			const userRef = db.collection("users").doc(user) as DocumentReference<
				UserDocument,
				UserDocument
			>;

			const entrantVal = (await userRef.get()).get("entrant");

			if (entrantVal === undefined || entrantVal === null) {
				logger.error(
					`Failed to update history for user ${user}, continuing loop`,
				);
				continue;
			}

			// POST: All users on waitlist have this event added to the history
			userRef.update("entrant.history", FieldValue.arrayUnion(eventId));
			// POST: All users on waitlist have this event removed from enteredEvents
			userRef.update("entrant.enteredEvents", FieldValue.arrayRemove(eventId));
		} catch (e) {
			logger.error(`Failed to update or notify user ${user}`, e);
		}
	}

	const selectedNotif: NotificationDocument = {
		message: "You were selected for an event!",
		title: eventName,
		eventId,
		kind: {
			kind: "LOTTERY",
			selected: true,
		},
		targetUsers: selectedUsers,
	};
	const rejectedNotif: NotificationDocument = {
		message: "You were not selected for an event.",
		title: eventName,
		eventId,
		kind: {
			kind: "LOTTERY",
			selected: false,
		},
		targetUsers: rejectedUsers,
	};

	util.sendBatchNotifications(
		selectedNotif.targetUsers,
		selectedNotif.title,
		selectedNotif.message,
	);
	util.sendBatchNotifications(
		rejectedNotif.targetUsers,
		rejectedNotif.title,
		rejectedNotif.message,
	);

	try {
		await db.collection("notifications").doc(uuidv4()).create(selectedNotif);
		await db.collection("notifications").doc(uuidv4()).create(rejectedNotif);
	} catch (e) {
		logger.error("Notification cannot be created, as it already exists", e);
		throw new HttpsError("already-exists", "Notification already exists");
	}
}

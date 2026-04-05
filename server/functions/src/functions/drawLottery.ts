import { FieldValue, getFirestore, Timestamp } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const drawLotteryInterface = util.standardForm(
	z.object({ eventId: z.uuid() }),
);

export async function drawLottery(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		drawLotteryInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "organizer");

	const db = getFirestore();

	logger.info(`Drawing lottery for ${data.eventId}.`);

	const eventCollection = db.collection("events") as EventDocCollection;
	const eventRef = eventCollection.doc(data.eventId);

	// create separate lists of selected and rejected users

	let selectedUsers: string[];
	let rejectedUsers: string[];

	let eventName: string;

	try {
		const result = await db.runTransaction(async (transaction) => {
			const event = await transaction.get(eventRef);

			if (!event.exists) {
				throw new HttpsError("not-found", "The target event does not exist");
			}

			const eventDoc = event.data()!;

			// PRE: Require event owned by organizer
			if (eventDoc.organizer !== userId) {
				throw new HttpsError(
					"failed-precondition",
					"Event is not owned by this user.",
				);
			}

			// PRE: Event has not yet had a lottery drawn
			if (eventDoc.drawn ?? false) {
				throw new HttpsError(
					"failed-precondition",
					"Event has already drawn lottery.",
				);
			}

			// PRE: Passed its registration end date
			const now = Timestamp.now();
			if (eventDoc.registrationEndTime > now) {
				throw new HttpsError(
					"failed-precondition",
					"Event is still open for registration.",
				);
			}

			// POST: Lottery marked as being drawn
			transaction.update(
				eventRef,
				util.enforcePartial<EventDocument>({ drawn: true }),
			);

			const waitlist = eventDoc.waitList;

			// Randomize order
			for (let i = waitlist.length - 1; i > 0; i--) {
				const j = Math.floor(Math.random() * (i + 1));
				[waitlist[i], waitlist[j]] = [waitlist[j], waitlist[i]];
			}

			// Slice winners
			const selected = waitlist.slice(0, eventDoc.eventCapacity);
			const rejected = waitlist.slice(eventDoc.eventCapacity);

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
				eventName: eventDoc.eventName,
			};
		});

		selectedUsers = result.selectedUsers;
		rejectedUsers = result.rejectedUsers;
		eventName = result.eventName;

		logger.info("Successfully ran drawLottery transaction.");
	} catch (e) {
		logger.error("Failed to run drawLottery transaction.", e);
		if (e instanceof HttpsError) {
			throw e;
		} else {
			throw new HttpsError("internal", "Transaction failure");
		}
	}

	for (const userId of selectedUsers.concat(rejectedUsers)) {
		try {
			const userCollection = db.collection("users") as UserDocCollection;
			const userRef = userCollection.doc(userId);
			const user = await userRef.get();

			const entrantMap = user.get("entrant");

			if (entrantMap === undefined || entrantMap === null) {
				logger.error(
					`Failed to update history for user ${userId}, continuing loop.`,
				);
				continue;
			}

			// POST: All users on waitlist have this event added to the history
			userRef.update("entrant.history", FieldValue.arrayUnion(data.eventId));
			// POST: All users on waitlist have this event removed from enteredEvents
			userRef.update(
				"entrant.enteredEvents",
				FieldValue.arrayRemove(data.eventId),
			);
		} catch (e) {
			logger.error(`Failed to update or notify user ${userId}.`, e);
		}
	}

	const selectedNotif = util.enforceFull<NotificationDocument>({
		eventId: data.eventId,

		targetUsers: selectedUsers,

		title: eventName,
		message: "You were selected for an event!",

		kind: {
			kind: "LOTTERY",
			selected: true,
		},
	});

	const rejectedNotif = util.enforceFull<NotificationDocument>({
		eventId: data.eventId,

		targetUsers: rejectedUsers,

		title: eventName,
		message: "You were not selected for an event.",

		kind: {
			kind: "LOTTERY",
			selected: false,
		},
	});

	const selectedNotificationId = uuidv4();
	const rejectedNotificationId = uuidv4();

	logger.info(
		`Creating selected notification ${selectedNotificationId} on event ${data.eventId}`,
	);
	logger.info(
		`Creating rejected notification ${rejectedNotificationId} on event ${data.eventId}`,
	);

	const notificationCollection = db.collection(
		"notifications",
	) as NotificationDocCollection;
	const selectedNotificationRef = notificationCollection.doc(
		selectedNotificationId,
	);
	const rejectedNotificationRef = notificationCollection.doc(
		rejectedNotificationId,
	);

	try {
		await selectedNotificationRef.create(selectedNotif);
		await rejectedNotificationRef.create(rejectedNotif);
	} catch (e) {
		logger.error(
			"Selected/rejected notification cannot be created, as it already exists.",
			e,
		);
		throw new HttpsError(
			"already-exists",
			"Selected/rejected notification already exists.",
		);
	}

	const selectedNotification = await selectedNotificationRef.get();
	const rejectedNotification = await rejectedNotificationRef.get();

	const selectedNotificationDoc = selectedNotification.data()!;
	const rejectedNotificationDoc = rejectedNotification.data()!;

	await util.sendBatchNotifications(
		selectedUsers,
		selectedNotificationId,
		selectedNotificationDoc,
	);

	await util.sendBatchNotifications(
		rejectedUsers,
		rejectedNotificationId,
		rejectedNotificationDoc,
	);

	return {};
}

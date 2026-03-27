import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import {
	DocumentReference,
	FieldValue,
	getFirestore,
	Timestamp,
} from "firebase-admin/firestore";

const drawLotteryInterface = util.standardForm(
	z.object({
		eventId: z.uuid(),
	})
);

export async function drawLottery(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		drawLotteryInterface,
		request
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
	try {
		const { affectedUsers, eventName } = await db.runTransaction(
			async (transaction) => {
				const eventDoc = await transaction.get(eventRef);

				if (!eventDoc.exists) {
					throw new HttpsError("not-found", "The event does not exist");
				}

				const event = eventDoc.data() as EventDocument;

				// PRE: Require event owned by organizer
				if (event.organizer !== userId) {
					throw new HttpsError(
						"failed-precondition",
						"Event is not owned by this user"
					);
				}

				// PRE: Event has not yet had a lottery drawn
				if (event.drawn || false) {
					throw new HttpsError(
						"failed-precondition",
						"Event has already drawn lottery"
					);
				}

				// PRE: Passed its registration end date
				const now = Timestamp.now();
				if (event.registrationEndTime > now) {
					throw new HttpsError(
						"failed-precondition",
						"Event is still open for registration"
					);
				}

				// POST: Lottery marked as being drawn
				transaction.update(
					eventRef,
					util.enforcePartial<EventDocument>({ drawn: true })
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
					})
				);

				return {
					affectedUsers: selected
						.map((val) => ({ user: val, selected: true }))
						.concat(rejected.map((val) => ({ user: val, selected: false }))),
					eventName: event.eventName,
				};
			}
		);

		logger.info("Successfully ran drawLottery transaction");

		for (const { user, selected } of affectedUsers) {
			// POST: All users on waitlist have this event added to the history
			const userRef = db.collection("users").doc(user) as DocumentReference<
				UserDocument,
				UserDocument
			>;

			const entrantVal = (await userRef.get()).get("entrant");

			if (entrantVal === undefined || entrantVal === null) {
				logger.error(
					`Failed to update history for user ${user}, continuing loop`
				);
				continue;
			}

			userRef.update("entrant.history", FieldValue.arrayUnion([eventId]));

			// POST: All users on waitlist receive a notification
			const messageBody = selected
				? "You were selected for an event!"
				: "You were not selected for an event.";

			util.sendNotification(user, eventName, messageBody);
		}
	} catch (err) {
		logger.error("Failed to run drawLottery transaction", err);
		if (err instanceof HttpsError) {
			throw err;
		} else {
			throw new HttpsError("internal", "Transaction failure");
		}
	}
}

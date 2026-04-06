import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const updateRolesInterface = util.standardForm(
	z.object({
		targetUserId: z.uuid(),
		isEntrant: z.boolean(),
		isOrganizer: z.boolean(),
		isAdmin: z.boolean(),
	}),
);

export async function updateRoles(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		updateRolesInterface,
		request,
	);

	const userData = await util.verifyUser(userId, deviceId);
	util.requireRole(userData, "admin");

	logger.info(`Updating roles for user ${data.targetUserId}.`);

	const db = getFirestore();

	const userCollection = db.collection("users") as UserDocCollection;
	const userRef = userCollection.doc(data.targetUserId);
	const user = await userRef.get();

	if (!user.exists) {
		throw new HttpsError("not-found", "The target user does not exist.");
	}

	const userDoc = user.data()!;

	// Check if the user currently has these roles
	let currentEntrant = userDoc?.entrant;
	let currentOrganizer = userDoc?.organizer;
	let currentAdmin = userDoc?.admin;

	const eventCollection = db.collection("events") as EventDocCollection;
	const events = await eventCollection.get();

	// Granted entrant permission
	if (data.isEntrant && currentEntrant === null) {
		userDoc.entrant = util.enforceFull<EntrantMap>({
			enteredEvents: [],
			history: [],
			undismissedNotifications: [],
			receiveNotifications: true,
		});
	}

	// Banned from being entrant
	else if (!data.isEntrant && currentEntrant !== null) {
		const entrantUpdatePromises = events.docs.map(async (event) => {
			const eventDoc = event.data()!;

			const isInSelectedList = eventDoc.selectedList?.includes(
				data.targetUserId,
			);

			const isInAnyList =
				eventDoc.waitList?.includes(data.targetUserId) ||
				eventDoc.finalList?.includes(data.targetUserId) ||
				eventDoc.cancelledList?.includes(data.targetUserId) ||
				eventDoc.rejectedList?.includes(data.targetUserId) ||
				isInSelectedList;

			if (!isInAnyList) return;

			if (isInSelectedList && eventDoc.rejectedList.length > 0) {
				await util.rejectedToSelected(event.id);
			}

			return event.ref.update({
				waitList: FieldValue.arrayRemove(data.targetUserId),
				finalList: FieldValue.arrayRemove(data.targetUserId),
				cancelledList: FieldValue.arrayRemove(data.targetUserId),
				rejectedList: FieldValue.arrayRemove(data.targetUserId),
				selectedList: FieldValue.arrayRemove(data.targetUserId),
			});
		});

		await Promise.all(entrantUpdatePromises);

		userDoc.entrant = null;
	}

	// Granted organizer permission
	if (data.isOrganizer && currentOrganizer === null) {
		userDoc.organizer = util.enforceFull<OrganizerMap>({
			createdEvents: [],
			sentNotifications: [],
		});
	}

	// Banned from being an organizer
	else if (!data.isOrganizer && currentOrganizer !== null) {
		const organizerUpdatePromises = events.docs.map(async (event) => {
			const eventDoc = event.data()!;

			if (eventDoc.organizer === data.targetUserId) {
				return util.deleteEvent(event.id);
			}

			return;
		});

		await Promise.all(organizerUpdatePromises);

		userDoc.organizer = null;
	}

	// Granted admin permission
	if (data.isAdmin && currentAdmin === null) {
		userDoc.admin = {};
	}

	// Banned from being an admin
	else if (!data.isAdmin && currentAdmin !== null) {
		userDoc.admin = null;
	}

	currentEntrant = userDoc.entrant;
	currentOrganizer = userDoc.organizer;
	currentAdmin = userDoc.admin;

	const updates: Record<string, any> = {
		entrant: currentEntrant,
		organizer: currentOrganizer,
		admin: currentAdmin,
	};

	await db.collection("users").doc(data.targetUserId).update(updates);
}

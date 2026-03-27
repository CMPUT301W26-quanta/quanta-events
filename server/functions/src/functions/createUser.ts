import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const createUserInterface = z.object({
	deviceId: z.uuid(),
	name: z.string().nullable(),
	email: z.email().nullable(),
	phone: z.string().nullable(),
	receiveNotifications: z.boolean().nullable(),
	isEntrant: z.boolean().nullable(),
	isOrganizer: z.boolean().nullable(),
	isAdmin: z.boolean().nullable(),
});

export async function createUser(request: CallableRequest) {
	const {
		deviceId,
		name,
		email,
		phone,
		receiveNotifications,
		isEntrant,
		isOrganizer,
		isAdmin,
	} = util.parseInterface(createUserInterface, request);

	const userId = uuidv4();

	const db = getFirestore();
	try {
		await db
			.collection("users")
			.doc(userId)
			.create({
				deviceId,
				name: name,
				email: email,
				phone: phone,
				entrant: isEntrant
					? {
							enteredEvents: [],
							history: [],
							receiveNotifications: receiveNotifications ?? false,
						}
					: null,
				organizer: isOrganizer
					? { createdEvents: [], sentNotifications: [] }
					: null,
				admin: isAdmin ? {} : null,
				notifToken: null,
			});
	} catch (_) {
		throw new HttpsError("already-exists", "User already exists");
	}

	logger.info("Created user", { userId });
	return { userId };
}

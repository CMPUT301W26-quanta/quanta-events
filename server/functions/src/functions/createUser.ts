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
});

export async function createUser(request: CallableRequest) {
	const {
		deviceId,
		name,
		email,
		phone,
		receiveNotifications,
	} = util.parseInterface(createUserInterface, request);

	const userId = uuidv4();

	const db = getFirestore();
	try {
		await db
			.collection("users")
			.doc(userId)
			.create(
				util.enforceFull<UserDocument>({
					deviceId,
					name: name,
					email: email,
					phone: phone,
					entrant: {enteredEvents: [], history: [], undismissedNotifications: [], receiveNotifications: receiveNotifications ?? false},
					organizer: { createdEvents: [], sentNotifications: [] },
					admin: null,
					notifToken: null,
				}),
			);
	} catch (_) {
		throw new HttpsError("already-exists", "User already exists");
	}

	logger.info("Created user", { userId });
	return { userId };
}

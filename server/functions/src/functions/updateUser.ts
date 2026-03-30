import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { getFirestore } from "firebase-admin/firestore";

const updateUserInterface = z.object({
	userId: z.uuid(),
	deviceId: z.uuid(),
	data: z.object({
		name: z.string().nullable(),
		email: z.email().nullable(),
		phone: z.string().nullable(),
		receiveNotifications: z.boolean().nullable(),
	}),
});

export async function updateUser(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		updateUserInterface,
		request,
	);

	const { name, email, phone, receiveNotifications } = data;

	await util.verifyUser(userId, deviceId);

	const db = getFirestore();

	const updates: {
		name?: string;
		email?: string;
		phone?: string;
		"entrant.receiveNotifications"?: boolean;
	} = {};

	if (name !== null) {
		updates.name = name;
	}

	if (email !== null) {
		updates.email = email;
	}

	if (phone !== null) {
		updates.phone = phone;
	}

	if (receiveNotifications !== null) {
		updates["entrant.receiveNotifications"] = receiveNotifications;
	}

	await db
		.collection("users")
		.doc(userId)
		.update(util.enforcePartial<UserDocument>(updates));

	logger.info("Updated user", { userId });
	return {};
}

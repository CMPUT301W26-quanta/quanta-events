import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";

const setTokenInterface = util.standardForm(
	z.object({
		token: z.string(),
	})
);

export async function setToken(request: CallableRequest): Promise<void> {
	const { userId, deviceId, data } = util.parseInterface(
		setTokenInterface,
		request
	);

	util.verifyUser(userId, deviceId);

	const { token } = data;

	const db = getFirestore();

	try {
		await db
			.collection("users")
			.doc(userId)
			.update(
				util.enforcePartial<UserDocument>({
					notifToken: token,
				})
			);
	} catch (e) {
		throw new HttpsError("not-found", "User not found");
	}

	return;
}

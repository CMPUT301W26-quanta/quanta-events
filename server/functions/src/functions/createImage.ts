import * as z from "zod";
import * as util from "../util";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const createImageInterface = util.standardForm(
	z.object({
		imageData: z.base64(),
	}),
);

export async function createImage(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		createImageInterface,
		request,
	);

	const { imageData } = data;

	await util.verifyUser(userId, deviceId);

	const imageId = uuidv4();

	const db = getFirestore();

	try {
		await db.collection("images").doc(imageId).create({ imageData });
	} catch (_) {
		throw new HttpsError("already-exists", "Event already exists");
	}

	logger.info("Image Created", { imageId });
	return { imageId };
}

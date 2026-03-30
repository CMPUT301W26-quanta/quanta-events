import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const getImageInterface = util.standardForm(
	z.object({
		imageId: z.uuid(),
	}),
);

export async function getImage(
	request: CallableRequest,
): Promise<ImageDocument> {
	const payload = util.parseInterface(getImageInterface, request);
	await util.verifyUser(payload.userId, payload.deviceId);

	logger.info(`Retrieving image ${payload.data.imageId}.`);

	const db = getFirestore();

	const image = await db.collection("images").doc(payload.data.imageId).get();

	if (!image.exists) {
		throw new HttpsError("not-found", "Image not found");
	}

	return image.data() as ImageDocument;
}

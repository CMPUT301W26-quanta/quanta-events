import { CallableRequest } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { logger } from "firebase-functions";

const deleteImageInterface = util.standardForm(
	z.object({
		target: z.uuid(),
	}),
);

export async function deleteImage(request: CallableRequest) {
	const payload = util.parseInterface(deleteImageInterface, request);

	const userData = await util.verifyUser(payload.userId, payload.deviceId);
	await util.requireRole(userData, "admin");

	logger.info(`Deleting image ${payload.data.target}.`);

	await util.removeImage(payload.data.target);

	return {};
}

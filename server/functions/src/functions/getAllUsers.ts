import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import * as util from "../util";

const getAllUsersInterface = util.standardForm(z.object({}));

export async function getAllUsers(
	request: CallableRequest,
): Promise<ExternalUser[]> {
	const payload = util.parseInterface(getAllUsersInterface, request);
	const userData = await util.verifyUser(payload.userId, payload.deviceId);
	util.requireRole(userData, "admin");

	logger.info("Retrieving all users.");

	const db = getFirestore();

	return (await db.collection("users").get()).docs.map(
		util.userDocToExternalUser,
	);
}

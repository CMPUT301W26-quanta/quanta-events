import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import * as util from "../util";
import Fuse from "fuse.js";

const getAllUsersInterface = util.standardForm(
	z.object({
		search: z.string().nullable(), // Search string for finding users
	}),
);

export async function getAllUsers(
	request: CallableRequest,
): Promise<ExternalUser[]> {
	const {
		userId,
		deviceId,
		data: { search },
	} = util.parseInterface(getAllUsersInterface, request);
	const userData = await util.verifyUser(userId, deviceId);

	const db = getFirestore();

	if (search === null) {
		// util.requireRole(userData, "admin");

		logger.info("Retrieving all users.");

		return (await db.collection("users").get()).docs.map(
			util.userDocToExternalUser,
		);
	} else {
		util.requireRole(userData, "organizer");

		logger.info("Retrieving searched users.");

		const allUsers = (
			await db.collection("users").where("entrant", "!=", null).get()
		).docs.map(util.userDocToExternalUser);

		const searchedUsers = new Fuse(allUsers, {
			keys: [
				{
					name: "name",
					getFn: (data) => data.name ?? "",
				},
				{
					name: "phone",
					getFn: (data) => data.phone ?? "",
				},
				{
					name: "email",
					getFn: (data) => data.email ?? "",
				},
			],
		})
			.search(search)
			.map((result) => result.item);

		return searchedUsers;
	}
}

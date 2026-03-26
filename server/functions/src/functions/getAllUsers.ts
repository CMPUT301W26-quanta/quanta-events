import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import * as util from "../util";
import { ExternalUser } from "./schema";

const getAllUsersInterface = util.standardForm(z.object({}));

export async function getAllUsers(
  request: CallableRequest
): Promise<ExternalUser[]> {
  const { userId, deviceId } = util.parseInterface(
    getAllUsersInterface,
    request
  );

  const userData = await util.verifyUser(userId, deviceId);
  await util.requireRole(userData, "admin");

  const db = getFirestore();

  const users = (await db.collection("users").get()).docs.map((doc) =>
    util.userDocToExternalUser(doc)
  );

  logger.info("Get all users.");
  return users;
}

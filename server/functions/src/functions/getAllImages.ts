import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import * as util from "../util";

const getAllImagesInterface = util.standardForm(z.object({}));

export async function getAllImages(
  request: CallableRequest,
): Promise<string[]> {
  const { userId, deviceId } = util.parseInterface(
	getAllImagesInterface,
	request,
  );

  const userData = await util.verifyUser(userId, deviceId);
  await util.requireRole(userData, "admin");

  const db = getFirestore();

  const imageIDs = (await db.collection("images").get()).docs.map((doc) => doc.id);

  logger.info("Get all users.");
  return imageIDs;
}

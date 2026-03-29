import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const deleteImageInterface = util.standardForm(
  z.object({
    target: z.uuid(),
  }),
);

export async function deleteImage(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    deleteImageInterface,
    request,
  );

  const userData = await util.verifyUser(userId, deviceId);
  await util.requireRole(userData, "admin");

  const db = getFirestore();
  const imageToDeleteId = data.target;

  const targetDoc = await db.collection("images").doc(imageToDeleteId).get();
  logger.log(targetDoc);

  if (!targetDoc.exists) {
    throw new HttpsError("not-found", "Target image does not exist");
  }

  await util.removeImage(data.target);

  return {};
}

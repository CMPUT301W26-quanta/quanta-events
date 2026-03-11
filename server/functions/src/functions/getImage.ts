import * as z from "zod";
import * as util from "../util";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const getImageInterface = util.standardForm(
  z.object({
    imageId: z.uuid(),
  })
);

export async function getImage(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    getImageInterface,
    request
  );
  const { imageId } = data;

  await util.verifyUser(userId, deviceId);

  const db = getFirestore();

  const imageDoc = await db.collection("images").doc(imageId).get();

  if (!imageDoc.exists) {
    throw new HttpsError("not-found", "Image not found");
  }

  logger.info("Found image", { imageId });
  return imageDoc.data();
}

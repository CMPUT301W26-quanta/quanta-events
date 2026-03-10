import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { logger } from "firebase-functions";

const getUserInterface = z.object({
  userId: z.uuid(),
  deviceId: z.uuid(),
});

export async function getUser(request: CallableRequest) {
  const { userId, deviceId } = util.parseInterface(getUserInterface, request);

  const userData = await util.verifyUser(userId, deviceId);

  logger.info("User found", { userId });
  return userData;
}

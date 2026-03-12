import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { getFirestore } from "firebase-admin/firestore";

const updateUserInterface = z.object({
  userId: z.uuid(),
  deviceId: z.uuid(),
  data: z.object({
    name: z.string().nullable(),
    email: z.email().nullable(),
    phone: z.string().nullable(),
    receiveNotifications: z.boolean().nullable(),
  }),
});

export async function updateUser(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(updateUserInterface, request);

  const { name, email, phone, receiveNotifications } = data;

  await util.verifyUser(userId, deviceId);

  const db = getFirestore();

  const updates: Record<string, any> = {
    name,
    email,
    ...(phone !== undefined && { phone }),
    ...(receiveNotifications !== undefined && {
      "entrant.receiveNotifications": receiveNotifications,
    }),
  };

  await db.collection("users").doc(userId).update(updates);

  logger.info("Updated user", { userId });
  return { userId };
}

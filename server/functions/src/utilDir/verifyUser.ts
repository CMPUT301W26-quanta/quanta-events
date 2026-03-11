import { getFirestore } from "firebase-admin/firestore";
import { HttpsError } from "firebase-functions/https";

export async function verifyUser(userId: string, deviceId: string) {
  const db = getFirestore();

  const userDoc = await db.collection("users").doc(userId).get();

  if (!userDoc.exists) {
    throw new HttpsError("not-found", "User does not exist");
  }

  const data = userDoc.data()!;

  if (data.deviceId !== deviceId) {
    throw new HttpsError("unauthenticated", "Device ID does not match");
  }
  return data;
}

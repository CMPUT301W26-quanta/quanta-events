import { DocumentSnapshot, getFirestore } from "firebase-admin/firestore";
import { HttpsError } from "firebase-functions/https";

export async function verifyUser(
	userId: string,
	deviceId: string,
): Promise<UserDocument> {
	const db = getFirestore();

	const userDoc: DocumentSnapshot<UserDocument, UserDocument> = (await db
		.collection("users")
		.doc(userId)
		.get()) as DocumentSnapshot<UserDocument, UserDocument>;

	if (!userDoc.exists) {
		throw new HttpsError("not-found", "User does not exist");
	}

	const data = userDoc.data()!;

	if (data.deviceId !== deviceId) {
		throw new HttpsError("unauthenticated", "Device ID does not match");
	}
	return data;
}

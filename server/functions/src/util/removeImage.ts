import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

/**
 * Removes the image from the database.
 * Sets all references to it to null.
 * Does nothing if the given image is invalid; perform validation yourself.
 * @param imageId The id of the image to delete.
 */
export async function removeImage(imageId: string) {
	const db = getFirestore();

	const targetDoc = await db.collection("images").doc(imageId).get();
	if (!targetDoc.exists) return;

	const eventsSnapshot = await db.collection("events").where("imageId", "==", imageId).get();
	await Promise.all(eventsSnapshot.docs.map((eventDoc) => eventDoc.ref.update({ imageId: null })));

	await db.collection("images").doc(imageId).delete();

	logger.info("Deleted image and all references", { imageId });
}

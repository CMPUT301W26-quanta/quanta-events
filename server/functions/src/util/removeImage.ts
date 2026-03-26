import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { EventDocument } from "../schema";

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

	const eventsSnapshot = await db.collection("events").get();

	const eventUpdates = eventsSnapshot.docs.map(async (eventDoc) => {
		const event = eventDoc.data() as EventDocument;

		if (event.imageId === imageId) {
			eventDoc.ref.update({ imageId: null });
		}

		return;
	});

	await Promise.all(eventUpdates);
	await db.collection("images").doc(imageId).delete();

	logger.info("Deleted image and all references", { imageId });
}

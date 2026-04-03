import { getFirestore } from "firebase-admin/firestore";

export async function commentDocToExternalComment(
	doc: FirebaseFirestore.QueryDocumentSnapshot<
		FirebaseFirestore.DocumentData,
		FirebaseFirestore.DocumentData
	>,
): Promise<ExternalComment> {
	const comment = doc.data() as CommentDocument;

	const db = getFirestore();

	const userRef = db.collection("users").doc(comment.senderId);
	const user = await userRef.get();

	let username;

	if (!user.exists) {
		username = "Deleted User";
	}
	else {
		username = (user.data() as UserDocument).name ?? "Unnamed User";
	}

	return {
		commentId: doc.id,

		senderName: username,
		senderId: comment.senderId,
		postTime: comment.postTime,
		message: comment.message,
	};
}

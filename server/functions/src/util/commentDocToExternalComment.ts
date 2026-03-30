export function commentDocToExternalComment(
	doc: FirebaseFirestore.QueryDocumentSnapshot<
		FirebaseFirestore.DocumentData,
		FirebaseFirestore.DocumentData
	>,
): ExternalComment {
	const comment = doc.data() as CommentDocument;

	return {
		commentId: doc.id,

		senderId: comment.senderId,
		message: comment.message,
	};
}

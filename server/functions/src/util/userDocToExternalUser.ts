export function userDocToExternalUser(
	doc: FirebaseFirestore.QueryDocumentSnapshot<
		FirebaseFirestore.DocumentData,
		FirebaseFirestore.DocumentData
	>,
): ExternalUser {
	const user = doc.data() as UserDocument;

	return {
		userId: doc.id,
		name: user.name,
		phone: user.phone,
		email: user.email,
		isAdmin: !!user.admin,
		isOrganizer: !!user.organizer,
		isEntrant: !!user.entrant,
	};
}

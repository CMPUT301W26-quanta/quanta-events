import { ExternalUser, User } from "../functions/schema";

export function userDocToExternalUser(
  doc: FirebaseFirestore.QueryDocumentSnapshot<
    FirebaseFirestore.DocumentData,
    FirebaseFirestore.DocumentData
  >,
): ExternalUser {
  const user = doc.data() as User;

  return {
    userId: doc.id,
    name: user.name,
    isAdmin: !!user.admin,
    isOrganizer: !!user.organizer,
    isEntrant: !!user.entrant,
  };
}

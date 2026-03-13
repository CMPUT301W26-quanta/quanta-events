import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import { User } from "./schema";

interface Result {
  name: string;
  isAdmin: boolean;
  isOrganizer: boolean;
  isEntrant: boolean;
}

export async function getAllUsers(_request: CallableRequest): Promise<Result[]> {
  const db = getFirestore();

  const users = (await db.collection("users").get()).docs.map((doc) => {
    const user = doc.data() as User;
    return {
      name: user.name,
      isAdmin: !!user.admin,
      isOrganizer: !!user.organizer,
      isEntrant: !!user.entrant,
    };
  });

  logger.info("Get all users.");
  return users;
}

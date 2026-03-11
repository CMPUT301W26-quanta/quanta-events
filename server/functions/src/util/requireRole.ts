import { HttpsError } from "firebase-functions/https";

export type Role = "entrant" | "admin" | "organizer";

export async function requireRole(userData: any, role: Role) {
  if (!userData[role] || userData[role] === null) {
    throw new HttpsError("permission-denied", `User is not an ${role}`);
  }
}

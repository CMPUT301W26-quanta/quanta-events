import { HttpsError } from "firebase-functions/https";
import { UserDocument, UserRole, WithRole } from "../schema";

export function requireRole<U extends UserDocument, R extends UserRole>(
  userData: U,
  role: R
): WithRole<U, R> {
  if (!userData[role] || userData[role] === null) {
    userData[role];
    throw new HttpsError("permission-denied", `User is not an ${role}`);
  }
  return userData as WithRole<U, R>;
}

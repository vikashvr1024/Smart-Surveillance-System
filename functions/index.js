const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.setUserRole = functions.https.onCall(async (data, context) => {
    if (!context.auth || context.auth.token.role !== "admin") {
        throw new functions.https.HttpsError("permission-denied", "Only admins can assign roles.");
    }

    const { uid, role } = data;
    if (!["admin", "manager", "employee", "user"].includes(role)) {
        throw new functions.https.HttpsError("invalid-argument", "Invalid role.");
    }

    await admin.auth().setCustomUserClaims(uid, { role });
    return { message: `Role ${role} assigned to user ${uid}` };
});

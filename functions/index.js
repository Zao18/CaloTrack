const functions = require("firebase-functions");
const admin = require("firebase-admin");
const express = require("express");
const cors = require("cors");

// Initialize Firebase
admin.initializeApp();

const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

// Root route
app.get("/", (req, res) => {
    res.status(200).json({ message: "CaloTrack API is running!" });
});

// GET user by UID
app.get("/users/:uid", async (req, res) => {
    try {
        const uid = req.params.uid;
        const snapshot = await admin.database().ref(`/users/${uid}`).once("value");
        if (!snapshot.exists()) {
            return res.status(404).json({ message: "User not found" });
        }
        res.status(200).json(snapshot.val());
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST new user
app.post("/users", async (req, res) => {
    try {
        const userData = req.body; // expect full user object
        if (!userData.uid) return res.status(400).json({ message: "Missing uid" });
        await admin.database().ref(`/users/${userData.uid}`).set(userData);
        res.status(200).json({ message: "User saved!" });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// PUT update user (merge)
app.put("/users/:uid", async (req, res) => {
    try {
        const uid = req.params.uid;
        const updateData = req.body;
        if (!uid || !updateData) return res.status(400).json({ message: "Missing uid or data" });

        // Merge the update with existing user
        await admin.database().ref(`/users/${uid}`).update(updateData);
        res.status(200).json({ message: "User updated!" });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// PUT clear food log
app.put("/users/:uid/clearFoodLog", async (req, res) => {
    try {
        const uid = req.params.uid;
        if (!uid) return res.status(400).json({ message: "Missing uid" });

        await admin.database().ref(`/users/${uid}/food_log`).set({});
        res.status(200).json({ message: "Food log cleared!" });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.post("/saveToken", async (req, res) => {
    try {
        const { uid, token } = req.body;

        if (!uid || !token) {
            return res.status(400).json({ message: "Missing uid or token" });
        }

        await admin.database().ref(`/users/${uid}/fcmToken`).set(token);

        res.status(200).json({ message: "Token saved!" });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

exports.api = functions.https.onRequest(app);

exports.dailyReminder = functions.pubsub
    .schedule("every day 08:00") // 08:00 AM every day
    .timeZone("Africa/Johannesburg") // <-- your timezone
    .onRun(async (context) => {
        const usersSnapshot = await admin.database().ref("/users").once("value");

        const messages = [];

        usersSnapshot.forEach((user) => {
            const token = user.child("fcmToken").val();
            // NEW: Check the notifications_enabled flag (defaulting to true if not set)
            const notificationsEnabled = user.child("notifications_enabled").val();

            // Only send if a token exists AND notifications are NOT explicitly set to false
            if (token && notificationsEnabled !== false) {
                messages.push({
                    token: token,
                    notification: {
                        title: "Daily Reminder",
                        body: "Don't forget to track your meals today!",
                    },
                });
            }
        });

        // Send notifications in batches
        const results = await admin.messaging().sendEach(messages);
        console.log("Daily notifications sent:", results.successCount);

        return null;
    });






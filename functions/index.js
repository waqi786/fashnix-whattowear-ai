const admin = require("firebase-admin");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { defineSecret } = require("firebase-functions/params");
const { OpenAI } = require("openai");

admin.initializeApp();

const db = admin.firestore();
const openAiKey = defineSecret("OPENAI_API_KEY");

exports.askAI = onCall({ secrets: [openAiKey], region: "us-central1" }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Please sign in to use AI chat.");
  }

  const apiKey = openAiKey.value();
  if (!apiKey) {
    throw new HttpsError("failed-precondition", "OpenAI is not configured on the server.");
  }

  const { messages = [], userProfile = {}, wardrobeItems = [], currentWeather } = request.data || {};
  if (!Array.isArray(messages) || messages.length === 0 || messages.length > 20) {
    throw new HttpsError("invalid-argument", "Send 1 to 20 chat messages.");
  }

  const normalizedMessages = messages.map((message) => ({
    role: message.role === "assistant" ? "assistant" : "user",
    content: String(message.content || "").slice(0, 2000),
  })).filter((message) => message.content.length > 0);

  if (normalizedMessages.length === 0) {
    throw new HttpsError("invalid-argument", "Message content cannot be empty.");
  }

  const wardrobeDescription = wardrobeItems.slice(0, 50).map((item) =>
    `${item.name || "Item"} (${item.category || "unknown"}, ${item.color || "unknown"}, ${item.occasion || "any"}, ${item.gender || "unisex"})`
  ).join(", ");

  const systemPrompt = [
    "You are Fashnix, a professional and friendly AI fashion stylist assistant.",
    `User profile: gender ${userProfile.gender || "unknown"}, body type ${userProfile.bodyType || "unknown"}, skin tone ${userProfile.skinTone || "unknown"}.`,
    `Wardrobe: ${wardrobeDescription || "No saved wardrobe items yet."}`,
    currentWeather ? `Current weather: ${currentWeather}.` : "",
    "Give practical, concise advice. Suggest specific wardrobe items when relevant. Keep responses under 200 words unless the user asks for more.",
  ].filter(Boolean).join(" ");

  try {
    const openai = new OpenAI({ apiKey });
    const response = await openai.chat.completions.create({
      model: "gpt-4o-mini",
      messages: [
        { role: "system", content: systemPrompt },
        ...normalizedMessages,
      ],
      max_tokens: 500,
      temperature: 0.7,
    });

    return { reply: response.choices?.[0]?.message?.content || "I could not prepare advice right now." };
  } catch (error) {
    console.error("askAI failed", error);
    throw new HttpsError("internal", "AI service failed. Please try again.");
  }
});

exports.sendDailyOutfitNotification = onSchedule({
  schedule: "0 6 * * *",
  timeZone: "Asia/Karachi",
  region: "us-central1",
}, async () => {
  const usersSnapshot = await db.collection("users").get();

  for (const userDoc of usersSnapshot.docs) {
    const user = userDoc.data();
    if (!user.fcmToken) continue;

    const wardrobeSnapshot = await db.collection("wardrobe")
      .where("userId", "==", userDoc.id)
      .where("laundryStatus", "==", "Clean")
      .get();

    const wardrobe = wardrobeSnapshot.docs.map((doc) => doc.data());
    const topwear = wardrobe.find((item) => /top|shirt|tee|apparel|dress/i.test(item.category || ""));
    const footwear = wardrobe.find((item) => /shoe|footwear/i.test(item.category || ""));
    const accessory = wardrobe.find((item) => /accessor/i.test(item.category || ""));

    if (!topwear) continue;

    const outfit = [topwear, footwear, accessory]
      .filter(Boolean)
      .map((item) => item.name || `${item.color || ""} ${item.category || "item"}`.trim())
      .join(" + ");

    await admin.messaging().send({
      token: user.fcmToken,
      notification: {
        title: "Good morning! Here's your outfit today",
        body: `Today's outfit: ${outfit}`,
      },
      data: {
        deepLink: "fashnix://home",
        outfitItemId: topwear.id || "",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "daily_outfit",
          color: "#D70F64",
          icon: "ic_notification",
        },
      },
    });
  }
});

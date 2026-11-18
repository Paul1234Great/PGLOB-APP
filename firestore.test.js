const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ts = () => new Date(Date.now() - 1000);

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read private users or transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("transactions").where("userId", "==", ALICE_UID).get());
});

test("Authenticated user: can create and read own private profile, cannot read another user's private profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice Globe",
      username: "alice_globe",
      coins: 100,
      createdAt: ts(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Shadow update / privilege escalation rejected on users", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Alice",
      username: "alice_1",
      isBanned: true,
      createdAt: ts(),
    })
  );
});

test("Chat threads and messages enforce memberUids membership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_1").set({
      title: "Alice & Bob",
      memberUids: [ALICE_UID, BOB_UID],
      lastMessage: "Hello!",
      createdAt: ts(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_1").collection("messages").doc("msg_1").set({
      chatId: "chat_1",
      senderId: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      text: "Welcome to PGLOB CHAT!",
      messageType: "TEXT",
      createdAt: ts(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("chats").doc("chat_1").collection("messages")
      .where("memberUids", "array-contains", BOB_UID).get()
  );

  const eveDb = testEnv.authenticatedContext("eve_999").firestore();
  await assertFails(eveDb.collection("chats").doc("chat_1").get());
  await assertFails(eveDb.collection("chats").doc("chat_1").collection("messages").doc("msg_1").get());
  await assertFails(eveDb.collection("chats").doc("chat_1").collection("messages").get());
});

test("Transactions enforce userId filter and prevent non-admin status update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      txType: "WITHDRAW",
      amount: 500,
      fee: 50,
      status: "PENDING",
      createdAt: ts(),
    })
  );

  await assertSucceeds(aliceDb.collection("transactions").where("userId", "==", ALICE_UID).get());
  await assertFails(aliceDb.collection("transactions").doc("tx_1").update({ status: "PAID" }));
});

test("Public profiles allow owner bio/interests/photoUri writes and other-user read visibility only", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("publicProfiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Globe",
      username: "alice_globe",
      isPublic: true,
      bio: "Building communities & Afrobeats creator",
      interests: ["Afrobeats", "Ajo Savings", "Tech & AI"],
      photoUri: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD",
      createdAt: ts(),
    })
  );

  // Other signed-in users can view Alice's public profile
  const pubSnap = await assertSucceeds(bobDb.collection("publicProfiles").doc(ALICE_UID).get());
  if (!pubSnap.exists || pubSnap.data().bio !== "Building communities & Afrobeats creator") {
    throw new Error("Expected Bob to read Alice's public profile bio and interests");
  }

  // Other users cannot overwrite Alice's public profile
  await assertFails(
    bobDb.collection("publicProfiles").doc(ALICE_UID).update({ bio: "Hacked bio" })
  );

  // Oversized bio (> 300 chars) or too many interests (> 15) rejected
  await assertFails(
    aliceDb.collection("publicProfiles").doc(ALICE_UID).update({ bio: "a".repeat(301) })
  );
  await assertFails(
    aliceDb.collection("publicProfiles").doc(ALICE_UID).update({
      interests: Array.from({ length: 16 }, (_, i) => `tag_${i}`),
    })
  );
});

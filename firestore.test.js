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

test("Unauthenticated user: cannot read bills", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bills").get());
});

test("Authenticated user: cannot read another user's bills", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("bills").doc("bill_bob_1").set({
      userId: BOB_UID,
      title: "Bob Internet",
      amount: 60.0,
      dueDateEpochDay: 20000,
      isPaid: false,
      reminderDaysBefore: 3,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("bills").doc("bill_bob_1").get());
});

test("Authenticated user: can create valid bill in their own collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bills").doc("bill_1").set({
      userId: ALICE_UID,
      title: "Electricity Bill",
      amount: 85.50,
      currency: "$",
      category: "Utilities",
      dueDateEpochDay: 20050,
      dueDayOfMonth: 15,
      isRecurring: true,
      frequency: "MONTHLY",
      isPaid: false,
      paidAtEpochMillis: null,
      reminderDaysBefore: 3,
      reminderEnabled: true,
      notes: "Account #12345",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create bill with mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("bills").doc("bill_2").set({
      userId: BOB_UID,
      title: "Spoofed Bill",
      amount: 100.0,
      dueDateEpochDay: 20050,
      isPaid: false,
      reminderDaysBefore: 3,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can update bill status to paid", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("bills").doc("bill_3").set({
      userId: ALICE_UID,
      title: "Water Bill",
      amount: 45.0,
      dueDateEpochDay: 20050,
      isPaid: false,
      reminderDaysBefore: 2,
      createdAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bills").doc("bill_3").update({
      isPaid: true,
      paidAtEpochMillis: Date.now(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can delete own bill", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("bills").doc("bill_4").set({
      userId: ALICE_UID,
      title: "Trash Bill",
      amount: 25.0,
      dueDateEpochDay: 20050,
      isPaid: false,
      reminderDaysBefore: 1,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bills").doc("bill_4").delete()
  );
});

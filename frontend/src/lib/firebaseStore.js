const databaseUrl = import.meta.env.VITE_FIREBASE_DATABASE_URL || "";
const apiKey = import.meta.env.VITE_FIREBASE_API_KEY || "";

const cleanPath = (path) => path
  .split("/")
  .map((part) => encodeURIComponent(part.trim()))
  .filter(Boolean)
  .join("/");

export const isFirebaseConfigured = () => Boolean(databaseUrl);

export const saveFirebaseRecord = async (path, payload) => {
  if (!isFirebaseConfigured()) {
    return { saved: false, reason: "Firebase is not configured." };
  }
  const base = databaseUrl.replace(/\/$/, "");
  const query = apiKey ? `?key=${encodeURIComponent(apiKey)}` : "";
  const response = await fetch(`${base}/${cleanPath(path)}.json${query}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      ...payload,
      savedAt: new Date().toISOString(),
    }),
  });
  if (!response.ok) {
    throw new Error(`Firebase save failed with status ${response.status}`);
  }
  return { saved: true };
};

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export async function searchJobs(q, page = 0, size = 10) {
  const url = `${BASE_URL}/search?q=${encodeURIComponent(q)}&page=${page}&size=${size}`;
  const res = await fetch(url);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API error ${res.status}: ${text}`);
  }
  return res.json();
}

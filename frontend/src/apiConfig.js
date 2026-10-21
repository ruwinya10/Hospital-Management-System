// This setting is public and is embedded into the frontend at build time.
const configured = (import.meta.env.VITE_API_BASE_URL || '/api').trim().replace(/\/+$/, '');
export const API_BASE_URL = configured;

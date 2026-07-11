export type ModuleKey = 'workflows' | 'inventory' | 'staff' | 'orders' | 'invoices';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export async function fetchModule<T>(module: ModuleKey, accessToken?: string): Promise<T[]> {
  const response = await fetch(`${API_BASE_URL}/${module}`, {
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  });

  if (!response.ok) {
    throw new Error(`Unable to load ${module}: ${response.status}`);
  }
  return response.json();
}

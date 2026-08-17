import axios from 'axios';

const refreshClient = axios.create({
    withCredentials: true,
});

export async function refreshToken(): Promise<void> {
    await refreshClient.post('/api/auth/refresh');
}

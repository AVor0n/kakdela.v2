import type { Subscribers } from '@/shared/types/Subscribers.type';
import { apiClient } from './client';

type AddSubscriberResponse = {
    subscribedEmails: string[];
    alreadySubscribedEmails: string[];
    notFoundEmails: string[];
};

export async function getSubscribers(surveyId: string): Promise<Subscribers[]> {
    const { data } = await apiClient<Subscribers[]>(`/api/surveys/${surveyId}/subscribers`);

    return data;
}

export async function addSubscriber(surveyId: string, email: string): Promise<AddSubscriberResponse> {
    const { data } = await apiClient.post<AddSubscriberResponse>(`/api/surveys/${surveyId}/subscribers`, {
        emails: [email],
    });

    return data;
}

export async function deleteSubscriber(surveyId: string, email: string): Promise<void> {
    await apiClient.delete(`/api/surveys/${surveyId}/subscribers?email=${email}`);
}
